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
        List<Intervention> interventions = interventionRepository.findAll();
        log.info("Retrieved {} intervention records", interventions.size());

        return interventions.stream()
                .map(this::interventionToMapPoint)
                .filter(dto -> dto != null) // Filter out nulls
                .collect(Collectors.toList());
    }

    /**
     * Get all map points (equipment + interventions combined)
     */
    public List<MapPointDTO> getAllMapPoints() {
        List<MapPointDTO> allPoints = new ArrayList<>();
        allPoints.addAll(getAllEquipmentPoints());
        allPoints.addAll(getAllInterventionPoints());

        log.info("Retrieved {} total map points", allPoints.size());
        return allPoints;
    }

    /**
     * Get nearby points within a radius of given coordinates
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

        // Find nearby interventions
        List<Intervention> allInterventions = interventionRepository.findAll();
        allInterventions.stream()
                .filter(intervention -> {
                    // Get location from equipment
                    if (intervention.getEquipment() == null) {
                        return false;
                    }
                    
                    // Check if equipment has location
                    if (intervention.getEquipment().getLocation() == null) {
                        return false;
                    }
                    
                    double lng = intervention.getEquipment().getLocation().getX();
                    double lat = intervention.getEquipment().getLocation().getY();
                    Point interventionPoint = new Point(lng, lat);
                    
                    // Simple distance check
                    double dx = interventionPoint.getX() - location.getX();
                    double dy = interventionPoint.getY() - location.getY();
                    double distanceKm = Math.sqrt(dx * dx + dy * dy) * 111.0;
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
     * Convert Equipment to MapPointDTO
     */
    private MapPointDTO equipmentToMapPoint(Equipment equipment) {
        if (equipment.getLocation() == null) {
            return null;
        }

        MapPointDTO dto = MapPointDTO.builder()
                .id(equipment.getId())
                .type("equipment")
                .label(equipment.getName())
                .status(equipment.getStatus() != null ? equipment.getStatus() : "UNKNOWN")
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

        return dto;
    }

    /**
     * Convert Intervention to MapPointDTO
     */
    private MapPointDTO interventionToMapPoint(Intervention intervention) {
        // Get location from equipment
        if (intervention.getEquipment() == null) {
            log.warn("Intervention {} has no equipment reference", intervention.getInterventionId());
            return null;
        }
        
        if (intervention.getEquipment().getLocation() == null) {
            log.warn("Equipment for intervention {} has no location", intervention.getInterventionId());
            return null;
        }

        // Use equipment location
        double latitude = intervention.getEquipment().getLocation().getY();
        double longitude = intervention.getEquipment().getLocation().getX();
        
        // Get address from equipment and convert Object to String
        String address = intervention.getEquipment().getAddress() != null ? 
                        intervention.getEquipment().getAddress().toString() : null;

        MapPointDTO dto = MapPointDTO.builder()
                .id(intervention.getInterventionId())
                .type("intervention")
                .label(intervention.getTitle())
                .status(intervention.getStatus() != null ? intervention.getStatus() : "UNKNOWN")
                .latitude(latitude)
                .longitude(longitude)
                .build();

        // Add intervention metadata
        if (intervention.getPriority() != null) {
            dto.addMetadata("priority", intervention.getPriority().toString());
        }
        dto.addMetadata("interventionType", intervention.getInterventionType());
        dto.addMetadata("createdDate", intervention.getCreatedDate().toString());
        
        // Add equipment info
        dto.addMetadata("equipmentId", intervention.getEquipmentId());
        dto.addMetadata("equipmentName", intervention.getEquipment().getName());
        dto.addMetadata("equipmentType", 
            intervention.getEquipment().getType() != null ? 
            intervention.getEquipment().getType().toString() : "UNKNOWN");

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
        if (address != null) {
            dto.addMetadata("address", address);
        }
        
        // Add technician info if assigned
        if (intervention.getAssignedTechnicianId() != null) {
            dto.addMetadata("assignedTechnicianId", intervention.getAssignedTechnicianId());
        }

        return dto;
    }
}