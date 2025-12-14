package com.smartcity.service;

import com.smartcity.dto.MapPointDTO;
import com.smartcity.dto.OsrmRouteResponse;
import com.smartcity.model.Equipment;
import com.smartcity.model.Intervention;
import com.smartcity.model.Technician;
import com.smartcity.repository.EquipmentRepository;
import com.smartcity.repository.InterventionRepository;
import com.smartcity.repository.TechnicianRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MapService {

    private final EquipmentRepository equipmentRepository;
    private final TechnicianRepository technicianRepository;
    private final InterventionRepository interventionRepository;
    private final OsrmService osrmService;

    // =========================================================================
    // Core API Methods (UNCHANGED)
    // =========================================================================

    public List<Equipment> getAllEquipments() {
        log.info("Retrieving all equipment");
        return equipmentRepository.findAll();
    }

    public List<Technician> getAllTechnicians() {
        log.info("Retrieving all technicians");
        return technicianRepository.findAll();
    }

    public List<Technician> getNearestTechnicians(double lng, double lat, double maxDistanceKm) {
        Point location = new Point(lng, lat);
        Distance distance = new Distance(maxDistanceKm, Metrics.KILOMETERS);

        log.info("Finding technicians within {}km of ({}, {})", maxDistanceKm, lat, lng);
        List<Technician> technicians = technicianRepository.findByLocationNear(location, distance);
        log.info("Found {} technicians", technicians.size());

        return technicians;
    }

    public OsrmRouteResponse computeRoute(double fromLng, double fromLat, double toLng, double toLat) {
        log.info("Computing route from ({}, {}) to ({}, {})", fromLat, fromLng, toLat, toLng);
        return osrmService.getRoute(fromLng, fromLat, toLng, toLat);
    }

    // =========================================================================
    // MapPointDTO Helper Methods
    // =========================================================================

    /**
     * Get all equipment as map points
     */
    public List<MapPointDTO> getAllEquipmentPoints() {
        List<Equipment> equipments = equipmentRepository.findAll();
        log.info("Retrieved {} equipment records", equipments.size());

        return equipments.stream()
                .map(this::equipmentToMapPoint)
                .collect(Collectors.toList());
    }

    /**
     * Get all interventions as map points
     */
    public List<MapPointDTO> getAllInterventionPoints() {
        // Deprecated: We now merge interventions into equipment points
        // Returning empty list to avoid duplication on map if called directly
        return new ArrayList<>();
    }

    /**
     * Get all map points (equipment only, enriched with intervention data)
     */
    public List<MapPointDTO> getAllMapPoints() {
        List<Equipment> equipments = equipmentRepository.findAll();
        List<Intervention> activeInterventions = interventionRepository.findAll().stream()
                .filter(i -> !"COMPLETED".equals(i.getStatus()) && !"CANCELLED".equals(i.getStatus()))
                .collect(Collectors.toList());

        // Map equipmentId -> Active Intervention
        java.util.Map<String, Intervention> interventionMap = activeInterventions.stream()
                .filter(i -> i.getEquipmentId() != null)
                .collect(Collectors.toMap(Intervention::getEquipmentId, i -> i, (existing, replacement) -> existing));

        log.info("Retrieved {} equipment and {} active interventions", equipments.size(), activeInterventions.size());

        return equipments.stream()
                .map(eq -> {
                    Intervention activeIntervention = interventionMap.get(eq.getId());
                    return equipmentToMapPoint(eq, activeIntervention);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get nearby points within a radius of given coordinates
     */
    public List<MapPointDTO> getNearbyPoints(double latitude, double longitude, double radiusMeters) {
        Point location = new Point(longitude, latitude);
        Distance distance = new Distance(radiusMeters / 1000.0, Metrics.KILOMETERS);

        // Find nearby equipment
        List<Equipment> nearbyEquipment = equipmentRepository.findByLocationNear(location, distance);

        // Fetch active interventions for these equipments
        List<Intervention> activeInterventions = interventionRepository.findAll().stream()
                .filter(i -> !"COMPLETED".equals(i.getStatus()) && !"CANCELLED".equals(i.getStatus()))
                .collect(Collectors.toList());

        java.util.Map<String, Intervention> interventionMap = activeInterventions.stream()
                .filter(i -> i.getEquipmentId() != null)
                .collect(Collectors.toMap(Intervention::getEquipmentId, i -> i, (existing, replacement) -> existing));

        return nearbyEquipment.stream()
                .map(eq -> {
                    Intervention activeIntervention = interventionMap.get(eq.getId());
                    return equipmentToMapPoint(eq, activeIntervention);
                })
                .collect(Collectors.toList());
    }

    // =========================================================================
    // Private Helper Methods
    // =========================================================================

    /**
     * Convert Equipment to MapPointDTO, optionally enriching with Intervention data
     */
    private MapPointDTO equipmentToMapPoint(Equipment equipment, Intervention activeIntervention) {
        if (equipment.getLocation() == null) {
            return null;
        }

        // Determine status: Use intervention status if active, otherwise equipment
        // status
        String status = (activeIntervention != null && activeIntervention.getStatus() != null)
                ? activeIntervention.getStatus()
                : (equipment.getStatus() != null ? equipment.getStatus() : "UNKNOWN");

        MapPointDTO dto = MapPointDTO.builder()
                .id(equipment.getId())
                .type("EQUIPMENT") // Always EQUIPMENT type
                .label(equipment.getName())
                .status(status)
                .latitude(equipment.getLocation().getY())
                .longitude(equipment.getLocation().getX())
                .build();

        // Add equipment-specific metadata
        dto.addMetadata("equipmentType",
                equipment.getType() != null ? equipment.getType().toString() : "UNKNOWN");

        if (equipment.getLastMaintenance() != null) {
            dto.addMetadata("lastMaintenance", equipment.getLastMaintenance().toString());
        }

        // Convert address Object to String
        if (equipment.getAddress() != null) {
            dto.addMetadata("address", equipment.getAddress().toString());
        }

        // Add Active Intervention Metadata if present
        if (activeIntervention != null) {
            dto.addMetadata("hasActiveIntervention", "true");
            dto.addMetadata("interventionId", activeIntervention.getInterventionId());
            dto.addMetadata("interventionTitle", activeIntervention.getTitle());
            dto.addMetadata("interventionStatus", activeIntervention.getStatus());
            dto.addMetadata("interventionPriority", activeIntervention.getPriority());
            dto.addMetadata("interventionDescription", activeIntervention.getDescription());
            dto.addMetadata("interventionDate", activeIntervention.getCreatedDate().toString());

            if (activeIntervention.getAssignedTechnicianId() != null) {
                dto.addMetadata("assignedTechnicianId", activeIntervention.getAssignedTechnicianId());
            }
        } else {
            dto.addMetadata("hasActiveIntervention", "false");
        }

        return dto;
    }

    // Kept for compatibility but unused by main map logic now
    private MapPointDTO equipmentToMapPoint(Equipment equipment) {
        return equipmentToMapPoint(equipment, null);
    }

    // Kept for compatibility
    private MapPointDTO interventionToMapPoint(Intervention intervention) {
        return null; // Deprecated for map visualization
    }
}