package com.smartcity.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
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

    private String interventionType; 

    private Priority priority;

    @Builder.Default
    private Status status = Status.PENDING;

    // GeoJSON Point for geospatial queries
    @GeoSpatialIndexed
    private GeoJsonPoint location;
    
    private String address; // Optional address field

    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();

    private LocalDateTime completedDate;

    private Integer priorityScore; // Calculated score for sorting

    private String photoUrl;
    
    // Equipment and Technician references
    private String equipmentId;
    private String assignedTechnicianId;

    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum Status {
        PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    }

    // MANUALLY ADD ONLY THE GETTERS THAT WERE MISSING (no setters)
    public String getTitle() {
        return this.title;
    }

    public String getInterventionType() {
        return this.interventionType;
    }

    public GeoJsonPoint getLocation() {
        return this.location;
    }
}