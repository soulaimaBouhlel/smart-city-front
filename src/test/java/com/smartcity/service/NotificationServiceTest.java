package com.smartcity.service;

import com.smartcity.model.Notification;
import com.smartcity.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void notifyCitizen_shouldSaveNotification() {
        String citizenId = "citizen1";
        String message = "Hello Citizen";

        notificationService.notifyCitizen(citizenId, message);

        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/notifications/" + citizenId),
                any(Notification.class));
    }

    @Test
    void notifyTechnician_shouldSaveNotification() {
        String technicianId = "tech1";
        String message = "Hello Technician";

        notificationService.notifyTechnician(technicianId, message);

        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/notifications/" + technicianId),
                any(Notification.class));
    }

    @Test
    void sendAlert_shouldSaveNotification() {
        String interventionId = "int1";
        String equipmentId = "eq1";
        String message = "Alert!";

        notificationService.sendAlert(interventionId, equipmentId, Notification.NotificationType.CRITICAL, message);

        verify(notificationRepository, times(1)).save(any(Notification.class));
        // In sendAlert we used a placeholder "DISPATCHER"
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/notifications/DISPATCHER"),
                any(Notification.class));
    }

    @Test
    void getNotificationsForUser_shouldReturnList() {
        String userId = "user1";
        when(notificationRepository.findByUserId(userId)).thenReturn(List.of(new Notification()));

        List<Notification> notifications = notificationService.getNotificationsForUser(userId);

        assertFalse(notifications.isEmpty());
        verify(notificationRepository, times(1)).findByUserId(userId);
    }

    @Test
    void markAsRead_shouldUpdateStatus() {
        String notificationId = "notif1";
        Notification notification = new Notification();
        notification.setRead(false);
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(notificationId);

        assertTrue(notification.isRead());
        verify(notificationRepository, times(1)).save(notification);
    }
}
