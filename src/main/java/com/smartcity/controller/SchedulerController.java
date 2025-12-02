package com.smartcity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.smartcity.service.SmartSchedulerService;
import com.smartcity.service.GeminiAIService;
import com.smartcity.repository.InterventionRepository;
import com.smartcity.repository.TechnicianRepository;
import com.smartcity.model.*;
import java.util.*;

@RestController
@RequestMapping("/api/scheduler")
public class SchedulerController {
    
    @Autowired
    private SmartSchedulerService smartSchedulerService;
    
    @Autowired
    private GeminiAIService geminiAIService;
    
    @Autowired
    private InterventionRepository interventionRepository;
    
    @Autowired
    private TechnicianRepository technicianRepository;

    /**
     * Assign technician to REAL intervention (uses AI for matching)
     * PRODUCTION ENDPOINT - uses real database only
     */
    @PostMapping("/assign-technician")
    public ResponseEntity<Map<String, Object>> assignTechnician(@RequestParam String interventionId) {
        try {
            Optional<Intervention> interventionOpt = interventionRepository.findById(interventionId);
            
            if (interventionOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Intervention not found",
                    "interventionId", interventionId
                ));
            }
            
            Intervention intervention = interventionOpt.get();
            System.out.println("Processing Intervention: " + intervention.getTitle());
            
            // Use AI to find best technician from REAL database
            Technician bestTechnician = smartSchedulerService.findBestTechnician(intervention);
            
            if (bestTechnician == null) {
                return ResponseEntity.ok(Map.of(
                    "message", "No available technicians found",
                    "interventionId", interventionId,
                    "assigned", false
                ));
            }
            
            // Update REAL technician workload in database
            bestTechnician.increaseWorkload();
            technicianRepository.save(bestTechnician);
            
            // Update REAL intervention status in database
            intervention.setStatus(Intervention.Status.IN_PROGRESS);
            interventionRepository.save(intervention);
            
            return ResponseEntity.ok(Map.of(
                "message", "Technician assigned successfully",
                "interventionId", interventionId,
                "technicianId", bestTechnician.getTechnicianId(),
                "technicianName", bestTechnician.getName(),
                "technicianSkills", bestTechnician.getSkills(),
                "assigned", true
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to assign technician",
                "details", e.getMessage()
            ));
        }
    }

    /**
     * AI processes REAL intervention with full analysis and assignment
     * PRODUCTION ENDPOINT - uses real database only
     */
    @PostMapping("/ai-process-real-intervention")
    public ResponseEntity<Map<String, Object>> aiProcessRealIntervention(@RequestParam String interventionId) {
        try {
            // 1. GET REAL INTERVENTION FROM DATABASE
            Optional<Intervention> interventionOpt = interventionRepository.findById(interventionId);
            
            if (interventionOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Intervention not found in database",
                    "interventionId", interventionId
                ));
            }
            
            Intervention intervention = interventionOpt.get();
            System.out.println("AI Processing Intervention: " + intervention.getTitle());
            
            // 2. GET COMPLETE AI ANALYSIS (priority, urgency, skills)
            GeminiAIService.InterventionAnalysis analysis = 
                smartSchedulerService.analyzeIntervention(
                    intervention.getDescription(), 
                    intervention.getInterventionType()
                );
            
            // 3. AI FINDS BEST TECHNICIAN FROM REAL DATABASE
            Technician bestTechnician = smartSchedulerService.findBestTechnician(intervention);
            
            // Use HashMap for production safety
            Map<String, Object> result = new HashMap<>();
            result.put("interventionId", interventionId);
            result.put("realDescription", intervention.getDescription());
            result.put("aiPriority", analysis.getPriority());
            result.put("aiUrgencyScore", analysis.getUrgency());
            result.put("aiRecommendedSkills", analysis.getSkills());
            
            if (bestTechnician != null) {
                // 4. UPDATE REAL DATABASE
                bestTechnician.increaseWorkload();
                technicianRepository.save(bestTechnician);
                
                intervention.setStatus(Intervention.Status.IN_PROGRESS);
                intervention.setPriority(smartSchedulerService.mapPriority(analysis.getPriority()));
                intervention.setPriorityScore(analysis.getUrgency());
                interventionRepository.save(intervention);
                
                result.put("assigned", true);
                result.put("technicianId", bestTechnician.getTechnicianId());
                result.put("technicianName", bestTechnician.getName());
                result.put("technicianSkills", bestTechnician.getSkills());
                result.put("technicianWorkload", bestTechnician.getCurrentWorkload());
                result.put("message", "AI successfully processed intervention and assigned technician");
            } else {
                result.put("assigned", false);
                result.put("message", "AI processed intervention but no technicians available");
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "AI processing failed",
                "details", e.getMessage()
            ));
        }
    }

    /**
     * Get complete AI analysis for intervention
     * PRODUCTION ENDPOINT - uses real database only
     */
    @GetMapping("/analyze-intervention")
    public ResponseEntity<Map<String, Object>> analyzeIntervention(@RequestParam String interventionId) {
        try {
            // Get REAL intervention from database
            Optional<Intervention> interventionOpt = interventionRepository.findById(interventionId);
            
            if (interventionOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Intervention not found",
                    "interventionId", interventionId
                ));
            }
            
            Intervention intervention = interventionOpt.get();
            
            // Get complete AI analysis
            GeminiAIService.InterventionAnalysis analysis = 
                smartSchedulerService.analyzeIntervention(
                    intervention.getDescription(), 
                    intervention.getInterventionType()
                );
            
            // Use HashMap for production safety
            Map<String, Object> response = new HashMap<>();
            response.put("interventionId", interventionId);
            response.put("title", intervention.getTitle());
            response.put("description", intervention.getDescription());
            response.put("type", intervention.getInterventionType());
            response.put("aiPriority", analysis.getPriority());
            response.put("aiUrgencyScore", analysis.getUrgency());
            response.put("aiRecommendedSkills", analysis.getSkills());
            response.put("mappedPriority", smartSchedulerService.mapPriority(analysis.getPriority()));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to analyze intervention",
                "details", e.getMessage()
            ));
        }
    }

    /**
     * View REAL data in database (for debugging only - can be removed in production)
     */
    @GetMapping("/real-data")
    public ResponseEntity<Map<String, Object>> viewRealData() {
        try {
            // Get REAL interventions from database
            List<Intervention> interventions = interventionRepository.findAll();
            List<Technician> technicians = technicianRepository.findAll();
            
            // Show what we have to work with - use HashMap for safety
            List<Map<String, Object>> interventionList = new ArrayList<>();
            for (Intervention intervention : interventions) {
                Map<String, Object> interventionMap = new HashMap<>();
                interventionMap.put("id", intervention.getInterventionId());
                interventionMap.put("title", intervention.getTitle() != null ? intervention.getTitle() : "No Title");
                interventionMap.put("description", intervention.getDescription() != null ? intervention.getDescription() : "No Description");
                interventionMap.put("type", intervention.getInterventionType() != null ? intervention.getInterventionType() : "UNKNOWN");
                interventionMap.put("status", intervention.getStatus() != null ? intervention.getStatus() : "UNKNOWN");
                interventionMap.put("priority", intervention.getPriority() != null ? intervention.getPriority() : "UNKNOWN");
                interventionList.add(interventionMap);
            }
            
            List<Map<String, Object>> technicianList = new ArrayList<>();
            for (Technician tech : technicians) {
                Map<String, Object> technicianMap = new HashMap<>();
                technicianMap.put("id", tech.getTechnicianId());
                technicianMap.put("name", tech.getName() != null ? tech.getName() : "Unknown");
                technicianMap.put("skills", tech.getSkills() != null ? tech.getSkills() : Collections.emptySet());
                technicianMap.put("available", tech.isAvailable());
                technicianMap.put("workload", tech.getCurrentWorkload() != null ? tech.getCurrentWorkload() : 0);
                technicianList.add(technicianMap);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("interventions", interventionList);
            response.put("technicians", technicianList);
            response.put("total_interventions", interventions.size());
            response.put("total_technicians", technicians.size());
            response.put("available_technicians", technicians.stream().filter(Technician::isAvailable).count());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to get real data",
                "details", e.getMessage()
            ));
        }
    }
}