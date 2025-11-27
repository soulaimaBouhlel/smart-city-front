// src/main/java/com/smartcity/repository/EquipmentRepository.java
package com.smartcity.repository;

import com.smartcity.model.Equipment;
import com.smartcity.model.EquipmentType;
import com.smartcity.model.Status;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentRepository extends MongoRepository<Equipment, String> {
    
    Optional<Equipment> findByEquipmentId(Integer equipmentId);
    
    List<Equipment> findByType(EquipmentType type);
    
    List<Equipment> findByStatus(Status status);
    
    List<Equipment> findByTypeAndStatus(EquipmentType type, Status status);
    
    @Query("{ 'location.address': { $regex: ?0, $options: 'i' } }")
    List<Equipment> findByAddressContaining(String address);
    
    // FIXED: Use the correct field name and query syntax
    @Query("{ 'lastMaintenance': { $lt: ?0 } }")
    List<Equipment> findByLastMaintenanceBefore(LocalDateTime dateThreshold);
    
    // Alternative approach using method naming convention
    List<Equipment> findByLastMaintenanceIsNull();
    
    boolean existsByEquipmentId(Integer equipmentId);
    
    void deleteByEquipmentId(Integer equipmentId);
}