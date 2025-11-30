package com.smartcity.model;

import com.smartcity.model.EquipmentType;
import com.smartcity.model.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

// Maps to the Equipment class in the UML diagram
@Document(collection = "equipment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Equipment {

    // int equipmentId is the logical ID, but MongoDB uses String _id
    @Id
    private String id;
    
    private String name;
    
    // EquipmentType is an EEquipmentType in the UML diagram
    private EquipmentType type; 
    
    // Location is embedded in the document
    private Location location;
    
    // Status is an EStatus in the UML diagram
    private Status status;
    
    private Date lastMaintenance;

    // Based on the UML: o_needsMaintenance()
    public boolean needsMaintenance() {
        // Simple logic: Needs maintenance if last maintenance was more than 30 days ago.
        long thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000;
        return (new Date().getTime() - this.lastMaintenance.getTime()) > thirtyDaysInMillis;
    }
}