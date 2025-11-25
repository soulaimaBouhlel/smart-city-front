package com.smartcity.service;

import com.smartcity.model.Notification;
import com.smartcity.service.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InterventionService {

    private final INotificationService notificationService;

    public void createIntervention(String citizenId, String description) {
        // Logic to create intervention...
        Notification notification = Notification.builder()
                .id(java.util.UUID.randomUUID().toString())
                .title("Intervention Created")
                .message("Intervention created: " + description)
                .type(Notification.NotificationType.SYSTEM)
                .recipientId(citizenId)
                .timestamp(java.time.LocalDateTime.now())
                .build();
        notificationService.sendNotification(notification);
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
