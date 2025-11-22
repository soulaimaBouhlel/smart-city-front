package com.smartcity.repository;

import com.smartcity.model.Citizen;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CitizenRepository extends MongoRepository<Citizen, String> {
    Optional<Citizen> findByEmail(String email);
    boolean existsByEmail(String email);
    // REMOVED: existsByLogID and findByLogID methods
}