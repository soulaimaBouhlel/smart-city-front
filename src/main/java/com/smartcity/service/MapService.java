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

/**
 * Simplified MapService for geospatial queries and routing.
 * Clean, focused service with minimal responsibilities.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MapService {

    private final EquipmentRepository equipmentRepository;
    private final TechnicianRepository technicianRepository;
    private final InterventionRepository interventionRepository;
    private final OsrmService osrmService;

    // =========================================================================
    // Core API Methods (Phase 3 Requirements)
    // =========================================================================

    /**
     * Get all equipment.
     * 
     * @return list of all equipment
     */
    public List<Equipment> getAllEquipments() {
        log.info("Retrieving all equipment");
        return equipmentRepository.findAll();
    }

    /**
     * Get all technicians.
     * 
     * @return list of all technicians
     */
    public List<Technician> getAllTechnicians() {
        log.info("Retrieving all technicians");
        return technicianRepository.findAll();
    }

    /**
     * Get nearest technicians to a given location within a maximum distance.
     * 
     * @param lng           longitude of the center point
     * @param lat           latitude of the center point
     * @param maxDistanceKm maximum distance in kilometers
     * @return list of technicians within the distance, sorted by proximity
     */
    public List<Technician> getNearestTechnicians(double lng, double lat, double maxDistanceKm) {
        Point location = new Point(lng, lat);
        Distance distance = new Distance(maxDistanceKm, Metrics.KILOMETERS);

        log.info("Finding technicians within {}km of ({}, {})", maxDistanceKm, lat, lng);
        List<Technician> technicians = technicianRepository.findByLocationNear(location, distance);
        log.info("Found {} technicians", technicians.size());

        return technicians;
    }

    /**
     * Compute route between two points using OSRM.
     * 
     * @param fromLng starting longitude
     * @param fromLat starting latitude
     * @param toLng   destination longitude
     * @param toLat   destination latitude
     * @return OSRM route response with distance and duration
     */
    public OsrmRouteResponse computeRoute(double fromLng, double fromLat, double toLng, double toLat) {
        log.info("Computing route from ({}, {}) to ({}, {})", fromLat, fromLng, toLat, toLng);
        return osrmService.getRoute(fromLng, fromLat, toLng, toLat);
    }

    // =========================================================================
    // MapPointDTO Helper Methods (for backward compatibility with MapController)
    // =========================================================================

    /**
     * Get all equipment as map points.
     */
    public List<MapPointDTO> getAllEquipmentPoints() {
        List<Equipment> equipments = equipmentRepository.findAll();
        log.info("Retrieved {} equipment records", equipments.size());

        return equipments.stream()
                .map(this::equipmentToMapPoint)
                .collect(Collectors.toList());
    }

    /**
     * Get all interventions as map points.
     */
    public List<MapPointDTO> getAllInterventionPoints() {
        List<Intervention> interventions = interventionRepository.findAll();
        log.info("Retrieved {} intervention records", interventions.size());

        return interventions.stream()
                .map(this::interventionToMapPoint)
                .collect(Collectors.toList());
    }

    /**
     * Get all map points (equipment + interventions combined).
     */
    public List<MapPointDTO> getAllMapPoints() {
        List<MapPointDTO> allPoints = new ArrayList<>();
        allPoints.addAll(getAllEquipmentPoints());
        allPoints.addAll(getAllInterventionPoints());

        log.info("Retrieved {} total map points", allPoints.size());
        return allPoints;
    }

    /**
     * Get nearby points within a radius of given coordinates.
     * 
     * @param latitude     center latitude
     * @param longitude    center longitude
     * @param radiusMeters search radius in meters
     * @return list of map points within radius
     */
    public List<MapPointDTO> getNearbyPoints(double latitude, double longitude, double radiusMeters) {
        Point location = new Point(longitude, latitude);
        Distance distance = new Distance(radiusMeters / 1000.0, Metrics.KILOMETERS);

        List<MapPointDTO> nearbyPoints = new ArrayList<>();

        // Find nearby equipment
        List<Equipment> nearbyEquipment = equipmentRepository.findByLocationNear(location, distance);
        nearbyPoints.addAll(nearbyEquipment.stream()
                .map(this::equipmentToMapPoint)
                .collect(Collectors.toList()));

        // Note: Interventions don't have geospatial queries yet, so we filter manually
        List<Intervention> allInterventions = interventionRepository.findAll();
        allInterventions.stream()
                .filter(intervention -> {
                    if (intervention.getLocation() == null)
                        return false;
                    double lng = intervention.getLocation().getX();
                    double lat = intervention.getLocation().getY();
                    Point interventionPoint = new Point(lng, lat);
                    // Simple distance check (not accurate but works for small distances)
                    double dx = interventionPoint.getX() - location.getX();
                    double dy = interventionPoint.getY() - location.getY();
                    double distanceKm = Math.sqrt(dx * dx + dy * dy) * 111.0; // rough conversion
                    return distanceKm <= (radiusMeters / 1000.0);
                })
                .map(this::interventionToMapPoint)
                .forEach(nearbyPoints::add);

        log.info("Found {} points within {} meters of ({}, {})",
                nearbyPoints.size(), radiusMeters, latitude, longitude);

        return nearbyPoints;
    }

    // =========================================================================
    // Private Helper Methods
    // =========================================================================

    /**
     * Convert Equipment to MapPointDTO.
     */
    private MapPointDTO equipmentToMapPoint(Equipment equipment) {
        if (equipment.getLocation() == null) {
            return null;
        }

        MapPointDTO dto = MapPointDTO.builder()
                .id(equipment.getId())
                .type("equipment")
                .label(equipment.getName())
                .status(equipment.getStatus().toString())
                .latitude(equipment.getLocation().getY())
                .longitude(equipment.getLocation().getX())
                .build();

        // Add equipment-specific metadata
        dto.addMetadata("equipmentType", equipment.getType());
        if (equipment.getLastMaintenance() != null) {
            dto.addMetadata("lastMaintenance", equipment.getLastMaintenance().toString());
        }
        if (equipment.getAddress() != null) {
            dto.addMetadata("address", equipment.getAddress());
        }

        return dto;
    }

    /**
     * Convert Intervention to MapPointDTO.
     */
    private MapPointDTO interventionToMapPoint(Intervention intervention) {
        if (intervention.getLocation() == null) {
            return null;
        }

        MapPointDTO dto = MapPointDTO.builder()
                .id(intervention.getInterventionId())
                .type("intervention")
                .label(intervention.getTitle())
                .status(intervention.getStatus().toString())
                .latitude(intervention.getLocation().getY())
                .longitude(intervention.getLocation().getX())
                .build();

        // Add intervention-specific metadata
        dto.addMetadata("priority", intervention.getPriority().toString());
        dto.addMetadata("interventionType", intervention.getInterventionType());
        dto.addMetadata("createdDate", intervention.getCreatedDate().toString());

        if (intervention.getDescription() != null) {
            dto.addMetadata("description", intervention.getDescription());
        }
        if (intervention.getCompletedDate() != null) {
            dto.addMetadata("completedDate", intervention.getCompletedDate().toString());
        }
        if (intervention.getPriorityScore() != null) {
            dto.addMetadata("priorityScore", intervention.getPriorityScore());
        }
        if (intervention.getPhotoUrl() != null) {
            dto.addMetadata("photoUrl", intervention.getPhotoUrl());
        }
        if (intervention.getAddress() != null) {
            dto.addMetadata("address", intervention.getAddress());
        }

        return dto;
    }
}
