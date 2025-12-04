package com.smartcity.repository;

import com.smartcity.model.Intervention;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Intervention entities.
 */
@Repository
public interface InterventionRepository extends MongoRepository<Intervention, String> {

    /**
     * Find interventions by status
     */
    List<Intervention> findByStatus(Intervention.Status status);

    /**
     * Find interventions by priority
     */
    List<Intervention> findByPriority(Intervention.Priority priority);

    /**
     * Find interventions by type
     */
    List<Intervention> findByInterventionType(String interventionType);

    /**
     * Find interventions by equipment ID
     */
    List<Intervention> findByEquipmentId(String equipmentId);

    /**
     * Find interventions by assigned technician ID
     */
    List<Intervention> findByAssignedTechnicianId(String technicianId);

    /**
     * Find unassigned interventions (no technician assigned yet)
     */
    List<Intervention> findByAssignedTechnicianIdIsNull();
}
