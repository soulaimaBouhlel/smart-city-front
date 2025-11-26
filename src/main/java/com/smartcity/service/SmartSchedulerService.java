package com.smartcity.service;

import org.springframework.stereotype.Service;
import com.smartcity.model.*;
import java.util.*;

@Service
public class SmartSchedulerService {
    
    public int analyzeUrgency(String description) {
        System.out.println("🔍 Analyzing intervention urgency...");
        
        double urgencyScore = calculateUrgencyScore(description);
        int urgencyLevel = (int) (urgencyScore * 9) + 1; // Convert to 1-10 scale
        
        System.out.println("✅ Smart Analysis - Urgency Score: " + urgencyLevel + "/10");
        return urgencyLevel;
    }
    
    private double calculateUrgencyScore(String description) {
        if (description == null || description.trim().isEmpty()) return 0.3;
        
        String lowerDesc = description.toLowerCase();
        double score = 0.0;
        
        // CRITICAL EMERGENCIES (0.8-1.0 points)
        if (containsAny(lowerDesc, "fire", "explosion", "gas leak", "electrocution", "collapsed", "trapped")) {
            score += 0.9;
        }
        
        // HIGH RISK PUBLIC SAFETY (0.6-0.8 points)
        if (containsAny(lowerDesc, "accident", "bleeding", "unconscious", "evacuation", "flooding", "structural damage")) {
            score += 0.7;
        }
        
        // INFRASTRUCTURE FAILURES (0.5-0.7 points)
        if (containsAny(lowerDesc, "traffic light", "school", "hospital", "main street", "highway", "bridge")) {
            score += 0.6;
        }
        
        // URGENT PROBLEMS (0.4-0.6 points)
        if (containsAny(lowerDesc, "broken", "not working", "outage", "leaking", "burst", "damaged")) {
            score += 0.5;
        }
        
        // RISK TO VULNERABLE GROUPS (0.3-0.5 points)
        if (containsAny(lowerDesc, "children", "elderly", "disabled", "pedestrian", "crossing")) {
            score += 0.4;
        }
        
        // URGENCY KEYWORDS (0.2-0.4 points)
        if (containsAny(lowerDesc, "urgent", "emergency", "danger", "risk", "critical", "911")) {
            score += 0.3;
        }
        
        // TIME SENSITIVE (0.1-0.3 points)
        if (containsAny(lowerDesc, "immediately", "asap", "now", "right away")) {
            score += 0.2;
        }
        
        // EXCLAMATION/CAPS INTENSITY (0.1-0.2 points)
        long exclamationCount = lowerDesc.chars().filter(ch -> ch == '!').count();
        if (exclamationCount > 2) score += 0.2;
        else if (exclamationCount > 0) score += 0.1;
        
        // Multiple caps words indicate urgency
        String[] words = description.split(" ");
        long capsWords = Arrays.stream(words)
                .filter(word -> word.length() > 3 && word.equals(word.toUpperCase()))
                .count();
        if (capsWords > 1) score += 0.15;
        
        // COMBINATION BONUS - if multiple high-risk factors
        int highRiskFactors = 0;
        if (containsAny(lowerDesc, "fire", "explosion", "gas", "accident")) highRiskFactors++;
        if (containsAny(lowerDesc, "school", "hospital", "children")) highRiskFactors++;
        if (containsAny(lowerDesc, "trapped", "bleeding", "unconscious")) highRiskFactors++;
        
        if (highRiskFactors >= 2) score += 0.25;
        
        return Math.min(score, 1.0);
    }
    
    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }
    
    public String findBestTechnician(Intervention intervention, List<Technician> availableTechnicians) {
        System.out.println("🔍 Finding best technician for intervention: " + intervention.getTitle());
        
        if (availableTechnicians == null || availableTechnicians.isEmpty()) {
            return "No available technicians";
        }
        
        Technician bestTech = null;
        double bestScore = 0;
        Map<String, Double> scoreDetails = new HashMap<>();
        
        for (Technician tech : availableTechnicians) {
            if (tech.getAvailable()) {
                double score = calculateTechnicianMatchScore(tech, intervention, scoreDetails);
                
                if (score > bestScore) {
                    bestScore = score;
                    bestTech = tech;
                }
            }
        }
        
        if (bestTech != null) {
            return bestTech.getName() + " (Match Score: " + String.format("%.1f", bestScore) + ")";
        }
        
        return "No suitable technician available";
    }
    
    private double calculateTechnicianMatchScore(Technician tech, Intervention intervention, Map<String, Double> scoreDetails) {
        double totalScore = 0.0;
        
        // 1. Skill match (40% weight)
        Set<Skill> requiredSkills = getRequiredSkillsForIntervention(intervention);
        double skillScore = tech.calculateSkillMatch(requiredSkills);
        totalScore += skillScore * 0.4;
        
        // 2. Location proximity (30% weight) - simplified for now
        double locationScore = calculateLocationScore(tech, intervention);
        totalScore += locationScore * 0.3;
        
        // 3. Workload balance (20% weight)
        double workloadScore = calculateWorkloadScore(tech.getCurrentWorkload());
        totalScore += workloadScore * 0.2;
        
        // 4. Rating (10% weight)
        double ratingScore = calculateRatingScore(tech.getRating());
        totalScore += ratingScore * 0.1;
        
        // Store score breakdown for debugging
        scoreDetails.put(tech.getName() + "_skill", skillScore);
        scoreDetails.put(tech.getName() + "_location", locationScore);
        scoreDetails.put(tech.getName() + "_workload", workloadScore);
        scoreDetails.put(tech.getName() + "_rating", ratingScore);
        
        return totalScore;
    }
    
    private Set<Skill> getRequiredSkillsForIntervention(Intervention intervention) {
        String interventionType = intervention.getInterventionType();
        if (interventionType == null) return Set.of(Skill.GENERAL);
        
        switch (interventionType.toUpperCase()) {
            case "STREET_LIGHT":
            case "TRAFFIC_LIGHT":
                return Set.of(Skill.ELECTRICAL, Skill.HEIGHTS);
            case "WATER_LEAK":
                return Set.of(Skill.PLUMBING, Skill.EMERGENCY);
            case "ROAD_REPAIR":
                return Set.of(Skill.ROAD_WORK, Skill.CONSTRUCTION);
            case "WASTE_ISSUE":
                return Set.of(Skill.WASTE_MANAGEMENT, Skill.GENERAL);
            default:
                return Set.of(Skill.GENERAL);
        }
    }
    
    private double calculateLocationScore(Technician tech, Intervention intervention) {
        // Simplified location scoring - in real app, use actual distance calculation
        if (tech.getLocation() != null && intervention.getLocation() != null) {
            // Basic check - if both have locations, assume medium score
            return 0.7;
        }
        return 0.3; // Default score if location data missing
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
        return rating / 5.0; // Normalize to 0-1 scale
    }
    
    // Helper method to create mock technicians for testing
    public List<Technician> createMockTechnicians() {
        List<Technician> technicians = new ArrayList<>();
        
        // Mock technician 1 - Electrical specialist
        Technician tech1 = new Technician();
        tech1.setTechnicianId("tech1");
        tech1.setName("John Electric");
        tech1.setSkills(Set.of(Skill.ELECTRICAL, Skill.HEIGHTS, Skill.NETWORK));
        tech1.setAvailable(true);
        tech1.setRating(4.8);
        tech1.setCurrentWorkload(2);
        technicians.add(tech1);
        
        // Mock technician 2 - Plumbing specialist
        Technician tech2 = new Technician();
        tech2.setTechnicianId("tech2");
        tech2.setName("Mike Plumber");
        tech2.setSkills(Set.of(Skill.PLUMBING, Skill.EMERGENCY, Skill.GENERAL));
        tech2.setAvailable(true);
        tech2.setRating(4.6);
        tech2.setCurrentWorkload(1);
        technicians.add(tech2);
        
        // Mock technician 3 - Road work specialist
        Technician tech3 = new Technician();
        tech3.setTechnicianId("tech3");
        tech3.setName("Sarah Builder");
        tech3.setSkills(Set.of(Skill.ROAD_WORK, Skill.CONSTRUCTION, Skill.TRAFFIC_CONTROL));
        tech3.setAvailable(true);
        tech3.setRating(4.9);
        tech3.setCurrentWorkload(3);
        technicians.add(tech3);
        
        return technicians;
    }
}