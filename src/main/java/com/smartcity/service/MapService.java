package com.smartcity.service;

import com.smartcity.dto.MapPointDTO;
import com.smartcity.model.Equipment;
import com.smartcity.model.Intervention;
import com.smartcity.model.Location;
import com.smartcity.repository.EquipmentRepository;
import com.smartcity.repository.InterventionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for aggregating and transforming map data from Equipment and
 * Intervention entities.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MapService {

    private final EquipmentRepository equipmentRepository;
    private final InterventionRepository interventionRepository;

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
        Location center = Location.builder()
                .latitude(latitude)
                .longitude(longitude)
                .build();

        List<MapPointDTO> allPoints = getAllMapPoints();

        List<MapPointDTO> nearbyPoints = allPoints.stream()
                .filter(point -> {
                    Location pointLocation = Location.builder()
                            .latitude(point.getLatitude())
                            .longitude(point.getLongitude())
                            .build();
                    double distance = center.distanceTo(pointLocation);

                    // Add distance to metadata
                    point.addMetadata("distance", distance);

                    return distance <= radiusMeters;
                })
                .collect(Collectors.toList());

        log.info("Found {} points within {} meters of ({}, {})",
                nearbyPoints.size(), radiusMeters, latitude, longitude);

        return nearbyPoints;
    }

    /**
     * Convert Equipment to MapPointDTO.
     */
    private MapPointDTO equipmentToMapPoint(Equipment equipment) {
        MapPointDTO dto = MapPointDTO.builder()
                .id(equipment.getId())
                .type("equipment")
                .label(equipment.getName())
                .status(equipment.getStatus().toString())
                .latitude(equipment.getLocation().getLatitude())
                .longitude(equipment.getLocation().getLongitude())
                .build();

        // Add equipment-specific metadata
        dto.addMetadata("equipmentType", equipment.getType());
        if (equipment.getLastMaintenance() != null) {
            dto.addMetadata("lastMaintenance", equipment.getLastMaintenance().toString());
        }
        if (equipment.getLocation().getAddress() != null) {
            dto.addMetadata("address", equipment.getLocation().getAddress());
        }

        return dto;
    }

    /**
     * Convert Intervention to MapPointDTO.
     */
    private MapPointDTO interventionToMapPoint(Intervention intervention) {
        MapPointDTO dto = MapPointDTO.builder()
                .id(intervention.getInterventionId())
                .type("intervention")
                .label(intervention.getTitle())
                .status(intervention.getStatus().toString())
                .latitude(intervention.getLocation().getLatitude())
                .longitude(intervention.getLocation().getLongitude())
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
        if (intervention.getLocation().getAddress() != null) {
            dto.addMetadata("address", intervention.getLocation().getAddress());
        }

        return dto;
    }
}
