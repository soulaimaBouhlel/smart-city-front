package com.smartcity.repository;

import com.smartcity.model.Technician;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
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

    /**
     * Find technicians near a given location within a specified distance.
     * Uses MongoDB's geospatial query capabilities.
     * 
     * @param location the center point
     * @param distance the maximum distance
     * @return list of technicians within the distance
     */
    List<Technician> findByLocationNear(Point location, Distance distance);
}