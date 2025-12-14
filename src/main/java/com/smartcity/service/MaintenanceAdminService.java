package com.smartcity.service;

import com.smartcity.model.*;
import com.smartcity.repository.EquipmentRepository;
import com.smartcity.repository.InterventionRepository;
import com.smartcity.repository.MaintenanceAdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MaintenanceAdminService {

    @Autowired
    private MaintenanceAdminRepository maintenanceAdminRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private InterventionRepository interventionRepository;

    // ==================== EQUIPMENT METHODS ====================

    public Map<String, Object> addEquipment(String adminId, String name, EquipmentType type,
            Double latitude, Double longitude,
            String address, Status status) {
        MaintenanceAdmin admin = maintenanceAdminRepository.findById(adminId).orElse(null);
        if (admin == null) {
            admin = maintenanceAdminRepository.findMaintenanceAdminByEmail(adminId);
        }

        if (admin == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "MaintenanceAdmin not found");
            return result;
        }

        try {
            // Create new Equipment object
            Equipment equipment = Equipment.builder()
                    .name(name)
                    .type(type)
                    .address(address)
                    .status(status.toString())
                    .lastMaintenance(new Date())
                    .build();

            // Set location if coordinates provided
            if (latitude != null && longitude != null) {
                equipment.setLocation(new org.springframework.data.mongodb.core.geo.GeoJsonPoint(
                        longitude, latitude));
            }

            // Save to database
            Equipment savedEquipment = equipmentRepository.save(equipment);

            Map<String, Object> result = new HashMap<>();
            result.put("method", "addEquipment");
            result.put("success", true);
            result.put("adminId", admin.getMaintenanceAdminId());
            result.put("adminName", admin.getName());
            result.put("equipmentId", savedEquipment.getId());
            result.put("equipmentName", savedEquipment.getName());
            result.put("equipmentType", savedEquipment.getType());
            result.put("address", savedEquipment.getAddress());
            result.put("status", savedEquipment.getStatus());
            result.put("timestamp", new Date());
            result.put("message", "Equipment '" + name + "' has been successfully added to the database");

            System.out.println("[ACTION LOG] " + new Date() + " - ADD_EQUIPMENT - " +
                    admin.getMaintenanceAdminId() + " - Added equipment: " + name +
                    " (ID: " + savedEquipment.getId() + ")");

            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("method", "addEquipment");
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("timestamp", new Date());
            result.put("message", "Failed to add equipment: " + e.getMessage());

            return result;
        }
    }

    public Map<String, Object> updateEquipment(String adminId, String equipmentId, String name,
            EquipmentType type, Double latitude, Double longitude,
            String address, Status status) {
        MaintenanceAdmin admin = maintenanceAdminRepository.findById(adminId).orElse(null);
        if (admin == null) {
            admin = maintenanceAdminRepository.findMaintenanceAdminByEmail(adminId);
        }

        if (admin == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "MaintenanceAdmin not found");
            return result;
        }

        try {
            // Find existing equipment
            Optional<Equipment> existingEquipmentOpt = equipmentRepository.findById(equipmentId);
            if (existingEquipmentOpt.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("method", "updateEquipment");
                result.put("success", false);
                result.put("error", "Equipment not found");
                result.put("equipmentId", equipmentId);
                result.put("timestamp", new Date());
                result.put("message", "Equipment with ID '" + equipmentId + "' not found");
                return result;
            }

            Equipment equipment = existingEquipmentOpt.get();

            // Update fields
            if (name != null)
                equipment.setName(name);
            if (type != null)
                equipment.setType(type);
            if (address != null)
                equipment.setAddress(address);
            if (status != null)
                equipment.setStatus(status);
            if (latitude != null && longitude != null) {
                equipment.setLocation(new org.springframework.data.mongodb.core.geo.GeoJsonPoint(
                        longitude, latitude));
            }

            // Save updated equipment
            Equipment updatedEquipment = equipmentRepository.save(equipment);

            Map<String, Object> result = new HashMap<>();
            result.put("method", "updateEquipment");
            result.put("success", true);
            result.put("adminId", admin.getMaintenanceAdminId());
            result.put("adminName", admin.getName());
            result.put("equipmentId", updatedEquipment.getId());
            result.put("equipmentName", updatedEquipment.getName());
            result.put("equipmentType", updatedEquipment.getType());
            result.put("address", updatedEquipment.getAddress());
            result.put("status", updatedEquipment.getStatus());
            result.put("timestamp", new Date());
            result.put("message", "Equipment ID '" + equipmentId + "' has been successfully updated");

            System.out.println("[ACTION LOG] " + new Date() + " - UPDATE_EQUIPMENT - " +
                    admin.getMaintenanceAdminId() + " - Updated equipment ID: " + equipmentId);

            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("method", "updateEquipment");
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("timestamp", new Date());
            result.put("message", "Failed to update equipment: " + e.getMessage());

            return result;
        }
    }

    public Map<String, Object> removeEquipment(String adminId, String equipmentId, String reason) {
        MaintenanceAdmin admin = maintenanceAdminRepository.findById(adminId).orElse(null);
        if (admin == null) {
            admin = maintenanceAdminRepository.findMaintenanceAdminByEmail(adminId);
        }

        if (admin == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "MaintenanceAdmin not found");
            return result;
        }

        try {
            // Check if equipment exists
            if (!equipmentRepository.existsById(equipmentId)) {
                Map<String, Object> result = new HashMap<>();
                result.put("method", "removeEquipment");
                result.put("success", false);
                result.put("error", "Equipment not found");
                result.put("equipmentId", equipmentId);
                result.put("timestamp", new Date());
                result.put("message", "Equipment with ID '" + equipmentId + "' not found");
                return result;
            }

            // Delete equipment
            equipmentRepository.deleteById(equipmentId);

            Map<String, Object> result = new HashMap<>();
            result.put("method", "removeEquipment");
            result.put("success", true);
            result.put("adminId", admin.getMaintenanceAdminId());
            result.put("adminName", admin.getName());
            result.put("equipmentId", equipmentId);
            result.put("reason", reason);
            result.put("timestamp", new Date());
            result.put("message", "Equipment ID '" + equipmentId + "' has been successfully removed");

            System.out.println("[ACTION LOG] " + new Date() + " - REMOVE_EQUIPMENT - " +
                    admin.getMaintenanceAdminId() + " - Removed equipment ID: " + equipmentId +
                    ". Reason: " + reason);

            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("method", "removeEquipment");
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("timestamp", new Date());
            result.put("message", "Failed to remove equipment: " + e.getMessage());

            return result;
        }
    }

    // Add other methods (createIntervention, updateIntervention, etc.) similarly
}