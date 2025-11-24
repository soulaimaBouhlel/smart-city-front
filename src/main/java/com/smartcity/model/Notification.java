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
    private String userId;
    private UserRole userRole;
    private NotificationType type;
    private String message;
    private LocalDateTime timestamp;
    private boolean read;
    private Map<String, Object> metadata;

    public enum UserRole {
        CITIZEN, TECHNICIAN
    }

    public enum NotificationType {
        INFO, WARNING, CRITICAL
    }
}
