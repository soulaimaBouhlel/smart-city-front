package com.smartcity.repository;

import com.smartcity.model.MaintenanceAdmin;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceAdminRepository extends MongoRepository<MaintenanceAdmin, String> {
    
    @Query("{ 'email' : ?0 }")
    Optional<MaintenanceAdmin> findByEmail(String email);
    
    // Helper method that returns MaintenanceAdmin directly (not Optional)
    default MaintenanceAdmin findMaintenanceAdminByEmail(String email) {
        return findByEmail(email).orElse(null);
    }
    
    @Query("{ 'department' : ?0 }")
    List<MaintenanceAdmin> findByDepartment(String department);
    
    @Query(value = "{ 'email' : ?0 }", exists = true)
    boolean existsByEmail(String email);
}