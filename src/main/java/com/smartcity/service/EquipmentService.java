package com.smartcity.service;

import com.smartcity.model.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final NotificationService notificationService;

    public void detectMaintenanceNeeded(String equipmentId, String reason) {
        // Logic to detect maintenance...
        notificationService.sendAlert(null, equipmentId, Notification.NotificationType.WARNING, "Maintenance needed for equipment " + equipmentId + ": " + reason);
    }
}
