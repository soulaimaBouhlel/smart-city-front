package com.smartcity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.smartcity.model.Intervention;
import com.smartcity.model.Technician;
import com.smartcity.model.Equipment;
import com.smartcity.repository.TechnicianRepository;
import com.smartcity.repository.InterventionRepository;
import com.smartcity.repository.EquipmentRepository;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

@Service
@Slf4j
public class SmartSchedulerService {
    
    @Autowired
    private TechnicianRepository technicianRepository;
    
    @Autowired
    private InterventionRepository interventionRepository;
    
    @Autowired
    private OpenAIService openAIService;
    
    @Autowired
    private EquipmentRepository equipmentRepository;
    
    @Autowired
    private MapService mapService;

    // ===========================================================
    // METHOD 1: analyzeIntervention() - From Class Diagram
    // ===========================================================
    
    public OpenAIService.InterventionAnalysis analyzeIntervention(
            String description, String interventionType, String equipmentType) {
        log.info("🤖 Analyzing intervention: {} | {} | {}", description, interventionType, equipmentType);
        return openAIService.analyzeIntervention(description, interventionType, equipmentType);
    }
    
    public OpenAIService.InterventionAnalysis analyzeIntervention(
            String description, String interventionType) {
        return analyzeIntervention(description, interventionType, "UNKNOWN");
    }

    // ===========================================================
    // METHOD 2: findBestTechnician() - From Class Diagram (FIXED)
    // ===========================================================
    
    public Technician findBestTechnician(Intervention intervention) {
        log.info("🔍 Finding best technician for intervention: {}", intervention.getTitle());
        
        // 1. Get intervention equipment location
        GeoJsonPoint equipmentLocation = getInterventionLocation(intervention);
        if (equipmentLocation == null) {
            log.error("❌ Intervention has no location data");
            return null;
        }
        
        // 2. Get nearby technicians within 50km using MapService (YOUR FRIEND'S METHOD)
        List<Technician> nearbyTechnicians = mapService.getNearestTechnicians(
            equipmentLocation.getX(),  // longitude
            equipmentLocation.getY(),  // latitude
            50.0  // 50km radius
        );
        
        log.info("📍 MapService found {} nearby technicians", nearbyTechnicians.size());
        
        // 3. Pass ONLY nearby technicians to AI for skill evaluation
        return openAIService.findBestTechnician(intervention, nearbyTechnicians);
    }
    
    // ===========================================================
    // METHOD 3: mapPriority() - From Class Diagram
    // ===========================================================
    
    public String mapPriority(String aiPriority) {
        if (aiPriority == null) return "MEDIUM";
        
        switch (aiPriority.toUpperCase()) {
            case "CRITICAL": return "CRITICAL";
            case "HIGH": return "HIGH";
            case "MEDIUM": return "MEDIUM";
            case "LOW": return "LOW";
            default: return "MEDIUM";
        }
    }
    
    // ===========================================================
    // HELPER METHODS
    // ===========================================================
    
    /**
     * Get equipment location from intervention
     */
    private GeoJsonPoint getInterventionLocation(Intervention intervention) {
        // First try to get from equipment reference
        if (intervention.getEquipment() != null && intervention.getEquipment().getLocation() != null) {
            return intervention.getEquipment().getLocation();
        }
        
        // If not, try to fetch equipment from DB
        if (intervention.getEquipmentId() != null) {
            Equipment equipment = equipmentRepository.findById(intervention.getEquipmentId()).orElse(null);
            if (equipment != null && equipment.getLocation() != null) {
                // Update the intervention with equipment reference
                intervention.setEquipment(equipment);
                return equipment.getLocation();
            }
        }
        
        return null;
    }
    
    /**
     * Assign technician to intervention (complete method)
     */
    public Map<String, Object> assignTechnicianToIntervention(String interventionId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. Get intervention
            Intervention intervention = interventionRepository.findById(interventionId)
                .orElseThrow(() -> new RuntimeException("Intervention not found: " + interventionId));
            
            // 2. Find best technician
            Technician bestTechnician = findBestTechnician(intervention);
            
            if (bestTechnician == null) {
                result.put("success", false);
                result.put("message", "No suitable technician found");
                return result;
            }
            
            // 3. Update intervention
            intervention.setAssignedTechnicianId(bestTechnician.getTechnicianId());
            intervention.setStatus("IN_PROGRESS");
            
            // 4. Get AI analysis for priority
            String equipmentType = "UNKNOWN";
            if (intervention.getEquipment() != null && intervention.getEquipment().getType() != null) {
                equipmentType = intervention.getEquipment().getType().toString();
            }
            
            OpenAIService.InterventionAnalysis analysis = analyzeIntervention(
                intervention.getDescription(),
                intervention.getInterventionType(),
                equipmentType
            );
            
            intervention.setPriority(mapPriority(analysis.getPriority()));
            intervention.setPriorityScore(analysis.getUrgency() * 10);
            
            // 5. Update technician workload
            bestTechnician.increaseWorkload();
            if (bestTechnician.getCurrentWorkload() >= 5) {
                bestTechnician.setAvailable(false);
            }
            
            // 6. Update equipment status
            if (intervention.getEquipmentId() != null) {
                equipmentRepository.findById(intervention.getEquipmentId())
                    .ifPresent(equipment -> {
                        equipment.setStatus("PENDING");
                        equipmentRepository.save(equipment);
                    });
            }
            
            // 7. Save all changes
            interventionRepository.save(intervention);
            technicianRepository.save(bestTechnician);
            
            // 8. Return success
            result.put("success", true);
            result.put("message", "Technician assigned successfully");
            result.put("technician", Map.of(
                "id", bestTechnician.getTechnicianId(),
                "name", bestTechnician.getName(),
                "skills", bestTechnician.getSkills()
            ));
            result.put("intervention", Map.of(
                "id", intervention.getInterventionId(),
                "title", intervention.getTitle(),
                "priority", intervention.getPriority()
            ));
            
        } catch (Exception e) {
            log.error("❌ Error assigning technician: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "Assignment failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Get intervention details with equipment info
     */
    public Map<String, Object> getInterventionDetails(String interventionId) {
        Map<String, Object> details = new HashMap<>();
        
        try {
            Intervention intervention = interventionRepository.findById(interventionId)
                .orElseThrow(() -> new RuntimeException("Intervention not found"));
            
            details.put("interventionId", intervention.getInterventionId());
            details.put("title", intervention.getTitle());
            details.put("status", intervention.getStatus());
            details.put("priority", intervention.getPriority());
            details.put("hasEquipmentId", intervention.getEquipmentId() != null);
            details.put("equipmentId", intervention.getEquipmentId());
            
            // Get equipment details if exists
            if (intervention.getEquipmentId() != null) {
                equipmentRepository.findById(intervention.getEquipmentId())
                    .ifPresent(equipment -> {
                        details.put("equipmentName", equipment.getName());
                        details.put("equipmentType", equipment.getType());
                        details.put("equipmentStatus", equipment.getStatus());
                        details.put("hasLocation", equipment.getLocation() != null);
                        
                        if (equipment.getLocation() != null) {
                            details.put("latitude", equipment.getLocation().getY());
                            details.put("longitude", equipment.getLocation().getX());
                        }
                    });
            }
            
            // Get technician details if assigned
            if (intervention.getAssignedTechnicianId() != null) {
                technicianRepository.findById(intervention.getAssignedTechnicianId())
                    .ifPresent(technician -> {
                        details.put("assignedTechnician", Map.of(
                            "name", technician.getName(),
                            "skills", technician.getSkills()
                        ));
                    });
            }
            
        } catch (Exception e) {
            details.put("error", e.getMessage());
        }
        
        return details;
    }
}