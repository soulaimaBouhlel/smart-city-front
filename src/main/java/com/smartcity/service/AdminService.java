package com.smartcity.service;

import com.smartcity.model.*;
import com.smartcity.repository.UserAdminRepository;
import com.smartcity.repository.MaintenanceAdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AdminService {
    
    @Autowired
    private UserAdminRepository userAdminRepository;
    
    @Autowired
    private MaintenanceAdminRepository maintenanceAdminRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // ==================== USER ADMIN METHODS ====================
    
    public UserAdmin createUserAdmin(UserAdmin userAdmin) {
        userAdmin.setPassword(passwordEncoder.encode(userAdmin.getPassword()));
        userAdmin.setRole(UserRole.USER_ADMIN);
        return userAdminRepository.save(userAdmin);
    }
    
    public Optional<UserAdmin> getUserAdminById(String id) {
        return userAdminRepository.findById(id);
    }
    
    public UserAdmin getUserAdminByEmail(String email) {
        return userAdminRepository.findUserAdminByEmail(email);
    }
    
    public List<UserAdmin> getAllUserAdmins() {
        return userAdminRepository.findAll();
    }
    
    public List<UserAdmin> getUserAdminsByDepartment(String department) {
        return userAdminRepository.findByDepartment(department);
    }
    
    public UserAdmin updateUserAdmin(String id, UserAdmin updatedAdmin) {
        return userAdminRepository.findById(id).map(existingAdmin -> {
            existingAdmin.setName(updatedAdmin.getName());
            existingAdmin.setEmail(updatedAdmin.getEmail());
            existingAdmin.setPhone(updatedAdmin.getPhone());
            existingAdmin.setPhotoUrl(updatedAdmin.getPhotoUrl());
            existingAdmin.setDepartment(updatedAdmin.getDepartment());
            existingAdmin.setActive(updatedAdmin.getActive());
            existingAdmin.setPermissions(updatedAdmin.getPermissions());
            return userAdminRepository.save(existingAdmin);
        }).orElseThrow(() -> new RuntimeException("UserAdmin not found with id: " + id));
    }
    
    // Add this missing method
    public UserAdmin updateUserAdminPassword(String id, String newPassword) {
        return userAdminRepository.findById(id).map(admin -> {
            admin.setPassword(passwordEncoder.encode(newPassword));
            return userAdminRepository.save(admin);
        }).orElseThrow(() -> new RuntimeException("UserAdmin not found with id: " + id));
    }
    
    public void deleteUserAdmin(String id) {
        userAdminRepository.deleteById(id);
    }
    
    // Add this missing method
    public UserAdmin toggleUserAdminStatus(String id) {
        return userAdminRepository.findById(id).map(admin -> {
            admin.setActive(!admin.getActive());
            return userAdminRepository.save(admin);
        }).orElseThrow(() -> new RuntimeException("UserAdmin not found with id: " + id));
    }
    
    // ==================== MAINTENANCE ADMIN METHODS ====================
    
    public MaintenanceAdmin createMaintenanceAdmin(MaintenanceAdmin maintenanceAdmin) {
        maintenanceAdmin.setPassword(passwordEncoder.encode(maintenanceAdmin.getPassword()));
        maintenanceAdmin.setRole(UserRole.MAINTENANCE_ADMIN);
        return maintenanceAdminRepository.save(maintenanceAdmin);
    }
    
    public Optional<MaintenanceAdmin> getMaintenanceAdminById(String id) {
        return maintenanceAdminRepository.findById(id);
    }
    
    public MaintenanceAdmin getMaintenanceAdminByEmail(String email) {
        return maintenanceAdminRepository.findMaintenanceAdminByEmail(email);
    }
    
    public List<MaintenanceAdmin> getAllMaintenanceAdmins() {
        return maintenanceAdminRepository.findAll();
    }
    
    public List<MaintenanceAdmin> getMaintenanceAdminsByDepartment(String department) {
        return maintenanceAdminRepository.findByDepartment(department);
    }
    
    public MaintenanceAdmin updateMaintenanceAdmin(String id, MaintenanceAdmin updatedAdmin) {
        return maintenanceAdminRepository.findById(id).map(existingAdmin -> {
            existingAdmin.setName(updatedAdmin.getName());
            existingAdmin.setEmail(updatedAdmin.getEmail());
            existingAdmin.setPhone(updatedAdmin.getPhone());
            existingAdmin.setPhotoUrl(updatedAdmin.getPhotoUrl());
            existingAdmin.setDepartment(updatedAdmin.getDepartment());
            existingAdmin.setActive(updatedAdmin.getActive());
            existingAdmin.setManagedEquipmentTypes(updatedAdmin.getManagedEquipmentTypes());
            existingAdmin.setCanConfigureScheduler(updatedAdmin.getCanConfigureScheduler());
            existingAdmin.setMaxAssignmentsPerDay(updatedAdmin.getMaxAssignmentsPerDay());
            existingAdmin.setPermissions(updatedAdmin.getPermissions());
            return maintenanceAdminRepository.save(existingAdmin);
        }).orElseThrow(() -> new RuntimeException("MaintenanceAdmin not found with id: " + id));
    }
    
    // Add this missing method
    public MaintenanceAdmin updateMaintenanceAdminPassword(String id, String newPassword) {
        return maintenanceAdminRepository.findById(id).map(admin -> {
            admin.setPassword(passwordEncoder.encode(newPassword));
            return maintenanceAdminRepository.save(admin);
        }).orElseThrow(() -> new RuntimeException("MaintenanceAdmin not found with id: " + id));
    }
    
    public void deleteMaintenanceAdmin(String id) {
        maintenanceAdminRepository.deleteById(id);
    }
    
    // Add this missing method
    public MaintenanceAdmin toggleMaintenanceAdminStatus(String id) {
        return maintenanceAdminRepository.findById(id).map(admin -> {
            admin.setActive(!admin.getActive());
            return maintenanceAdminRepository.save(admin);
        }).orElseThrow(() -> new RuntimeException("MaintenanceAdmin not found with id: " + id));
    }
    
    // ==================== GENERIC ADMIN METHODS ====================
    
    public boolean authenticateAdmin(String email, String password) {
        // Try UserAdmin
        UserAdmin userAdmin = userAdminRepository.findUserAdminByEmail(email);
        if (userAdmin != null) {
            return passwordEncoder.matches(password, userAdmin.getPassword());
        }
        
        // Try MaintenanceAdmin
        MaintenanceAdmin maintenanceAdmin = maintenanceAdminRepository.findMaintenanceAdminByEmail(email);
        if (maintenanceAdmin != null) {
            return passwordEncoder.matches(password, maintenanceAdmin.getPassword());
        }
        
        return false;
    }
    
    public UserRole getAdminRole(String email) {
        UserAdmin userAdmin = userAdminRepository.findUserAdminByEmail(email);
        if (userAdmin != null) {
            return userAdmin.getRole();
        }
        
        MaintenanceAdmin maintenanceAdmin = maintenanceAdminRepository.findMaintenanceAdminByEmail(email);
        if (maintenanceAdmin != null) {
            return maintenanceAdmin.getRole();
        }
        
        return null;
    }
    
    public long getTotalAdminCount() {
        return userAdminRepository.count() + maintenanceAdminRepository.count();
    }
}