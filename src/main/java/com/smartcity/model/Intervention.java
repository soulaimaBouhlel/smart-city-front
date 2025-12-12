package com.smartcity.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import java.time.LocalDateTime;

@Document(collection = "interventions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Intervention {
    
    @Id
    private String interventionId;
    
    private String title;
    private String description;
    private String interventionType;
    
    private String priority;
    
    @Builder.Default
    private String status = "PENDING";
    
    private String address;
    
    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();
    
    private LocalDateTime completedDate;
    private Integer priorityScore;
    private String photoUrl;
    private String equipmentId;
    
    @DBRef
    private Equipment equipment;
    
    private String assignedTechnicianId;

    // -----------------------------------------------------------------
    // SIMPLE BUILDER CUSTOMIZATION
    // -----------------------------------------------------------------
    
    public static class InterventionBuilder {
        // Builder's status field
        private String status = "PENDING";
        
        public InterventionBuilder priority(String priority) {
            this.priority = priority;
            return this;
        }
        
        public InterventionBuilder status(String status) {
            // Assign parameter to builder's field
            this.status = status;
            return this;
        }
    }
}