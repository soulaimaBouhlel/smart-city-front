package com.smartcity.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.*;

@Document(collection = "maintenance_admins")
public class MaintenanceAdmin extends Admin {
    private List<String> managedEquipmentTypes;
    private Boolean canConfigureScheduler;
    private Integer maxAssignmentsPerDay;

    // Add @Field annotations for inherited fields
    @Field("email")
    @Override
    public String getEmail() {
        return super.getEmail();
    }
    
    @Field("email")
    @Override
    public void setEmail(String email) {
        super.setEmail(email);
    }
    
    @Field("name")
    @Override
    public String getName() {
        return super.getName();
    }
    
    @Field("name")
    @Override
    public void setName(String name) {
        super.setName(name);
    }
    
    @Field("password")
    @Override
    public String getPassword() {
        return super.getPassword();
    }
    
    @Field("password")
    @Override
    public void setPassword(String password) {
        super.setPassword(password);
    }
    
    public MaintenanceAdmin() {
        super();
        this.setRole(UserRole.MAINTENANCE_ADMIN);
        this.canConfigureScheduler = true;
        this.maxAssignmentsPerDay = 20;
    }

    public MaintenanceAdmin(String name, String email, String phone, String password, 
                            String photoUrl, String department) {
        super(name, email, phone, password, photoUrl, UserRole.MAINTENANCE_ADMIN, department);
        this.canConfigureScheduler = true;
        this.maxAssignmentsPerDay = 20;
    }

    // ==================== ALIAS METHODS ====================
    public String getMaintenanceAdminId() {
        return getUserId();
    }

    public void setMaintenanceAdminId(String maintenanceAdminId) {
        setUserId(maintenanceAdminId);
    }
    // ======================================================

    // Getters and Setters
    public List<String> getManagedEquipmentTypes() { 
        return managedEquipmentTypes; 
    }
    
    public void setManagedEquipmentTypes(List<String> managedEquipmentTypes) { 
        this.managedEquipmentTypes = managedEquipmentTypes; 
    }

    public Boolean getCanConfigureScheduler() { 
        return canConfigureScheduler; 
    }
    
    public void setCanConfigureScheduler(Boolean canConfigureScheduler) { 
        this.canConfigureScheduler = canConfigureScheduler; 
    }

    public Integer getMaxAssignmentsPerDay() { 
        return maxAssignmentsPerDay; 
    }
    
    public void setMaxAssignmentsPerDay(Integer maxAssignmentsPerDay) { 
        this.maxAssignmentsPerDay = maxAssignmentsPerDay; 
    }

    // ==================== BUSINESS METHODS (LOGIC ONLY - NO DATABASE OPERATIONS) ====================
    
    /**
     * Business logic for equipment addition
     * Returns instructions/validation for the existing /api/equipment endpoint
     */
    public Map<String, Object> addEquipmentLogic(String name, EquipmentType type, 
                                                Double latitude, Double longitude, 
                                                String address, Status status) {
        System.out.println("MaintenanceAdmin " + this.getName() + " is initiating equipment addition: " + name);
        
        Map<String, Object> result = new HashMap<>();
        result.put("method", "addEquipment");
        result.put("adminId", this.getMaintenanceAdminId());
        result.put("adminName", this.getName());
        result.put("action", "CALL_EXISTING_API");
        result.put("endpoint", "POST /api/equipment");
        result.put("requiredPayload", Map.of(
            "name", name,
            "type", type.toString(),
            "latitude", latitude,
            "longitude", longitude,
            "address", address,
            "status", status.toString()
        ));
        result.put("permissionsRequired", Arrays.asList("MAINTENANCE_ADMIN"));
        result.put("timestamp", new Date());
        result.put("message", "Use existing endpoint: POST /api/equipment with the provided payload");
        
        logAction("INITIATE_ADD_EQUIPMENT", "Instructed to add equipment: " + name + " via existing API");
        
        return result;
    }
    
    /**
     * Business logic for equipment update
     */
    public Map<String, Object> updateEquipmentLogic(String equipmentId, String name, 
                                                   EquipmentType type, Double latitude, 
                                                   Double longitude, String address, 
                                                   Status status) {
        System.out.println("MaintenanceAdmin " + this.getName() + " is initiating equipment update for ID: " + equipmentId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("method", "updateEquipment");
        result.put("adminId", this.getMaintenanceAdminId());
        result.put("adminName", this.getName());
        result.put("action", "CALL_EXISTING_API");
        result.put("endpoint", "PUT /api/equipment/" + equipmentId);
        result.put("requiredPayload", Map.of(
            "name", name,
            "type", type.toString(),
            "latitude", latitude,
            "longitude", longitude,
            "address", address,
            "status", status.toString()
        ));
        result.put("permissionsRequired", Arrays.asList("MAINTENANCE_ADMIN"));
        result.put("timestamp", new Date());
        result.put("message", "Use existing endpoint: PUT /api/equipment/" + equipmentId + " with the provided payload");
        
        logAction("INITIATE_UPDATE_EQUIPMENT", "Instructed to update equipment ID: " + equipmentId + " via existing API");
        
        return result;
    }
    
    /**
     * Business logic for equipment removal
     */
    public Map<String, Object> removeEquipmentLogic(String equipmentId, String reason) {
        System.out.println("MaintenanceAdmin " + this.getName() + " is initiating equipment removal for ID: " + equipmentId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("method", "removeEquipment");
        result.put("adminId", this.getMaintenanceAdminId());
        result.put("adminName", this.getName());
        result.put("action", "CALL_EXISTING_API");
        result.put("endpoint", "DELETE /api/equipment/" + equipmentId);
        result.put("queryParams", Map.of("reason", reason));
        result.put("permissionsRequired", Arrays.asList("MAINTENANCE_ADMIN"));
        result.put("timestamp", new Date());
        result.put("message", "Use existing endpoint: DELETE /api/equipment/" + equipmentId + "?reason=" + reason);
        
        logAction("INITIATE_REMOVE_EQUIPMENT", "Instructed to remove equipment ID: " + equipmentId + " via existing API");
        
        return result;
    }
    
    /**
     * Business logic for intervention creation
     */
    public Map<String, Object> createInterventionLogic(String title, String description, 
                                                      String interventionType, String location, 
                                                      String photoUrl) {
        System.out.println("MaintenanceAdmin " + this.getName() + " is initiating intervention creation: " + title);
        
        Map<String, Object> result = new HashMap<>();
        result.put("method", "createIntervention");
        result.put("adminId", this.getMaintenanceAdminId());
        result.put("adminName", this.getName());
        result.put("action", "CALL_EXISTING_API");
        result.put("endpoint", "POST /api/interventions");
        result.put("requiredPayload", Map.of(
            "title", title,
            "description", description,
            "type", interventionType,
            "location", location,
            "photoUrl", photoUrl
        ));
        result.put("permissionsRequired", Arrays.asList("MAINTENANCE_ADMIN"));
        result.put("timestamp", new Date());
        result.put("message", "Use existing endpoint: POST /api/interventions with the provided payload");
        
        logAction("INITIATE_CREATE_INTERVENTION", "Instructed to create intervention: " + title + " via existing API");
        
        return result;
    }
    
    /**
     * Business logic for intervention update
     */
    public Map<String, Object> updateInterventionLogic(String interventionId, String title, 
                                                      String description, String status, 
                                                      String priority) {
        System.out.println("MaintenanceAdmin " + this.getName() + " is initiating intervention update for ID: " + interventionId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("method", "updateIntervention");
        result.put("adminId", this.getMaintenanceAdminId());
        result.put("adminName", this.getName());
        result.put("action", "CALL_EXISTING_API");
        result.put("endpoint", "PUT /api/interventions/" + interventionId);
        result.put("requiredPayload", Map.of(
            "title", title,
            "description", description,
            "status", status,
            "priority", priority
        ));
        result.put("permissionsRequired", Arrays.asList("MAINTENANCE_ADMIN"));
        result.put("timestamp", new Date());
        result.put("message", "Use existing endpoint: PUT /api/interventions/" + interventionId + " with the provided payload");
        
        logAction("INITIATE_UPDATE_INTERVENTION", "Instructed to update intervention ID: " + interventionId + " via existing API");
        
        return result;
    }
    
    /**
     * Business logic for technician assignment
     * This might require a new endpoint if not existing
     */
    public Map<String, Object> assignTechnicianLogic(String technicianId, String technicianName, 
                                                    String interventionId, String interventionTitle, 
                                                    Date assignmentDate) {
        System.out.println("MaintenanceAdmin " + this.getName() + " is assigning technician " + 
                          technicianName + " to intervention " + interventionTitle);
        
        // Check if assignment endpoint exists
        Map<String, Object> result = new HashMap<>();
        result.put("method", "assignTechnician");
        result.put("adminId", this.getMaintenanceAdminId());
        result.put("adminName", this.getName());
        result.put("technicianId", technicianId);
        result.put("technicianName", technicianName);
        result.put("interventionId", interventionId);
        result.put("interventionTitle", interventionTitle);
        result.put("assignmentDate", assignmentDate != null ? assignmentDate : new Date());
        result.put("assignedBy", this.getName());
        result.put("timestamp", new Date());
        result.put("message", "Technician assignment initiated");
        result.put("note", "This would typically call PATCH /api/interventions/" + interventionId + "/assign-technician");
        
        logAction("ASSIGN_TECHNICIAN", "Assigned technician " + technicianName + " to intervention " + interventionTitle);
        
        return result;
    }
    
    /**
     * Business logic for scheduler configuration
     */
    public Map<String, Object> configureSchedulerLogic(Integer newMaxAssignments, Boolean enableAutoAssignment, 
                                                      List<String> priorityRules, Map<String, Object> settings) {
        System.out.println("MaintenanceAdmin " + this.getName() + " is configuring scheduler");
        
        // Update admin's scheduler settings
        if (newMaxAssignments != null) {
            this.maxAssignmentsPerDay = newMaxAssignments;
        }
        if (enableAutoAssignment != null) {
            this.canConfigureScheduler = enableAutoAssignment;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("method", "configureScheduler");
        result.put("adminId", this.getMaintenanceAdminId());
        result.put("adminName", this.getName());
        result.put("maxAssignmentsPerDay", this.maxAssignmentsPerDay);
        result.put("canConfigureScheduler", this.canConfigureScheduler);
        result.put("managedEquipmentTypes", this.managedEquipmentTypes);
        result.put("newSettings", settings);
        result.put("priorityRules", priorityRules);
        result.put("timestamp", new Date());
        result.put("message", "Scheduler configuration updated in admin profile");
        result.put("note", "This updates the MaintenanceAdmin's configuration. Actual scheduler service would use these settings.");
        
        logAction("CONFIGURE_SCHEDULER", "Configured scheduler with max assignments: " + this.maxAssignmentsPerDay);
        
        return result;
    }
    
    /**
     * Test all business methods at once
     */
    public Map<String, Object> testAllMethods() {
        Map<String, Object> results = new HashMap<>();
        
        // Test each method
        results.put("addEquipment", 
            addEquipmentLogic("Test Street Light", EquipmentType.LIGHTING, 
                             34.0522, -118.2437, "123 Test St", Status.PENDING));
        
        results.put("updateEquipment", 
            updateEquipmentLogic("EQUIP_001", "Updated Street Light", EquipmentType.LIGHTING,
                                34.0522, -118.2437, "456 Test Ave", Status.COMPLETED));
        
        results.put("removeEquipment", 
            removeEquipmentLogic("EQUIP_002", "Equipment decommissioned"));
        
        results.put("createIntervention", 
            createInterventionLogic("Test Intervention", "This is a test intervention", 
                                   "URGENT", "Downtown Area", "https://example.com/photo.jpg"));
        
        results.put("updateIntervention", 
            updateInterventionLogic("INTERV_001", "Updated Intervention", 
                                   "Updated description", "IN_PROGRESS", "HIGH"));
        
        results.put("assignTechnician", 
            assignTechnicianLogic("TECH_001", "John Technician", 
                                 "INTERV_001", "Fix Street Light", new Date()));
        
        results.put("configureScheduler", 
            configureSchedulerLogic(25, true, 
                                  Arrays.asList("URGENT first", "BY_LOCATION", "BY_SKILL"), 
                                  new HashMap<>()));
        
        // Add summary
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalMethodsTested", 7);
        summary.put("adminName", this.getName());
        summary.put("adminEmail", this.getEmail());
        summary.put("existingEndpointsUsed", Arrays.asList(
            "/api/equipment",
            "/api/interventions"
        ));
        summary.put("timestamp", new Date());
        results.put("summary", summary);
        
        return results;
    }
    
    // ==================== HELPER METHODS ====================
    
    private void logAction(String action, String details) {
        // In a real application, you would log this to a database or log file
        System.out.println("[ACTION LOG] " + new Date() + " - " + action + " - " + 
                          this.getMaintenanceAdminId() + " - " + details);
    }

    @Override
    public String toString() {
        return "MaintenanceAdmin{" +
                "maintenanceAdminId='" + getMaintenanceAdminId() + '\'' +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", department='" + getDepartment() + '\'' +
                ", managedEquipmentTypes=" + managedEquipmentTypes +
                ", canConfigureScheduler=" + canConfigureScheduler +
                ", maxAssignmentsPerDay=" + maxAssignmentsPerDay +
                ", isActive=" + getActive() +
                ", joinDate=" + getJoinDate() +
                '}';
    }
}