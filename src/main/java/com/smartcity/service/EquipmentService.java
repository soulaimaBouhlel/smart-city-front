package com.smartcity.service;

import com.smartcity.model.Notification;
import com.smartcity.service.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final INotificationService notificationService;

    public void detectMaintenanceNeeded(String equipmentId, String reason) {
        // Logic to detect maintenance...
        Notification notification = Notification.builder()
                .id(java.util.UUID.randomUUID().toString())
                .title("Maintenance Alert")
                .message("Maintenance needed for equipment " + equipmentId + ": " + reason)
                .type(Notification.NotificationType.SYSTEM)
                .recipientId("SYSTEM_ADMIN") // Placeholder
                .timestamp(java.time.LocalDateTime.now())
                .build();
        notificationService.sendNotification(notification);
    }
}
