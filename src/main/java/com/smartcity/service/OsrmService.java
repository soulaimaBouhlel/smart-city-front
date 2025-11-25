package com.smartcity.service;

import com.smartcity.model.Location;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * Service for integrating with OSRM (Open Source Routing Machine) API.
 * Provides routing and distance calculation functionality.
 */
@Slf4j
@Service
public class OsrmService {

    private static final String OSRM_BASE_URL = "https://router.project-osrm.org/route/v1/driving/";
    private final RestTemplate restTemplate;

    public OsrmService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Validate if coordinates are within valid ranges.
     */
    public boolean validateCoordinates(double latitude, double longitude) {
        return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
    }

    /**
     * Get road-based distance between two locations using OSRM.
     * 
     * @param from starting location
     * @param to   destination location
     * @return distance in meters, or null if route cannot be calculated
     */
    public Double getDistance(Location from, Location to) {
        if (!validateCoordinates(from.getLatitude(), from.getLongitude()) ||
                !validateCoordinates(to.getLatitude(), to.getLongitude())) {
            log.error("Invalid coordinates provided");
            return null;
        }

        try {
            String url = buildRouteUrl(from, to);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && "Ok".equals(response.get("code"))) {
                Map<String, Object>[] routes = (Map<String, Object>[]) response.get("routes");
                if (routes != null && routes.length > 0) {
                    Number distance = (Number) routes[0].get("distance");
                    return distance != null ? distance.doubleValue() : null;
                }
            }

            log.warn("No route found between locations");
            return null;

        } catch (RestClientException e) {
            log.error("Error calling OSRM API: {}", e.getMessage());
            // Fallback to straight-line distance
            return from.distanceTo(to);
        }
    }

    /**
     * Get full route information between two locations.
     * 
     * @param from starting location
     * @param to   destination location
     * @return route response map, or null if error
     */
    public Map<String, Object> getRoute(Location from, Location to) {
        if (!validateCoordinates(from.getLatitude(), from.getLongitude()) ||
                !validateCoordinates(to.getLatitude(), to.getLongitude())) {
            log.error("Invalid coordinates provided");
            return null;
        }

        try {
            String url = buildRouteUrl(from, to);
            return restTemplate.getForObject(url, Map.class);
        } catch (RestClientException e) {
            log.error("Error calling OSRM API: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Build OSRM route URL from two locations.
     */
    private String buildRouteUrl(Location from, Location to) {
        return String.format("%s%f,%f;%f,%f?overview=false",
                OSRM_BASE_URL,
                from.getLongitude(), from.getLatitude(),
                to.getLongitude(), to.getLatitude());
    }
}
