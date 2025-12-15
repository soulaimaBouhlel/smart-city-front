package com.smartcity.dto;

import lombok.Data;

@Data
public class LocationUpdateRequest {
    private double lat;
    private double lng;
}