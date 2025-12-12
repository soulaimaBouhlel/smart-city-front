package com.smartcity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.smartcity.service.SmartSchedulerService;
import com.smartcity.service.OpenAIService;
import com.smartcity.repository.InterventionRepository;
import com.smartcity.repository.TechnicianRepository;
import com.smartcity.repository.EquipmentRepository;
import com.smartcity.model.*;
import java.util.*;

@RestController
@RequestMapping("/api/scheduler")
@CrossOrigin(origins = "*")
public class SchedulerController {
    
    @Autowired
    private SmartSchedulerService smartSchedulerService;
    
    @Autowired
    private OpenAIService openAIService;
    
    @Autowired
    private InterventionRepository interventionRepository;
    
    @Autowired
    private TechnicianRepository technicianRepository;
    
    @Autowired
    private EquipmentRepository equipmentRepository;

    // ==== VALIDATION ====
    
    private Map<String, Object> validateInterventionWithEquipment(String interventionId) {
        Map<String, Object> result = new HashMap<>();
        
        Optional<Intervention> interventionOpt = interventionRepository.findById(interventionId);
        if (interventionOpt.isEmpty()) {
            result.put("valid", false);
            result.put("error", "Intervention not found");
            return result;
        }
        
        Intervention intervention = interventionOpt.get();
        
        if (intervention.getEquipmentId() == null) {
            result.put("valid", false);
            result.put("error", "Intervention has no equipment reference");
            return result;
        }
        
        Optional<Equipment> equipmentOpt = equipmentRepository.findById(intervention.getEquipmentId());
        if (equipmentOpt.isEmpty()) {
            result.put("valid", false);
            result.put("error", "Referenced equipment not found");
            return result;
        }
        
        result.put("valid", true);
        result.put("intervention", intervention);
        result.put("equipment", equipmentOpt.get());
        return result;
    }

    // ==== MAIN ENDPOINTS ====
    
    @PostMapping("/assign-technician")
    public ResponseEntity<Map<String, Object>> assignTechnician(@RequestParam String interventionId) {
        try {
            Map<String, Object> validation = validateInterventionWithEquipment(interventionId);
            if (!(Boolean) validation.get("valid")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", validation.get("error"),
                    "interventionId", interventionId,
                    "assigned", false
                ));
            }
            
            Intervention intervention = (Intervention) validation.get("intervention");
            Equipment equipment = (Equipment) validation.get("equipment");
            
            Technician bestTechnician = smartSchedulerService.findBestTechnician(intervention);
            
            if (bestTechnician == null) {
                return ResponseEntity.ok(Map.of(
                    "message", "No available technicians found",
                    "interventionId", interventionId,
                    "assigned", false
                ));
            }
            
            // Update technician
            bestTechnician.increaseWorkload();
            technicianRepository.save(bestTechnician);
            
            // Update intervention
            intervention.setStatus("IN_PROGRESS");
            intervention.setAssignedTechnicianId(bestTechnician.getTechnicianId());
            
            // Get AI analysis for priority
            String equipmentType = equipment.getType() != null ? equipment.getType().toString() : "UNKNOWN";
            OpenAIService.InterventionAnalysis analysis = openAIService.analyzeIntervention(
                intervention.getDescription(), 
                intervention.getInterventionType(),
                equipmentType
            );
            
            intervention.setPriority(smartSchedulerService.mapPriority(analysis.getPriority()));
            intervention.setPriorityScore(analysis.getUrgency() * 10);
            interventionRepository.save(intervention);
            
            // Update equipment
            equipment.setStatus("PENDING");
            equipmentRepository.save(equipment);
            
            return ResponseEntity.ok(Map.of(
                "message", "Technician assigned successfully",
                "interventionId", interventionId,
                "equipmentId", equipment.getId(),
                "equipmentName", equipment.getName(),
                "technicianId", bestTechnician.getTechnicianId(),
                "technicianName", bestTechnician.getName(),
                "assigned", true
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to assign technician",
                "details", e.getMessage()
            ));
        }
    }

    @PostMapping("/ai-process")
    public ResponseEntity<Map<String, Object>> aiProcessIntervention(@RequestParam String interventionId) {
        try {
            Map<String, Object> validation = validateInterventionWithEquipment(interventionId);
            if (!(Boolean) validation.get("valid")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", validation.get("error"),
                    "interventionId", interventionId,
                    "processed", false
                ));
            }
            
            Intervention intervention = (Intervention) validation.get("intervention");
            Equipment equipment = (Equipment) validation.get("equipment");
            
            String equipmentType = equipment.getType() != null ? equipment.getType().toString() : "UNKNOWN";
            OpenAIService.InterventionAnalysis analysis = 
                openAIService.analyzeIntervention(
                    intervention.getDescription(), 
                    intervention.getInterventionType(),
                    equipmentType
                );
            
            Technician bestTechnician = smartSchedulerService.findBestTechnician(intervention);
            
            Map<String, Object> result = new HashMap<>();
            result.put("interventionId", interventionId);
            result.put("interventionTitle", intervention.getTitle());
            result.put("equipmentId", equipment.getId());
            result.put("equipmentName", equipment.getName());
            result.put("equipmentType", equipment.getType());
            result.put("aiPriority", analysis.getPriority());
            result.put("aiUrgencyScore", analysis.getUrgency());
            result.put("aiRecommendedSkills", analysis.getSkills());
            
            if (bestTechnician != null) {
                result.put("assigned", true);
                result.put("technicianId", bestTechnician.getTechnicianId());
                result.put("technicianName", bestTechnician.getName());
                result.put("technicianSkills", bestTechnician.getSkills());
                result.put("message", "AI successfully processed and assigned technician");
            } else {
                result.put("assigned", false);
                result.put("message", "AI processed but no technicians available");
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "AI processing failed",
                "details", e.getMessage()
            ));
        }
    }

    @GetMapping("/analyze-intervention")
    public ResponseEntity<Map<String, Object>> analyzeIntervention(@RequestParam String interventionId) {
        try {
            Map<String, Object> validation = validateInterventionWithEquipment(interventionId);
            if (!(Boolean) validation.get("valid")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", validation.get("error"),
                    "interventionId", interventionId
                ));
            }
            
            Intervention intervention = (Intervention) validation.get("intervention");
            Equipment equipment = (Equipment) validation.get("equipment");
            
            String equipmentType = equipment.getType() != null ? equipment.getType().toString() : "UNKNOWN";
            OpenAIService.InterventionAnalysis analysis = 
                openAIService.analyzeIntervention(
                    intervention.getDescription(), 
                    intervention.getInterventionType(),
                    equipmentType
                );
            
            Map<String, Object> response = new HashMap<>();
            response.put("interventionId", interventionId);
            response.put("title", intervention.getTitle());
            response.put("description", intervention.getDescription());
            response.put("type", intervention.getInterventionType());
            response.put("equipment", Map.of(
                "id", equipment.getId(),
                "name", equipment.getName(),
                "type", equipment.getType()
            ));
            response.put("aiPriority", analysis.getPriority());
            response.put("aiUrgencyScore", analysis.getUrgency());
            response.put("aiRecommendedSkills", analysis.getSkills());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to analyze intervention",
                "details", e.getMessage()
            ));
        }
    }



    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingInterventions() {
        try {
            // Use String status "PENDING" instead of enum
            List<Intervention> pendingInterventions = interventionRepository.findByStatus("PENDING");
            
            List<Map<String, Object>> pendingList = new ArrayList<>();
            for (Intervention intervention : pendingInterventions) {
                Map<String, Object> interventionMap = new HashMap<>();
                interventionMap.put("id", intervention.getInterventionId());
                interventionMap.put("title", intervention.getTitle());
                interventionMap.put("description", intervention.getDescription());
                interventionMap.put("type", intervention.getInterventionType());
                interventionMap.put("priority", intervention.getPriority());
                interventionMap.put("equipmentId", intervention.getEquipmentId());
                interventionMap.put("createdDate", intervention.getCreatedDate());
                
                pendingList.add(interventionMap);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", pendingInterventions.size(),
                "pendingInterventions", pendingList
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to get pending interventions",
                "details", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Smart City Scheduler");
        
        try {
            long interventionCount = interventionRepository.count();
            long technicianCount = technicianRepository.count();
            long equipmentCount = equipmentRepository.count();
            
            health.put("database", "Connected");
            health.put("counts", Map.of(
                "interventions", interventionCount,
                "technicians", technicianCount,
                "equipment", equipmentCount
            ));
        } catch (Exception e) {
            health.put("database", "Error: " + e.getMessage());
        }
        
        return ResponseEntity.ok(health);
    }
}