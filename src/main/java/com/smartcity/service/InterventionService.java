package com.smartcity.service;

import com.smartcity.model.*;
import com.smartcity.repository.TechnicianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InterventionService {

    private final INotificationService notificationService;
    private final SmartSchedulerService smartSchedulerService;
    private final TechnicianRepository technicianRepository;

    public Intervention createIntervention(String citizenId, String description, String interventionType,
            GeoJsonPoint location, String address) {
        // Get complete AI analysis
        GeminiAIService.InterventionAnalysis analysis = smartSchedulerService.analyzeIntervention(description,
                interventionType);

        // Create intervention with AI-determined priority
        Intervention intervention = Intervention.builder()
                .title("Intervention - " + interventionType)
                .description(description)
                .interventionType(interventionType)
                .location(location)
                .address(address)
                .priorityScore(analysis.getUrgency())
                .priority(smartSchedulerService.mapPriority(analysis.getPriority()))
                .status(Intervention.Status.PENDING)
                .build();

        // Find best technician using AI
        Technician bestTechnician = smartSchedulerService.findBestTechnician(intervention);
        String technicianInfo = bestTechnician != null ? bestTechnician.getName() : "No available technicians";

        // Send notification with AI analysis
        Notification notification = Notification.builder()
                .id(java.util.UUID.randomUUID().toString())
                .title("Intervention Created - " + analysis.getPriority())
                .message("AI analysis: " + analysis.getPriority() + " priority, " +
                        analysis.getUrgency() + "/10 urgency. Best match: " + technicianInfo)
                .type(Notification.NotificationType.SYSTEM)
                .recipientId(citizenId)
                .timestamp(java.time.LocalDateTime.now())
                .build();
        notificationService.sendNotification(notification);

        return intervention;
    }

    public void assignTechnician(String interventionId, String technicianId) {
        // Manual assignment logic would go here
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
        // Status update logic would go here
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