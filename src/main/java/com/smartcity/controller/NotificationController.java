package com.smartcity.controller;

import com.smartcity.model.Notification;
import com.smartcity.repository.NotificationRepository;
import com.smartcity.service.BasicNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final BasicNotificationService basicNotificationService;

    @GetMapping("/{recipientId}")
    public ResponseEntity<java.util.List<Notification>> getNotifications(@PathVariable String recipientId) {
        return ResponseEntity.ok(notificationRepository.findByRecipientId(recipientId));
    }

    @PostMapping("/send")
    public ResponseEntity<Void> sendNotification(@RequestBody NotificationRequest request) {
        Notification notification = Notification.builder()
                .id(UUID.randomUUID().toString())
                .title("Notification") // Default title, maybe add to request
                .message(request.getMessage())
                .type(request.getType())
                .recipientId(request.getRecipientId())
                .timestamp(LocalDateTime.now())
                .build();

        basicNotificationService.sendNotification(notification);

        return ResponseEntity.ok().build();
    }

    @lombok.Data
    public static class NotificationRequest {
        private String recipientId;
        private Notification.NotificationType type;
        private String message;
    }
}
