package com.smartcity.model;

import com.smartcity.model.EquipmentType;
import com.smartcity.model.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

// Maps to the Equipment class in the UML diagram
@Document(collection = "equipments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Equipment {

    // int equipmentId is the logical ID, but MongoDB uses String _id
    @Id
    private String id;

    private String name;

    // EquipmentType is an EEquipmentType in the UML diagram
    private EquipmentType type;

    // GeoJSON Point for geospatial queries
    @GeoSpatialIndexed
    private GeoJsonPoint location;

    private String address; // Optional address field

    // Status is an EStatus in the UML diagram
    private Status status;

    private Date lastMaintenance;

    // -------------------------------------------------------------------------
    // 🛑 CUSTOM BUILDER IMPLEMENTATION TO ALLOW STRING INPUT FROM CONTROLLERS
    // -------------------------------------------------------------------------

    public static class EquipmentBuilder {
        private String id;
        private String name;
        // Use the final enum types for the actual fields
        private EquipmentType type;
        private GeoJsonPoint location;
        private String address;
        private Status status;
        private Date lastMaintenance;

        // Custom builder method for 'type' to accept String input (e.g., "Street
        // Light")
        public EquipmentBuilder type(String type) {
            // Map the controller strings to the internal EquipmentType enum
            String normalizedType = switch (type.toLowerCase()) {
                case "street light", "lighting" -> "LIGHTING";
                case "traffic signal", "traffic" -> "TRAFFIC";
                case "sensor", "road" -> "ROAD";
                case "water pump station a", "water" -> "WATER";
                default -> "LIGHTING"; // Default to a safe type
            };
            this.type = EquipmentType.valueOf(normalizedType);
            return this;
        }

        // Custom builder method for 'status' to accept String input (e.g., "Active")
        public EquipmentBuilder status(String status) {
            // Map the controller strings to the internal Status enum
            String normalizedStatus = switch (status.toLowerCase()) {
                // "Active" is mapped to "COMPLETED" (no intervention needed)
                case "active", "completed" -> "COMPLETED";
                // "Maintenance" and "Faulty" are mapped to "PENDING" (needs intervention)
                case "maintenance", "faulty", "pending" -> "PENDING";
                default -> "PENDING";
            };
            this.status = Status.valueOf(normalizedStatus);
            return this;
        }

        // Must manually include the build method when defining custom methods
        public Equipment build() {
            return new Equipment(id, name, type, location, address, status, lastMaintenance);
        }
    }

    // -------------------------------------------------------------------------
    // 🛑 END CUSTOM BUILDER
    // -------------------------------------------------------------------------

    // Based on the UML: o_needsMaintenance()
    public boolean needsMaintenance() {
        // Simple logic: Needs maintenance if last maintenance was more than 30 days
        // ago.
        long thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000;
        if (this.lastMaintenance == null)
            return true; // Assume if null, it needs maintenance
        return (new Date().getTime() - this.lastMaintenance.getTime()) > thirtyDaysInMillis;
    }
}
