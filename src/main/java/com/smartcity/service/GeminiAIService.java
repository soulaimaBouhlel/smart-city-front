package com.smartcity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import com.smartcity.model.Intervention;
import com.smartcity.model.Technician;
import com.smartcity.model.Skill;
import java.util.*;

@Service
public class GeminiAIService {

    private final String API_KEY = "AIzaSyCDQY8_Rdww_B9VSuNse8GAPXJHt6DmqT8";
    private final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * AI analyzes intervention and returns complete analysis
     */
    public InterventionAnalysis analyzeIntervention(String description, String interventionType) {
        try {
            String prompt = """
                Analyze this city maintenance problem and return ONLY in this exact format:
                PRIORITY:CRITICAL|HIGH|MEDIUM|LOW
                URGENCY:1-10
                SKILLS:skill1,skill2,skill3
                
                Problem: "%s"
                Type: %s
                
                Considerations:
                - PRIORITY: Consider public safety risk, infrastructure impact, emergency level
                - URGENCY: 1=lowest (routine), 10=highest (life-threatening)
                - SKILLS: Choose from ELECTRICAL, PLUMBING, ROAD_WORK, GENERAL, EMERGENCY, HEIGHTS, CONSTRUCTION, NETWORK, WASTE_MANAGEMENT, TRAFFIC_CONTROL
                
                Return ONLY the 3 lines in the exact format above.
                """.formatted(description, interventionType != null ? interventionType : "UNKNOWN");

            String response = callGeminiDirect(prompt);
            return parseInterventionAnalysis(response);
            
        } catch (Exception e) {
            return getFallbackAnalysis();
        }
    }

    /**
     * AI determines the best technician based on contextual relevance
     */
    public Technician findBestTechnician(Intervention intervention, List<Technician> availableTechnicians) {
        try {
            if (availableTechnicians.isEmpty()) {
                return null;
            }

            // Get AI analysis for this intervention
            InterventionAnalysis analysis = analyzeIntervention(
                intervention.getDescription(), 
                intervention.getInterventionType()
            );

            // Find technician with best AI-calculated relevance
            Technician bestTech = null;
            double bestRelevance = 0;

            for (Technician tech : availableTechnicians) {
                if (tech.isAvailable()) {
                    double relevance = calculateTechnicianRelevance(tech, intervention, analysis);
                    
                    if (relevance > bestRelevance) {
                        bestRelevance = relevance;
                        bestTech = tech;
                    }
                }
            }

            return bestTech;
            
        } catch (Exception e) {
            return findBestTechnicianFallback(availableTechnicians, intervention);
        }
    }

    /**
     * AI calculates contextual relevance score for a technician
     */
    private double calculateTechnicianRelevance(Technician tech, Intervention intervention, InterventionAnalysis analysis) {
        try {
            String prompt = String.format(
                "Calculate relevance score 0.0-1.0 for this technician.%n%n" +
                "PROBLEM: \"%s\" (PRIORITY: %s)%n" +
                "REQUIRED SKILLS: %s ⚠️ SKILL MATCH IS MOST IMPORTANT%n%n" +
                "TECHNICIAN SKILLS: %s%n" +
                "WORKLOAD: %d/5 | RATING: %.1f/5%n%n" +
                "Scoring priority:%n" +
                "1. Required skill matches (MOST IMPORTANT - 70%%)%n" +
                "2. Current workload (20%%) %n" +
                "3. Rating (10%%)%n%n" +
                "Return ONLY a number 0.0-1.0",
                intervention.getDescription(),
                analysis.getPriority(),
                analysis.getSkills(),
                tech.getSkills(),
                tech.getCurrentWorkload() != null ? tech.getCurrentWorkload() : 0,
                tech.getRating() != null ? tech.getRating() : 0.0
            );

            String response = callGeminiDirect(prompt);
            return parseRelevanceScore(response);
            
        } catch (Exception e) {
            return calculateSmarterFallbackRelevance(tech, intervention, analysis);
        }
    }

    /**
     * SMARTER fallback relevance calculation - PRIORITIZE SKILL MATCHES
     */
    private double calculateSmarterFallbackRelevance(Technician tech, Intervention intervention, InterventionAnalysis analysis) {
        double score = 0.0;
        
        // 1. SKILL MATCH (70% weight) - MOST IMPORTANT
        double skillScore = calculateSkillMatchPriority(tech, analysis.getSkills());
        score += skillScore * 0.7;
        
        // 2. WORKLOAD (20% weight) - Secondary importance
        double workloadScore = calculateWorkloadScore(tech.getCurrentWorkload());
        score += workloadScore * 0.2;
        
        // 3. RATING (10% weight) - Least important
        double ratingScore = calculateRatingScore(tech.getRating());
        score += ratingScore * 0.1;
        
        // 4. BONUS: If technician has ANY required skill, ensure minimum score
        if (skillScore > 0 && score < 0.5) {
            score = 0.5;
        }
        
        return score;
    }

    /**
     * Priority-based skill matching
     */
    private double calculateSkillMatchPriority(Technician tech, Set<Skill> requiredSkills) {
        if (requiredSkills.isEmpty()) return 0.5;
        if (tech.getSkills() == null || tech.getSkills().isEmpty()) return 0.0;
        
        Set<Skill> techSkills = tech.getSkills();
        
        // Count exact matches
        long exactMatches = requiredSkills.stream()
                .filter(techSkills::contains)
                .count();
        
        double baseScore = (double) exactMatches / requiredSkills.size();
        
        // BONUS: If technician has ALL required skills
        if (exactMatches == requiredSkills.size()) {
            return 1.0;
        }
        
        // BONUS: If technician has at least one required skill
        if (exactMatches > 0) {
            return Math.max(baseScore, 0.7);
        }
        
        // Check for emergency/general fallback
        if (techSkills.contains(Skill.EMERGENCY) || techSkills.contains(Skill.GENERAL)) {
            return 0.3;
        }
        
        return 0.0;
    }

    private double calculateWorkloadScore(Integer workload) {
        if (workload == null) return 1.0;
        if (workload == 0) return 1.0;
        if (workload <= 2) return 0.8;
        if (workload <= 4) return 0.5;
        return 0.2;
    }

    private double calculateRatingScore(Double rating) {
        if (rating == null) return 0.5;
        return rating / 5.0;
    }

    /**
     * Parse AI response for intervention analysis
     */
    private InterventionAnalysis parseInterventionAnalysis(String response) {
        try {
            String priority = "MEDIUM";
            int urgency = 5;
            Set<Skill> skills = new HashSet<>(Set.of(Skill.GENERAL));

            String[] lines = response.split("\n");
            for (String line : lines) {
                if (line.startsWith("PRIORITY:")) {
                    priority = line.replace("PRIORITY:", "").trim();
                } else if (line.startsWith("URGENCY:")) {
                    String urgencyStr = line.replace("URGENCY:", "").replaceAll("[^0-9]", "").trim();
                    if (!urgencyStr.isEmpty()) {
                        urgency = Math.min(Math.max(Integer.parseInt(urgencyStr), 1), 10);
                    }
                } else if (line.startsWith("SKILLS:")) {
                    skills = parseSkills(line.replace("SKILLS:", "").trim());
                }
            }

            return new InterventionAnalysis(priority, urgency, skills);
            
        } catch (Exception e) {
            return getFallbackAnalysis();
        }
    }

    private Set<Skill> parseSkills(String skillsLine) {
        Set<Skill> skills = new HashSet<>();
        try {
            String[] skillNames = skillsLine.split(",");
            for (String skillName : skillNames) {
                try {
                    Skill skill = Skill.valueOf(skillName.trim().toUpperCase());
                    skills.add(skill);
                } catch (IllegalArgumentException e) {
                    // Skip invalid skills
                }
            }
        } catch (Exception e) {
            // Use default skills
        }
        
        if (skills.isEmpty()) {
            skills.add(Skill.GENERAL);
        }
        
        return skills;
    }

    private double parseRelevanceScore(String response) {
        try {
            String clean = response.replaceAll("[^0-9.]", "").trim();
            if (!clean.isEmpty()) {
                double score = Double.parseDouble(clean);
                return Math.min(Math.max(score, 0.0), 1.0);
            }
            return 0.5;
        } catch (Exception e) {
            return 0.5;
        }
    }

    private InterventionAnalysis getFallbackAnalysis() {
        return new InterventionAnalysis("MEDIUM", 5, Set.of(Skill.GENERAL));
    }

    // Fallback technician selection without AI
    private Technician findBestTechnicianFallback(List<Technician> technicians, Intervention intervention) {
        if (technicians.isEmpty()) return null;
        
        // Get AI analysis for fallback matching
        InterventionAnalysis analysis = analyzeIntervention(
            intervention.getDescription(), 
            intervention.getInterventionType()
        );
        
        Technician bestTech = null;
        double bestScore = 0;
        
        for (Technician tech : technicians) {
            if (tech.isAvailable()) {
                double score = calculateSmarterFallbackRelevance(tech, intervention, analysis);
                if (score > bestScore) {
                    bestScore = score;
                    bestTech = tech;
                }
            }
        }
        
        return bestTech;
    }

    // Existing REST call methods (keep these unchanged)
    private String callGeminiDirect(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, Object> part = new HashMap<>();
            
            part.put("text", prompt);
            content.put("parts", new Object[]{part});
            requestBody.put("contents", new Object[]{content});

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                GEMINI_URL, 
                HttpMethod.POST, 
                entity, 
                Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("candidates")) {
                return extractTextFromResponse(responseBody);
            }
            
            throw new RuntimeException("Invalid API response");
            
        } catch (Exception e) {
            throw new RuntimeException("Gemini API call failed: " + e.getMessage(), e);
        }
    }

    private String extractTextFromResponse(Map<String, Object> response) {
        try {
            java.util.List<?> candidates = (java.util.List<?>) response.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<?, ?> candidate = (Map<?, ?>) candidates.get(0);
                Map<?, ?> content = (Map<?, ?>) candidate.get("content");
                java.util.List<?> parts = (java.util.List<?>) content.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    Map<?, ?> part = (Map<?, ?>) parts.get(0);
                    return (String) part.get("text");
                }
            }
            return "PRIORITY:MEDIUM\nURGENCY:5\nSKILLS:GENERAL";
        } catch (Exception e) {
            return "PRIORITY:MEDIUM\nURGENCY:5\nSKILLS:GENERAL";
        }
    }

    /**
     * Inner class to hold AI analysis results
     */
    public static class InterventionAnalysis {
        private final String priority;
        private final int urgency;
        private final Set<Skill> skills;

        public InterventionAnalysis(String priority, int urgency, Set<Skill> skills) {
            this.priority = priority;
            this.urgency = urgency;
            this.skills = skills;
        }

        public String getPriority() { return priority; }
        public int getUrgency() { return urgency; }
        public Set<Skill> getSkills() { return skills; }
    }
}