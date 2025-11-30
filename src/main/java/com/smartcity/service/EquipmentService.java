package com.smartcity.service;

import com.smartcity.model.Equipment;
import com.smartcity.model.Status;
import com.smartcity.model.EquipmentType;
import com.smartcity.repository.EquipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EquipmentService {

    @Autowired
    private EquipmentRepository equipmentRepository;

    // CRUD - Create
    public Equipment createEquipment(Equipment equipment) {
        return equipmentRepository.save(equipment);
    }

    // CRUD - Read All
    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }

    // CRUD - Read By ID
    public Optional<Equipment> getEquipmentById(String id) {
        return equipmentRepository.findById(id);
    }

    // CRUD - Update
    public Equipment updateEquipment(String id, Equipment equipmentDetails) {
        return equipmentRepository.findById(id)
                .map(equipment -> {
                    equipment.setName(equipmentDetails.getName());
                    equipment.setType(equipmentDetails.getType());
                    equipment.setLocation(equipmentDetails.getLocation());
                    equipment.setStatus(equipmentDetails.getStatus());
                    equipment.setLastMaintenance(equipmentDetails.getLastMaintenance());
                    return equipmentRepository.save(equipment);
                }).orElseThrow(() -> new RuntimeException("Equipment not found with id " + id));
    }
    
    // CRUD - Delete
    public void deleteEquipment(String id) {
        equipmentRepository.deleteById(id);
    }

    // Filtering logic based on InteractiveMap methods
    public List<Equipment> filterByStatus(Status status) {
        return equipmentRepository.findByStatus(status);
    }

    public List<Equipment> filterByType(EquipmentType type) {
        return equipmentRepository.findByType(type);
    }
}