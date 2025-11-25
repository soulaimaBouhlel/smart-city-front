package com.smartcity.service;

import com.smartcity.model.Notification;
import com.smartcity.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicNotificationService implements INotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendNotification(Notification notification) {
        log.info("Processing notification: [ID={}, Type={}, Recipient={}]",
                notification.getId(), notification.getType(), notification.getRecipientId());

        // Save to database
        notificationRepository.save(notification);
        log.info("Notification saved to database: {}", notification.getId());

        // Send via WebSocket
        messagingTemplate.convertAndSend("/topic/notifications/" + notification.getRecipientId(), notification);
        log.info("WebSocket notification sent to user: {}", notification.getRecipientId());

        log.info("Notification processed successfully: {}", notification.getId());
    }
}
