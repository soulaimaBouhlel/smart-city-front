package com.smartcity.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(collection = "equipments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Equipment {
    @Id
    private String id;
    private String name;
    private EquipmentType type;
    
    @GeoSpatialIndexed
    private GeoJsonPoint location;
    
    private Object address; // Can be String or Map
    private String status; // Store as String in DB
    private Date lastMaintenance;

    // ONE simple fix: add String setter
    public void setStatus(String status) {
        this.status = status;
    }
    
    // Keep existing enum setter for compatibility
    public void setStatus(Status status) {
        this.status = status != null ? status.toString() : null;
    }
    
    // Simple helper if needed
    public boolean needsMaintenance() {
        if (this.lastMaintenance == null) return true;
        long thirtyDays = 30L * 24 * 60 * 60 * 1000;
        return (new Date().getTime() - this.lastMaintenance.getTime()) > thirtyDays;
    }
}