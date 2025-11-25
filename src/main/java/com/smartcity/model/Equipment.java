package com.smartcity.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Equipment entity representing city infrastructure equipment.
 * Examples: Street lights, traffic signals, sensors, etc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "equipments")
public class Equipment {

    @Id
    private String id;

    private String name;

    private String type; // e.g., "Street Light", "Traffic Signal", "Sensor"

    private Location location;

    private Date lastMaintenance;

    @Builder.Default
    private String status = "Active"; // Active, Maintenance, Inactive, Faulty
}
