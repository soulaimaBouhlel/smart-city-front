package com.smartcity.repository;

import com.smartcity.model.Equipment;
import com.smartcity.model.Status;
import com.smartcity.model.EquipmentType;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

// Extends MongoRepository to get basic CRUD operations
public interface EquipmentRepository extends MongoRepository<Equipment, String> {

    /**
     * Finds a list of Equipment entities filtered by their Status.
     * Uses the Status enum for type safety.
     */
    List<Equipment> findByStatus(Status status);

    /**
     * Finds a list of Equipment entities filtered by their EquipmentType.
     * Uses the EquipmentType enum for type safety.
     */
    List<Equipment> findByType(EquipmentType type);
}