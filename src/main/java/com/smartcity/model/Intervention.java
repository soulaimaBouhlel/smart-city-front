package com.smartcity.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Intervention entity representing city maintenance or repair interventions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "interventions")
public class Intervention {

    @Id
    private String interventionId;

    private String title;

    private String description;

    private String interventionType; // e.g., "Road Repair", "Equipment Maintenance", "Emergency"

    private Priority priority;

    @Builder.Default
    private Status status = Status.PENDING;

    private Location location;

    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();

    private LocalDateTime completedDate;

    private Integer priorityScore; // Calculated score for sorting

    private String photoUrl;

    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum Status {
        PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    }
}
