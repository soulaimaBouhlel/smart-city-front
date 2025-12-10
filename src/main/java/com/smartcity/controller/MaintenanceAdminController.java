package com.smartcity.controller;

import com.smartcity.model.*;
import com.smartcity.repository.MaintenanceAdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceAdminController {
    
    @Autowired
    private MaintenanceAdminRepository maintenanceAdminRepository;
    
    /**
     * Test endpoint to demonstrate MaintenanceAdmin business logic
     * This doesn't duplicate existing CRUD operations
     */
    @PostMapping("/{adminId}/test-business-logic")
    @PreAuthorize("hasRole('MAINTENANCE_ADMIN')")
    public ResponseEntity<?> testBusinessLogic(@PathVariable String adminId) {
        MaintenanceAdmin admin = maintenanceAdminRepository.findById(adminId).orElse(null);
        if (admin == null) {
            admin = maintenanceAdminRepository.findMaintenanceAdminByEmail(adminId);
        }
        
        if (admin == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> result = admin.testAllMethods();
        return ResponseEntity.ok(result);
    }
    
    /**
     * Test specific business logic
     */
    @PostMapping("/{adminId}/logic/configure-scheduler")
    @PreAuthorize("hasRole('MAINTENANCE_ADMIN')")
    public ResponseEntity<?> configureSchedulerLogic(
            @PathVariable String adminId,
            @RequestBody ConfigureSchedulerRequest request) {
        
        MaintenanceAdmin admin = maintenanceAdminRepository.findById(adminId).orElse(null);
        if (admin == null) {
            admin = maintenanceAdminRepository.findMaintenanceAdminByEmail(adminId);
        }
        
        if (admin == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> result = admin.configureSchedulerLogic(
            request.getMaxAssignments(),
            request.getEnableAutoAssignment(),
            request.getPriorityRules(),
            request.getSettings()
        );
        
        // Save the updated admin configuration
        maintenanceAdminRepository.save(admin);
        
        return ResponseEntity.ok(result);
    }
    
    public static class ConfigureSchedulerRequest {
        private Integer maxAssignments;
        private Boolean enableAutoAssignment;
        private List<String> priorityRules;
        private Map<String, Object> settings;
        
        // Getters and setters
        public Integer getMaxAssignments() { return maxAssignments; }
        public void setMaxAssignments(Integer maxAssignments) { this.maxAssignments = maxAssignments; }
        
        public Boolean getEnableAutoAssignment() { return enableAutoAssignment; }
        public void setEnableAutoAssignment(Boolean enableAutoAssignment) { this.enableAutoAssignment = enableAutoAssignment; }
        
        public List<String> getPriorityRules() { return priorityRules; }
        public void setPriorityRules(List<String> priorityRules) { this.priorityRules = priorityRules; }
        
        public Map<String, Object> getSettings() { return settings; }
        public void setSettings(Map<String, Object> settings) { this.settings = settings; }
    }
}