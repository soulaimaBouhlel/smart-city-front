package com.smartcity.repository;

import com.smartcity.model.Technician;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TechnicianRepository extends MongoRepository<Technician, String> {
    Optional<Technician> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Technician> findByAvailable(Boolean available);
    List<Technician> findByAvailableTrue();
    // REMOVED: existsByLogID and findByLogID methods
}