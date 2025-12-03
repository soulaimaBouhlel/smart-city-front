package com.smartcity.service;

import com.smartcity.dto.OsrmRouteResponse;
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
     * Get route between two points.
     * 
     * @param fromLng starting longitude
     * @param fromLat starting latitude
     * @param toLng   destination longitude
     * @param toLat   destination latitude
     * @return OsrmRouteResponse with route details
     */
    public OsrmRouteResponse getRoute(double fromLng, double fromLat, double toLng, double toLat) {
        if (!validateCoordinates(fromLat, fromLng) || !validateCoordinates(toLat, toLng)) {
            log.error("Invalid coordinates provided");
            return OsrmRouteResponse.builder()
                    .code("InvalidCoordinates")
                    .build();
        }

        try {
            String url = buildRouteUrl(fromLng, fromLat, toLng, toLat);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && "Ok".equals(response.get("code"))) {
                Map<String, Object>[] routes = (Map<String, Object>[]) response.get("routes");
                if (routes != null && routes.length > 0) {
                    Number distance = (Number) routes[0].get("distance");
                    Number duration = (Number) routes[0].get("duration");

                    return OsrmRouteResponse.builder()
                            .code("Ok")
                            .distance(distance != null ? distance.doubleValue() : null)
                            .duration(duration != null ? duration.doubleValue() : null)
                            .build();
                }
            }

            log.warn("No route found between locations");
            return OsrmRouteResponse.builder()
                    .code("NoRoute")
                    .build();

        } catch (RestClientException e) {
            log.error("Error calling OSRM API: {}", e.getMessage());
            return OsrmRouteResponse.builder()
                    .code("Error")
                    .build();
        }
    }

    /**
     * Build OSRM route URL from coordinates.
     */
    private String buildRouteUrl(double fromLng, double fromLat, double toLng, double toLat) {
        return String.format("%s%f,%f;%f,%f?overview=false",
                OSRM_BASE_URL,
                fromLng, fromLat,
                toLng, toLat);
    }
}
