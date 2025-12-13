package com.smartcity.repository;

import com.smartcity.model.Intervention;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InterventionRepository extends MongoRepository<Intervention, String> {

    /**
     * Find interventions by status (String)
     */
    List<Intervention> findByStatus(String status);

    /**
     * Find interventions by priority (String)
     */
    List<Intervention> findByPriority(String priority);

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
    
    /**
     * Find interventions with equipment (for debugging)
     */
    default List<Intervention> findByStatusWithEquipment(String status) {
        List<Intervention> interventions = findByStatus(status);
        interventions.forEach(intervention -> {
            // Ensure equipment is loaded if needed
            if (intervention.getEquipment() == null && intervention.getEquipmentId() != null) {
                // In real implementation, you'd fetch equipment
            }
        });
        return interventions;
    }
}