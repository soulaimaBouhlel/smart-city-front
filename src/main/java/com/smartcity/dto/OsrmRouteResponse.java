package com.smartcity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for OSRM route response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OsrmRouteResponse {
    private String code;
    private Double distance; // in meters
    private Double duration; // in seconds
    private String geometry; // optional route geometry
}
