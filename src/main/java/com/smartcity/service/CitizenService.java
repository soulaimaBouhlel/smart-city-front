package com.smartcity.service;

import com.smartcity.model.*;
import com.smartcity.repository.CitizenRepository;
import com.smartcity.repository.InterventionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CitizenService {
    private final CitizenRepository citizenRepository;
    private final InterventionRepository interventionRepository;
    private final SmartSchedulerService smartSchedulerService;
    private final INotificationService notificationService;

    public Intervention submitIssue(String citizenId, String description, 
                                  String interventionType, Location location) {
        // 1. Get AI analysis
        GeminiAIService.InterventionAnalysis analysis = 
            smartSchedulerService.analyzeIntervention(description, interventionType);
        
        // 2. Create intervention with AI-determined priority
        Intervention intervention = Intervention.builder()
            .title("Citizen Report: " + interventionType)
            .description(description)
            .interventionType(interventionType)
            .location(location)
            .priorityScore(analysis.getUrgency())
            .priority(smartSchedulerService.mapPriority(analysis.getPriority()))
            .status(Intervention.Status.PENDING)
            .build();
        
        Intervention savedIntervention = interventionRepository.save(intervention);
        
        // 3. Auto-assign technician using AI
        Technician bestTechnician = smartSchedulerService.findBestTechnician(savedIntervention);
        if (bestTechnician != null) {
            bestTechnician.increaseWorkload();
            // technicianRepository.save(bestTechnician); // You'd need this injected
            
            savedIntervention.setStatus(Intervention.Status.IN_PROGRESS);
            interventionRepository.save(savedIntervention);
        }
        
        // 4. Send notification to citizen
        Notification notification = Notification.builder()
            .id(java.util.UUID.randomUUID().toString())
            .title("Issue Submitted Successfully")
            .message("Your " + analysis.getPriority() + " priority issue has been logged. " +
                    (bestTechnician != null ? "Technician assigned: " + bestTechnician.getName() : "Awaiting technician assignment"))
            .type(Notification.NotificationType.SYSTEM)
            .recipientId(citizenId)
            .timestamp(java.time.LocalDateTime.now())
            .build();
        notificationService.sendNotification(notification);
        
        return savedIntervention;
    }
}