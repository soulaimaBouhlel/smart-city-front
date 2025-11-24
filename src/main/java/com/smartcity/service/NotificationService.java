package com.smartcity.service;

import com.smartcity.model.Notification;
import com.smartcity.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    public void notifyCitizen(String citizenId, String message) {
        createAndSaveNotification(citizenId, Notification.UserRole.CITIZEN, Notification.NotificationType.INFO, message,
                null);
    }

    public void notifyTechnician(String technicianId, String message) {
        createAndSaveNotification(technicianId, Notification.UserRole.TECHNICIAN, Notification.NotificationType.INFO,
                message, null);
    }

    public void sendAlert(String interventionId, String equipmentId, Notification.NotificationType type,
            String message) {
        // In a real scenario, we might need to look up who to alert based on
        // intervention or equipment.
        // For this implementation, we'll assume a generic system alert or broadcast,
        // but since the entity requires a userId, we might need to clarify or use a
        // placeholder.
        // However, the prompt says "sendAlert(interventionId, equipmentId, type,
        // message)".
        // And "Store notifications".
        // Let's assume for now we might notify a specific role or just log it,
        // but to fit the entity structure which has userId, let's assume we are
        // alerting a technician or admin.
        // For the purpose of this task, I will store it with a system user ID or
        // similar if not provided,
        // OR, maybe this method is intended to find the relevant users.

        // Let's assume we are notifying a technician associated with the intervention
        // (mock logic).
        // For simplicity in this step, I'll use a placeholder "SYSTEM_ADMIN" or similar
        // if no user is passed.
        // But wait, the prompt implies this is a specific responsibility.

        // Let's create a notification with metadata.
        Map<String, Object> metadata = Map.of(
                "interventionId", interventionId != null ? interventionId : "",
                "equipmentId", equipmentId != null ? equipmentId : "");

        // We'll assign it to a default "DISPATCHER" for now as the prompt doesn't
        // specify *who* gets the alert in this method signature.
        // Or maybe it's meant to be broadcast.
        // Let's use a placeholder ID.
        createAndSaveNotification("DISPATCHER", Notification.UserRole.TECHNICIAN, type, message, metadata);
    }

    public void sendNotification(String userId, Notification.UserRole role, Notification.NotificationType type,
            String message) {
        createAndSaveNotification(userId, role, type, message, null);
    }

    public List<Notification> getNotificationsForUser(String userId) {
        return notificationRepository.findByUserId(userId);
    }

    public void markAsRead(String notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    private void createAndSaveNotification(String userId, Notification.UserRole role,
            Notification.NotificationType type, String message, Map<String, Object> metadata) {
        Notification notification = Notification.builder()
                .userId(userId)
                .userRole(role)
                .type(type)
                .message(message)
                .timestamp(LocalDateTime.now())
                .read(false)
                .metadata(metadata)
                .build();
        notificationRepository.save(notification);
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, notification);
    }
}
