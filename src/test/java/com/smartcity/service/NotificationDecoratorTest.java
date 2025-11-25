package com.smartcity.service;

import com.smartcity.model.Notification;
import com.smartcity.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationDecoratorTest {

        @Mock
        private NotificationRepository notificationRepository;

        @Mock
        private SimpMessagingTemplate messagingTemplate;

        @InjectMocks
        private BasicNotificationService notificationService;

        @Test
        void testNotificationService() {
                Notification notification = Notification.builder()
                                .id(UUID.randomUUID().toString())
                                .title("Test Notification")
                                .message("Hello World")
                                .type(Notification.NotificationType.REALTIME)
                                .recipientId("user1")
                                .timestamp(LocalDateTime.now())
                                .build();

                notificationService.sendNotification(notification);

                // Verify database save
                verify(notificationRepository).save(any(Notification.class));

                // Verify WebSocket message sent
                verify(messagingTemplate).convertAndSend(eq("/topic/notifications/user1"), any(Notification.class));
        }
}
