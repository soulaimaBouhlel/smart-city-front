package com.smartcity.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedded document representing a geographic location.
 * Used by Equipment and Intervention entities.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    private Double latitude; // -90 to 90

    private Double longitude; // -180 to 180

    private String address;

    /**
     * Calculate distance to another location using Haversine formula.
     * * @param other the other location
     * @return distance in meters
     */
    public double distanceTo(Location other) {
        final int EARTH_RADIUS = 6371000; // meters

        double lat1Rad = Math.toRadians(this.latitude);
        double lat2Rad = Math.toRadians(other.latitude);
        double deltaLat = Math.toRadians(other.latitude - this.latitude);
        double deltaLon = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}