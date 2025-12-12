package com.smartcity.controller;

import com.smartcity.model.Intervention;
import com.smartcity.service.CitizenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/citizens")
@CrossOrigin(origins = "*")
public class CitizenController {

    @Autowired
    private CitizenService citizenService;

    /**
     * NEW: Report issue for SPECIFIC EQUIPMENT
     */
    @PostMapping("/report-issue-for-equipment")
    public ResponseEntity<?> reportIssueForEquipment(
            @RequestParam String citizenId,
            @RequestParam String equipmentId,  // REQUIRED: which equipment is broken
            @RequestParam String description,
            @RequestParam String interventionType) {
        
        try {
            Intervention intervention = citizenService.submitIssueForEquipment(
                citizenId, equipmentId, description, interventionType);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Issue reported successfully",
                "intervention", intervention
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * KEEP for backward compatibility but update to use equipment
     */
    @PostMapping("/report-issue")
    public ResponseEntity<?> reportIssue(
            @RequestParam String citizenId,
            @RequestParam String equipmentId,  // ADD THIS PARAMETER
            @RequestParam String description,
            @RequestParam String interventionType) {
        
        return reportIssueForEquipment(citizenId, equipmentId, description, interventionType);
    }

    @GetMapping("/test")
    public String test() {
        return "Citizen API is working!";
    }
}