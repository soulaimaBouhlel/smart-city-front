package com.smartcity.repository;

import com.smartcity.model.Equipment;
import com.smartcity.model.Status;
import com.smartcity.model.EquipmentType;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

// Extends MongoRepository to get basic CRUD operations
public interface EquipmentRepository extends MongoRepository<Equipment, String> {

    /**
     * Finds a list of Equipment entities filtered by their Status.
     * Uses the Status enum for type safety.
     */
    List<Equipment> findByStatus(Status status);

    /**
     * Finds a list of Equipment entities filtered by their Status as String.
     * Added to support String status values from services.
     */
    List<Equipment> findByStatus(String status);

    /**
     * Finds a list of Equipment entities filtered by their EquipmentType.
     * Uses the EquipmentType enum for type safety.
     */
    List<Equipment> findByType(EquipmentType type);

    /**
     * Find equipment near a given location within a specified distance.
     * Uses MongoDB's geospatial query capabilities.
     * 
     * @param location the center point
     * @param distance the maximum distance
     * @return list of equipment within the distance
     */
    List<Equipment> findByLocationNear(Point location, Distance distance);

    /**
     * Find equipment by status with case-insensitive search
     */
    @Query("{'status': {$regex: ?0, $options: 'i'}}")
    List<Equipment> findByStatusIgnoreCase(String status);

    /**
     * Find equipment by type name (String) instead of enum
     */
    @Query("{'type': {$regex: ?0, $options: 'i'}}")
    List<Equipment> findByTypeName(String typeName);

    /**
     * Find equipment needing maintenance (status is PENDING or IN_PROGRESS)
     */
    @Query("{'status': {$in: ['PENDING', 'IN_PROGRESS']}}")
    List<Equipment> findEquipmentNeedingMaintenance();

    /**
     * Find operational equipment (status is COMPLETED or ACTIVE)
     */
    @Query("{'status': {$in: ['COMPLETED', 'ACTIVE', 'OPERATIONAL']}}")
    List<Equipment> findOperationalEquipment();
}