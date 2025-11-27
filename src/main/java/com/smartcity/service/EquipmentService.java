// src/main/java/com/smartcity/service/EquipmentService.java
package com.smartcity.service;

import com.smartcity.model.Equipment;
import com.smartcity.model.EquipmentType;
import com.smartcity.model.Status;

import java.util.List;
import java.util.Optional;

public interface EquipmentService {
    
    List<Equipment> getAllEquipments();
    
    Optional<Equipment> getEquipmentById(String id);
    
    Optional<Equipment> getEquipmentByEquipmentId(Integer equipmentId);
    
    Equipment createEquipment(Equipment equipment);
    
    Equipment updateEquipment(String id, Equipment equipment);
    
    Equipment updateEquipmentStatus(Integer equipmentId, Status status);
    
    void deleteEquipment(String id);
    
    void deleteEquipmentByEquipmentId(Integer equipmentId);
    
    List<Equipment> getEquipmentsByType(EquipmentType type);
    
    List<Equipment> getEquipmentsByStatus(Status status);
    
    List<Equipment> getEquipmentsByTypeAndStatus(EquipmentType type, Status status);
    
    List<Equipment> getEquipmentsNeedingMaintenance();
    
    List<Equipment> searchEquipmentsByAddress(String address);
    
    boolean equipmentExists(Integer equipmentId);
}