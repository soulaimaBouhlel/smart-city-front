package com.smartcity.repository;

import com.smartcity.model.Equipment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Equipment entities.
 */
@Repository
public interface EquipmentRepository extends MongoRepository<Equipment, String> {

    /**
     * Find equipment by type
     */
    List<Equipment> findByType(String type);

    /**
     * Find equipment by status
     */
    List<Equipment> findByStatus(String status);
}
