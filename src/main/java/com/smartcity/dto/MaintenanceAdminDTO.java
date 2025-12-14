package com.smartcity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaintenanceAdminDTO extends AdminDTO {
    
    private List<String> managedEquipmentTypes;
    
    @Min(value = 1, message = "Max assignments per day must be at least 1")
    private Integer maxAssignmentsPerDay = 20;
    
    private Boolean canConfigureScheduler = true;
    
    // Getters and Setters
    public List<String> getManagedEquipmentTypes() { 
        return managedEquipmentTypes; 
    }
    
    public void setManagedEquipmentTypes(List<String> managedEquipmentTypes) { 
        this.managedEquipmentTypes = managedEquipmentTypes; 
    }
    
    public Integer getMaxAssignmentsPerDay() { 
        return maxAssignmentsPerDay; 
    }
    
    public void setMaxAssignmentsPerDay(Integer maxAssignmentsPerDay) { 
        this.maxAssignmentsPerDay = maxAssignmentsPerDay; 
    }
    
    public Boolean getCanConfigureScheduler() { 
        return canConfigureScheduler; 
    }
    
    public void setCanConfigureScheduler(Boolean canConfigureScheduler) { 
        this.canConfigureScheduler = canConfigureScheduler; 
    }
}