package com.smartcity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Unified DTO for map markers representing both Equipment and Intervention
 * entities.
 * Used by the map visualization endpoints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapPointDTO {

    private String id;

    private String type; // "equipment" or "intervention"

    private String label;

    private String status;

    private Double latitude;

    private Double longitude;

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * Add metadata entry
     */
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }
}
