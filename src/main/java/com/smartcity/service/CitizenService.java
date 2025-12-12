package com.smartcity.service;

import com.smartcity.model.*;
import com.smartcity.repository.CitizenRepository;
import com.smartcity.repository.InterventionRepository;
import com.smartcity.repository.EquipmentRepository;
import com.smartcity.repository.TechnicianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CitizenService {
    private final CitizenRepository citizenRepository;
    private final InterventionRepository interventionRepository;
    private final EquipmentRepository equipmentRepository;
    private final TechnicianRepository technicianRepository;
    private final SmartSchedulerService smartSchedulerService;
    private final INotificationService notificationService;

    /**
     * Submit issue for SPECIFIC EQUIPMENT
     */
    public Intervention submitIssueForEquipment(String citizenId, String equipmentId, 
            String description, String interventionType) {
        
        // 1. Validate citizen exists
        Citizen citizen = citizenRepository.findById(citizenId)
            .orElseThrow(() -> new RuntimeException("Citizen not found: " + citizenId));
        
        // 2. Validate equipment exists
        Equipment equipment = equipmentRepository.findById(equipmentId)
            .orElseThrow(() -> new RuntimeException("Equipment not found: " + equipmentId));
        
        // 3. Get AI analysis with equipment context
        OpenAIService.InterventionAnalysis analysis = smartSchedulerService.analyzeIntervention(
            description, interventionType, 
            equipment.getType() != null ? equipment.getType().toString() : "UNKNOWN");
        
        // 4. Create intervention linked to equipment
        Intervention intervention = Intervention.builder()
                .title("Citizen Report: " + interventionType)
                .description(description)
                .interventionType(interventionType)
                .equipmentId(equipmentId)
                .equipment(equipment)
                .priorityScore(analysis.getUrgency() * 10)
                .priority(smartSchedulerService.mapPriority(analysis.getPriority()))
                .status("PENDING") // Use String, not enum
                .build();

        Intervention savedIntervention = interventionRepository.save(intervention);
        
        // 5. Update equipment status
        equipment.setStatus("PENDING"); // Use String
        equipmentRepository.save(equipment);
        
        // 6. Try to assign technician
        Technician bestTechnician = smartSchedulerService.findBestTechnician(savedIntervention);
        if (bestTechnician != null) {
            savedIntervention.setAssignedTechnicianId(bestTechnician.getTechnicianId());
            savedIntervention.setStatus("IN_PROGRESS"); // Use String
            
            bestTechnician.increaseWorkload();
            if (bestTechnician.getCurrentWorkload() >= 5) {
                bestTechnician.setAvailable(false);
            }
            
            interventionRepository.save(savedIntervention);
            technicianRepository.save(bestTechnician);
        }
        
        // 7. Send notification
        Notification notification = Notification.builder()
                .id(UUID.randomUUID().toString())
                .title("Issue Submitted Successfully")
                .message("Your " + analysis.getPriority() + " priority issue for " + 
                        equipment.getName() + " has been logged. " +
                        (bestTechnician != null ? 
                            "Technician " + bestTechnician.getName() + " assigned." :
                            "Awaiting technician assignment."))
                .type(Notification.NotificationType.SYSTEM)
                .recipientId(citizenId)
                .timestamp(java.time.LocalDateTime.now())
                .build();
        notificationService.sendNotification(notification);

        return savedIntervention;
    }
}