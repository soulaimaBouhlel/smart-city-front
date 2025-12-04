package com.smartcity.controller;

import com.smartcity.model.Intervention;
import com.smartcity.service.CitizenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/citizens")
@CrossOrigin(origins = "*")
public class CitizenController {

    @Autowired
    private CitizenService citizenService;

    @PostMapping("/report-issue")
    public ResponseEntity<Intervention> reportIssue(
            @RequestParam String citizenId,
            @RequestParam String description,
            @RequestParam String interventionType,
            @RequestParam double longitude,
            @RequestParam double latitude,
            @RequestParam(required = false) String address) {

        GeoJsonPoint location = new GeoJsonPoint(longitude, latitude);
        Intervention intervention = citizenService.submitIssue(citizenId, description, interventionType, location,
                address);
        return ResponseEntity.ok(intervention);
    }

    @GetMapping("/test")
    public String test() {
        return "Citizen API is working!";
    }
}