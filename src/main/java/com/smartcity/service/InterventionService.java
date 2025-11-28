package com.smartcity.service;

import com.smartcity.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InterventionService {

    private final INotificationService notificationService;
    private final SmartSchedulerService smartSchedulerService;

    public Intervention createIntervention(String citizenId, String description, String interventionType, Location location) {
        // Use SmartScheduler to calculate urgency
        int urgencyScore = smartSchedulerService.analyzeUrgency(description);
        
        // Create intervention with smart priority
        Intervention intervention = Intervention.builder()
                .title("Intervention - " + interventionType)
                .description(description)
                .interventionType(interventionType)
                .location(location)
                .priorityScore(urgencyScore)
                .priority(mapUrgencyToPriority(urgencyScore))
                .build();
        
        // Find best technician automatically
        List<Technician> availableTechs = smartSchedulerService.createMockTechnicians();
        String bestTech = smartSchedulerService.findBestTechnician(intervention, availableTechs);
        
        // Send notification with smart details
        Notification notification = Notification.builder()
                .id(java.util.UUID.randomUUID().toString())
                .title("Intervention Created")
                .message("Intervention created with urgency " + urgencyScore + "/10. Best technician: " + bestTech)
                .type(Notification.NotificationType.SYSTEM)
                .recipientId(citizenId)
                .timestamp(java.time.LocalDateTime.now())
                .build();
        notificationService.sendNotification(notification);
        
        return intervention;
    }

    private Intervention.Priority mapUrgencyToPriority(int urgency) {
        if (urgency >= 9) return Intervention.Priority.CRITICAL;
        if (urgency >= 7) return Intervention.Priority.HIGH;
        if (urgency >= 5) return Intervention.Priority.MEDIUM;
        return Intervention.Priority.LOW;
    }

    public void assignTechnician(String interventionId, String technicianId) {
        // Logic to assign technician...
        Notification notification = Notification.builder()
                .id(java.util.UUID.randomUUID().toString())
                .title("Technician Assigned")
                .message("You have been assigned to intervention: " + interventionId)
                .type(Notification.NotificationType.SYSTEM)
                .recipientId(technicianId)
                .timestamp(java.time.LocalDateTime.now())
                .build();
        notificationService.sendNotification(notification);
    }

    public void updateStatus(String interventionId, String status, String citizenId) {
        // Logic to update status...
        Notification notification = Notification.builder()
                .id(java.util.UUID.randomUUID().toString())
                .title("Intervention Status Update")
                .message("Intervention " + interventionId + " status changed to: " + status)
                .type(Notification.NotificationType.SYSTEM)
                .recipientId(citizenId)
                .timestamp(java.time.LocalDateTime.now())
                .build();
        notificationService.sendNotification(notification);
    }
}