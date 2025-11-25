package com.smartcity.controller;

import com.smartcity.dto.MapPointDTO;
import com.smartcity.service.MapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for map-related endpoints.
 * Provides location-based data for Equipment and Interventions.
 */
@Slf4j
@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allow CORS for testing
public class MapController {

    private final MapService mapService;

    /**
     * GET /api/map/equipments
     * Returns all equipment locations as map points.
     */
    @GetMapping("/equipments")
    public ResponseEntity<List<MapPointDTO>> getAllEquipments() {
        log.info("GET /api/map/equipments - Fetching all equipment locations");
        List<MapPointDTO> equipments = mapService.getAllEquipmentPoints();
        return ResponseEntity.ok(equipments);
    }

    /**
     * GET /api/map/interventions
     * Returns all intervention locations as map points.
     */
    @GetMapping("/interventions")
    public ResponseEntity<List<MapPointDTO>> getAllInterventions() {
        log.info("GET /api/map/interventions - Fetching all intervention locations");
        List<MapPointDTO> interventions = mapService.getAllInterventionPoints();
        return ResponseEntity.ok(interventions);
    }

    /**
     * GET /api/map/all
     * Returns all map points (equipment + interventions combined).
     */
    @GetMapping("/all")
    public ResponseEntity<List<MapPointDTO>> getAllMapPoints() {
        log.info("GET /api/map/all - Fetching all map points");
        List<MapPointDTO> allPoints = mapService.getAllMapPoints();
        return ResponseEntity.ok(allPoints);
    }

    /**
     * GET /api/map/nearby?lat={lat}&lng={lng}&radius={radius}
     * Returns map points within the specified radius of the given coordinates.
     * 
     * @param lat    latitude of center point
     * @param lng    longitude of center point
     * @param radius search radius in meters
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<MapPointDTO>> getNearbyPoints(
            @RequestParam("lat") double lat,
            @RequestParam("lng") double lng,
            @RequestParam("radius") double radius) {

        log.info("GET /api/map/nearby - lat={}, lng={}, radius={}", lat, lng, radius);

        // Validate coordinates
        if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            log.error("Invalid coordinates: lat={}, lng={}", lat, lng);
            return ResponseEntity.badRequest().build();
        }

        // Validate radius
        if (radius <= 0 || radius > 50000) { // Max 50km
            log.error("Invalid radius: {}", radius);
            return ResponseEntity.badRequest().build();
        }

        List<MapPointDTO> nearbyPoints = mapService.getNearbyPoints(lat, lng, radius);
        return ResponseEntity.ok(nearbyPoints);
    }
}
