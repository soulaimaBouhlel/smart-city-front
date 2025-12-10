package com.smartcity.controller;

import com.smartcity.dto.*;
import com.smartcity.model.*;
import com.smartcity.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admins")
@Validated
public class AdminController {
    
    @Autowired
    private AdminService adminService;
    
    // ==================== USER ADMIN ENDPOINTS ====================
    
    @PostMapping("/user")
    @PreAuthorize("hasRole('USER_ADMIN')")
    public ResponseEntity<?> createUserAdmin(@Valid @RequestBody UserAdminDTO adminDTO) {
        try {
            UserAdmin userAdmin = new UserAdmin(
                adminDTO.getName(),
                adminDTO.getEmail(),
                adminDTO.getPhone(),
                adminDTO.getPassword(),
                adminDTO.getPhotoUrl(),
                adminDTO.getDepartment()
            );
            
            UserAdmin created = adminService.createUserAdmin(userAdmin);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User admin created successfully");
            response.put("adminId", created.getUserAdminId());
            response.put("admin", created);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error creating user admin: " + e.getMessage());
        }
    }
    
    @GetMapping("/user/{id}")
    @PreAuthorize("hasRole('USER_ADMIN')")
    public ResponseEntity<?> getUserAdmin(@PathVariable String id) {
        return adminService.getUserAdminById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/user")
    @PreAuthorize("hasRole('USER_ADMIN')")
    public ResponseEntity<List<UserAdmin>> getAllUserAdmins() {
        return ResponseEntity.ok(adminService.getAllUserAdmins());
    }
    
    @GetMapping("/user/department/{department}")
    @PreAuthorize("hasRole('USER_ADMIN')")
    public ResponseEntity<List<UserAdmin>> getUserAdminsByDepartment(@PathVariable String department) {
        return ResponseEntity.ok(adminService.getUserAdminsByDepartment(department));
    }
    
    @PutMapping("/user/{id}")
    @PreAuthorize("hasRole('USER_ADMIN')")
    public ResponseEntity<?> updateUserAdmin(
            @PathVariable String id,
            @Valid @RequestBody UserAdminDTO adminDTO) {
        try {
            UserAdmin updatedAdmin = new UserAdmin(
                adminDTO.getName(),
                adminDTO.getEmail(),
                adminDTO.getPhone(),
                "", // Password not updated here
                adminDTO.getPhotoUrl(),
                adminDTO.getDepartment()
            );
            
            UserAdmin result = adminService.updateUserAdmin(id, updatedAdmin);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        }
    }
    
    @PatchMapping("/user/{id}/password")
    @PreAuthorize("hasRole('USER_ADMIN')")
    public ResponseEntity<?> updateUserAdminPassword(
            @PathVariable String id,
            @Valid @RequestBody PasswordUpdateDTO passwordDTO) {
        try {
            // Verify passwords match
            if (!passwordDTO.getNewPassword().equals(passwordDTO.getConfirmPassword())) {
                return ResponseEntity.badRequest().body("New password and confirmation do not match");
            }
            
            // You'll need to implement this method in AdminService
            UserAdmin admin = adminService.updateUserAdminPassword(id, passwordDTO.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Password updated successfully", "adminId", admin.getUserAdminId()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        }
    }
    
    @PatchMapping("/user/{id}/toggle-status")
    @PreAuthorize("hasRole('USER_ADMIN')")
    public ResponseEntity<?> toggleUserAdminStatus(@PathVariable String id) {
        try {
            UserAdmin admin = adminService.toggleUserAdminStatus(id);
            String status = admin.getActive() ? "activated" : "deactivated";
            return ResponseEntity.ok(Map.of(
                "message", "User admin " + status + " successfully",
                "adminId", admin.getUserAdminId(),
                "active", admin.getActive()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/user/{id}")
    @PreAuthorize("hasRole('USER_ADMIN')")
    public ResponseEntity<?> deleteUserAdmin(@PathVariable String id) {
        try {
            adminService.deleteUserAdmin(id);
            return ResponseEntity.ok(Map.of("message", "User admin deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        }
    }
    
    // ==================== MAINTENANCE ADMIN ENDPOINTS ====================
    
    @PostMapping("/maintenance")
    @PreAuthorize("hasRole('USER_ADMIN')")
    public ResponseEntity<?> createMaintenanceAdmin(@Valid @RequestBody MaintenanceAdminDTO adminDTO) {
        try {
            MaintenanceAdmin maintenanceAdmin = new MaintenanceAdmin(
                adminDTO.getName(),
                adminDTO.getEmail(),
                adminDTO.getPhone(),
                adminDTO.getPassword(),
                adminDTO.getPhotoUrl(),
                adminDTO.getDepartment()
            );
            
            maintenanceAdmin.setManagedEquipmentTypes(adminDTO.getManagedEquipmentTypes());
            maintenanceAdmin.setCanConfigureScheduler(adminDTO.getCanConfigureScheduler());
            maintenanceAdmin.setMaxAssignmentsPerDay(adminDTO.getMaxAssignmentsPerDay());
            
            MaintenanceAdmin created = adminService.createMaintenanceAdmin(maintenanceAdmin);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Maintenance admin created successfully");
            response.put("adminId", created.getMaintenanceAdminId());
            response.put("admin", created);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error creating maintenance admin: " + e.getMessage());
        }
    }
    
    @GetMapping("/maintenance/{id}")
    @PreAuthorize("hasAnyRole('USER_ADMIN', 'MAINTENANCE_ADMIN')")
    public ResponseEntity<?> getMaintenanceAdmin(@PathVariable String id) {
        return adminService.getMaintenanceAdminById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/maintenance")
    @PreAuthorize("hasAnyRole('USER_ADMIN', 'MAINTENANCE_ADMIN')")
    public ResponseEntity<List<MaintenanceAdmin>> getAllMaintenanceAdmins() {
        return ResponseEntity.ok(adminService.getAllMaintenanceAdmins());
    }
    
    @GetMapping("/maintenance/department/{department}")
    @PreAuthorize("hasRole('MAINTENANCE_ADMIN')")
    public ResponseEntity<List<MaintenanceAdmin>> getMaintenanceAdminsByDepartment(@PathVariable String department) {
        return ResponseEntity.ok(adminService.getMaintenanceAdminsByDepartment(department));
    }
    
    @PutMapping("/maintenance/{id}")
    @PreAuthorize("hasRole('MAINTENANCE_ADMIN')")
    public ResponseEntity<?> updateMaintenanceAdmin(
            @PathVariable String id,
            @Valid @RequestBody MaintenanceAdminDTO adminDTO) {
        try {
            MaintenanceAdmin updatedAdmin = new MaintenanceAdmin(
                adminDTO.getName(),
                adminDTO.getEmail(),
                adminDTO.getPhone(),
                "", // Password not updated here
                adminDTO.getPhotoUrl(),
                adminDTO.getDepartment()
            );
            
            updatedAdmin.setManagedEquipmentTypes(adminDTO.getManagedEquipmentTypes());
            updatedAdmin.setCanConfigureScheduler(adminDTO.getCanConfigureScheduler());
            updatedAdmin.setMaxAssignmentsPerDay(adminDTO.getMaxAssignmentsPerDay());
            
            MaintenanceAdmin result = adminService.updateMaintenanceAdmin(id, updatedAdmin);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        }
    }
    
    @PatchMapping("/maintenance/{id}/password")
    @PreAuthorize("hasRole('MAINTENANCE_ADMIN')")
    public ResponseEntity<?> updateMaintenanceAdminPassword(
            @PathVariable String id,
            @Valid @RequestBody PasswordUpdateDTO passwordDTO) {
        try {
            // Verify passwords match
            if (!passwordDTO.getNewPassword().equals(passwordDTO.getConfirmPassword())) {
                return ResponseEntity.badRequest().body("New password and confirmation do not match");
            }
            
            // You'll need to implement this method in AdminService
            MaintenanceAdmin admin = adminService.updateMaintenanceAdminPassword(id, passwordDTO.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Password updated successfully", "adminId", admin.getMaintenanceAdminId()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        }
    }
    
    @PatchMapping("/maintenance/{id}/toggle-status")
    @PreAuthorize("hasRole('USER_ADMIN')")
    public ResponseEntity<?> toggleMaintenanceAdminStatus(@PathVariable String id) {
        try {
            MaintenanceAdmin admin = adminService.toggleMaintenanceAdminStatus(id);
            String status = admin.getActive() ? "activated" : "deactivated";
            return ResponseEntity.ok(Map.of(
                "message", "Maintenance admin " + status + " successfully",
                "adminId", admin.getMaintenanceAdminId(),
                "active", admin.getActive()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/maintenance/{id}")
    @PreAuthorize("hasRole('USER_ADMIN')")
    public ResponseEntity<?> deleteMaintenanceAdmin(@PathVariable String id) {
        try {
            adminService.deleteMaintenanceAdmin(id);
            return ResponseEntity.ok(Map.of("message", "Maintenance admin deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        }
    }
    
    // ==================== GENERIC ENDPOINTS ====================
    
    @PostMapping("/authenticate")
    @PreAuthorize("hasAnyRole('USER_ADMIN', 'MAINTENANCE_ADMIN')")
    public ResponseEntity<?> authenticateAdmin(
            @RequestParam String email,
            @RequestParam String password) {
        boolean isAuthenticated = adminService.authenticateAdmin(email, password);
        
        if (isAuthenticated) {
            UserRole role = adminService.getAdminRole(email);
            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", true);
            response.put("email", email);
            response.put("role", role);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("authenticated", false, "message", "Invalid credentials"));
        }
    }
    
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('USER_ADMIN', 'MAINTENANCE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        long userAdminCount = adminService.getAllUserAdmins().size();
        long maintenanceAdminCount = adminService.getAllMaintenanceAdmins().size();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAdmins", userAdminCount + maintenanceAdminCount);
        stats.put("userAdmins", userAdminCount);
        stats.put("maintenanceAdmins", maintenanceAdminCount);
        
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('USER_ADMIN', 'MAINTENANCE_ADMIN')")
    public ResponseEntity<Map<String, Long>> getAdminCount() {
        long count = adminService.getTotalAdminCount();
        return ResponseEntity.ok(Map.of("totalAdmins", count));
    }
}