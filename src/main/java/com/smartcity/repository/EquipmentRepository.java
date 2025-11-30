package com.smartcity.repository;

import com.smartcity.model.Equipment;
import com.smartcity.model.Status;
import com.smartcity.model.EquipmentType;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

// Extends MongoRepository to get basic CRUD operations
public interface EquipmentRepository extends MongoRepository<Equipment, String> {

    // Maps to o_filterByStatus(status: String) from InteractiveMap
    List<Equipment> findByStatus(Status status);

    // Maps to o_filterByType(type: String) from InteractiveMap
    List<Equipment> findByType(EquipmentType type);
}