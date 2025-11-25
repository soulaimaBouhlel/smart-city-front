package com.smartcity.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;
    private String title;
    private String message;
    private NotificationType type;
    private String recipientId;
    private LocalDateTime timestamp;
    private Map<String, Object> metadata;

    public enum NotificationType {
        EMAIL, SMS, REALTIME, PUSH, SYSTEM
    }
}
