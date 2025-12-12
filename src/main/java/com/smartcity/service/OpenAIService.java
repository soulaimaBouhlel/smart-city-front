package com.smartcity.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import com.smartcity.model.Intervention;
import com.smartcity.model.Technician;
import com.smartcity.model.Skill;
import java.util.*;

@Service
public class OpenAIService {

    private final String API_KEY = "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";
    private final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final String AI_MODEL = "gpt-3.5-turbo";
    
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Call OpenAI API with timeout protection
     */
    private String callOpenAI(String prompt) {
        try {
            System.out.println("🤖 Calling OpenAI API (" + AI_MODEL + ")...");
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + API_KEY);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", AI_MODEL);
            requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("max_tokens", 150);
            requestBody.put("temperature", 0.1); // Lower for more consistent
            requestBody.put("timeout", 10); // 10 second timeout
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, entity, Map.class);
            
            if (response.getStatusCode().isError()) {
                throw new RuntimeException("OpenAI API error: " + response.getStatusCode());
            }
            
            Map<String, Object> responseBody = response.getBody();
            
            if (responseBody != null && responseBody.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, Object> message = (Map<String, Object>) choice.get("message");
                    String content = (String) message.get("content");
                    System.out.println("🤖 OpenAI response received");
                    return content;
                }
            }
            
            throw new RuntimeException("OpenAI API returned unexpected response format");
            
        } catch (Exception e) {
            System.err.println("❌ OpenAI API call failed: " + e.getMessage());
            throw new RuntimeException("OpenAI API call failed: " + e.getMessage());
        }
    }

    /**
     * AI analyzes intervention - REALISTIC PRIORITIES
     */
    public InterventionAnalysis analyzeIntervention(
            String description, String interventionType, String equipmentType) {
        
        System.out.println("🤖 AI analyzing intervention...");
        System.out.println("   Description: " + description);
        System.out.println("   Type: " + interventionType);
        System.out.println("   Equipment Type: " + equipmentType);
        
        try {
            String prompt = String.format("""
                Analyze this municipal maintenance request REALISTICALLY:
                
                PROBLEM: "%s"
                TYPE: %s  
                EQUIPMENT: %s
                
                Return EXACTLY 3 lines:
                PRIORITY:CRITICAL|HIGH|MEDIUM|LOW
                URGENCY:1-10
                SKILLS:skill1,skill2,skill3
                
                REALISTIC PRIORITY GUIDELINES:
                - CRITICAL: ONLY for ACTIVE life-threatening emergencies happening NOW (fire with people trapped, building collapse, active shooter)
                - HIGH: Serious public safety risk (flooding streets, gas leak, traffic light out at busy intersection, electrical hazard)
                - MEDIUM: Service disruption (street light out, minor leak, pothole, equipment failure)
                - LOW: Routine work (calibration, maintenance, inspection, cosmetic repair)
                
                REALISTIC URGENCY (1-10):
                - 9-10: Fix immediately (ongoing danger)
                - 7-8: Fix within 2 hours (serious risk)
                - 5-6: Fix within 12 hours (moderate impact)
                - 3-4: Fix within 48 hours (minor impact)
                - 1-2: Fix within 1 week (routine)
                
                REALISTIC SKILLS (choose 1-3 max):
                - ELECTRICAL: for electrical equipment only
                - PLUMBING: for water-related issues only
                - ROAD_WORK: for road/surface issues only
                - GENERAL: for basic tasks
                - EMERGENCY: ONLY if priority is HIGH or CRITICAL
                - HEIGHTS: ONLY if work requires climbing
                
                IMPORTANT: Be conservative with HIGH/CRITICAL. Most issues are MEDIUM.
                
                Now analyze REALISTICALLY:
                """,
                description,
                interventionType != null ? interventionType : "UNKNOWN",
                equipmentType != null ? equipmentType : "UNKNOWN");

            String response = callOpenAI(prompt);
            return parseInterventionAnalysis(response);
            
        } catch (Exception e) {
            System.err.println("🤖 AI analysis failed: " + e.getMessage());
            return realisticFallbackAnalysis(description, interventionType, equipmentType);
        }
    }

    /**
     * REALISTIC fallback - Conservative priority assignment
     */
    private InterventionAnalysis realisticFallbackAnalysis(String description, String interventionType, String equipmentType) {
        System.out.println("🤖 Using realistic fallback (AI failed)");
        
        String desc = description.toLowerCase();
        String equip = equipmentType != null ? equipmentType.toLowerCase() : "";
        
        // CONSERVATIVE PRIORITY ASSIGNMENT
        String priority = "MEDIUM"; // Default to medium (most common)
        int urgency = 5;
        
        // Only promote to HIGH for clear safety risks
        if (desc.contains("flood") && desc.contains("street")) {
            priority = "HIGH";
            urgency = 8;
        } else if (desc.contains("burst") && desc.contains("pipe")) {
            priority = "HIGH";
            urgency = 7;
        } else if (desc.contains("traffic") && desc.contains("signal") && 
                   (desc.contains("broken") || desc.contains("not working"))) {
            priority = "HIGH";
            urgency = 7;
        } else if (desc.contains("light") && desc.contains("out") && desc.contains("main")) {
            priority = "MEDIUM";
            urgency = 6;
        } else if (desc.contains("sensor") && (desc.contains("calibration") || desc.contains("maintenance"))) {
            priority = "LOW";
            urgency = 3;
        } else if (desc.contains("pothole") || desc.contains("road damage")) {
            priority = "MEDIUM";
            urgency = 5;
        }
        
        // Skills based on equipment type
        Set<Skill> skills = new HashSet<>();
        
        if (equip.contains("light") || equip.equals("lighting")) {
            skills.add(Skill.ELECTRICAL);
            skills.add(Skill.HEIGHTS);
        } else if (equip.contains("traffic") || equip.contains("signal")) {
            skills.add(Skill.ELECTRICAL);
            if (priority.equals("HIGH") || priority.equals("CRITICAL")) {
                skills.add(Skill.EMERGENCY);
            }
        } else if (equip.contains("water") || equip.contains("pipe")) {
            skills.add(Skill.PLUMBING);
            if (priority.equals("HIGH") || priority.equals("CRITICAL")) {
                skills.add(Skill.EMERGENCY);
            }
        } else if (equip.contains("road") || equip.contains("street")) {
            skills.add(Skill.ROAD_WORK);
        } else if (equip.contains("sensor")) {
            skills.add(Skill.ELECTRICAL);
        }
        
        // Default to GENERAL if unsure
        if (skills.isEmpty()) {
            skills.add(Skill.GENERAL);
        }
        
        System.out.println("🤖 Realistic fallback: Priority=" + priority + ", Urgency=" + urgency + ", Skills=" + skills);
        return new InterventionAnalysis(priority, urgency, skills);
    }

    /**
     * Parse AI response
     */
    private InterventionAnalysis parseInterventionAnalysis(String response) {
        try {
            String priority = "MEDIUM";
            int urgency = 5;
            Set<Skill> skills = new HashSet<>();

            String[] lines = response.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("PRIORITY:")) {
                    String rawPriority = line.replace("PRIORITY:", "").trim().toUpperCase();
                    // Validate and normalize
                    if (rawPriority.equals("CRITICAL") || rawPriority.equals("HIGH") || 
                        rawPriority.equals("MEDIUM") || rawPriority.equals("LOW")) {
                        priority = rawPriority;
                    }
                } else if (line.startsWith("URGENCY:")) {
                    String urgencyStr = line.replace("URGENCY:", "").replaceAll("[^0-9]", "").trim();
                    if (!urgencyStr.isEmpty()) {
                        urgency = Math.min(Math.max(Integer.parseInt(urgencyStr), 1), 10);
                    }
                } else if (line.startsWith("SKILLS:")) {
                    skills = parseSkills(line.replace("SKILLS:", "").trim());
                }
            }

            System.out.println("🤖 AI Result: Priority=" + priority + ", Urgency=" + urgency + ", Skills=" + skills);
            return new InterventionAnalysis(priority, urgency, skills);
            
        } catch (Exception e) {
            System.err.println("🤖 Error parsing AI response: " + e.getMessage());
            return realisticFallbackAnalysis("", "", "");
        }
    }

    /**
     * Parse skills - strict validation
     */
    private Set<Skill> parseSkills(String skillsLine) {
        Set<Skill> skills = new HashSet<>();
        try {
            String[] skillNames = skillsLine.split(",");
            for (String skillName : skillNames) {
                String cleanName = skillName.trim().toUpperCase();
                // Only accept valid skills
                try {
                    Skill skill = Skill.valueOf(cleanName);
                    skills.add(skill);
                } catch (IllegalArgumentException e) {
                    System.out.println("⚠️ Ignoring invalid skill: " + cleanName);
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error parsing skills: " + e.getMessage());
        }
        
        if (skills.isEmpty()) {
            skills.add(Skill.GENERAL);
        }
        
        return skills;
    }

    /**
     * Find best technician - Robust with error handling
     */
    public Technician findBestTechnician(Intervention intervention, List<Technician> nearbyTechnicians) {
        try {
            if (nearbyTechnicians == null || nearbyTechnicians.isEmpty()) {
                System.out.println("🤖 No nearby technicians provided");
                return null;
            }

            System.out.println("🤖 Evaluating " + nearbyTechnicians.size() + " nearby technicians");
            
            // Get equipment type safely
            String equipmentType = "UNKNOWN";
            try {
                if (intervention != null && intervention.getEquipment() != null && 
                    intervention.getEquipment().getType() != null) {
                    equipmentType = intervention.getEquipment().getType().toString();
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error getting equipment type: " + e.getMessage());
            }
            
            // Get AI analysis
            InterventionAnalysis analysis = analyzeIntervention(
                intervention != null ? intervention.getDescription() : "",
                intervention != null ? intervention.getInterventionType() : "",
                equipmentType
            );
            
            System.out.println("🤖 AI says: Need " + analysis.getSkills() + " (Priority: " + analysis.getPriority() + ")");

            // Evaluate technicians
            Technician bestTech = null;
            double bestScore = 0;

            for (Technician tech : nearbyTechnicians) {
                try {
                    if (tech != null && tech.isAvailable()) {
                        double score = calculateTechnicianScore(tech, analysis);
                        
                        System.out.println("   " + tech.getName() + 
                            " | Skills: " + tech.getSkills() + 
                            " | Score: " + String.format("%.2f", score));
                        
                        if (score > bestScore) {
                            bestScore = score;
                            bestTech = tech;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Error evaluating technician " + tech.getName() + ": " + e.getMessage());
                }
            }

            if (bestTech != null) {
                System.out.println("✅ Selected: " + bestTech.getName() + " (Score: " + String.format("%.2f", bestScore) + ")");
            } else {
                System.out.println("❌ No suitable technician found");
            }

            return bestTech;
            
        } catch (Exception e) {
            System.err.println("🤖 Critical error in findBestTechnician: " + e.getMessage());
            e.printStackTrace();
            return safeTechnicianFallback(nearbyTechnicians);
        }
    }

    /**
     * Calculate technician score
     */
    private double calculateTechnicianScore(Technician tech, InterventionAnalysis analysis) {
        try {
            double totalScore = 0.0;
            
            // 1. Skill Match (75% weight)
            double skillScore = calculateSkillMatch(tech, analysis.getSkills());
            totalScore += skillScore * 0.75;
            
            // 2. Workload (20% weight)
            double workloadScore = calculateWorkloadScore(tech.getCurrentWorkload());
            totalScore += workloadScore * 0.20;
            
            // 3. Rating (5% weight)
            double ratingScore = calculateRatingScore(tech.getRating());
            totalScore += ratingScore * 0.05;
            
            // Emergency bonus
            if ((analysis.getPriority().equals("CRITICAL") || analysis.getPriority().equals("HIGH")) &&
                tech.getSkills() != null && tech.getSkills().contains(Skill.EMERGENCY)) {
                totalScore += 0.10;
            }
            
            return Math.min(totalScore, 1.0);
        } catch (Exception e) {
            System.err.println("⚠️ Error calculating score: " + e.getMessage());
            return 0.5; // Safe default
        }
    }

    /**
     * Skill match calculation
     */
    private double calculateSkillMatch(Technician tech, Set<Skill> requiredSkills) {
        try {
            if (requiredSkills == null || requiredSkills.isEmpty()) {
                return 0.5;
            }
            
            if (tech.getSkills() == null || tech.getSkills().isEmpty()) {
                return 0.0;
            }
            
            Set<Skill> techSkills = tech.getSkills();
            int matches = 0;
            
            for (Skill required : requiredSkills) {
                if (techSkills.contains(required)) {
                    matches++;
                }
            }
            
            if (matches == 0) {
                return techSkills.contains(Skill.GENERAL) ? 0.3 : 0.0;
            }
            
            double score = (double) matches / requiredSkills.size();
            
            // Perfect match bonus
            if (matches == requiredSkills.size()) {
                return 1.0;
            }
            
            // Good match
            if (score >= 0.5) {
                return Math.max(score, 0.7);
            }
            
            return score;
            
        } catch (Exception e) {
            System.err.println("⚠️ Error in skill match: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Workload score
     */
    private double calculateWorkloadScore(Integer workload) {
        if (workload == null) return 1.0;
        if (workload == 0) return 1.0;
        if (workload <= 2) return 0.8;
        if (workload <= 4) return 0.6;
        return 0.3;
    }

    /**
     * Rating score
     */
    private double calculateRatingScore(Double rating) {
        if (rating == null) return 0.5;
        if (rating <= 0) return 0.1;
        return Math.min(rating / 5.0, 1.0);
    }

    /**
     * Safe technician fallback
     */
    private Technician safeTechnicianFallback(List<Technician> technicians) {
        if (technicians == null || technicians.isEmpty()) return null;
        
        try {
            // Find first available technician
            for (Technician tech : technicians) {
                if (tech != null && tech.isAvailable()) {
                    return tech;
                }
            }
            // If none available, return first technician
            return technicians.get(0);
        } catch (Exception e) {
            System.err.println("⚠️ Error in safe fallback: " + e.getMessage());
            return null;
        }
    }

    /**
     * Inner class for AI analysis
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