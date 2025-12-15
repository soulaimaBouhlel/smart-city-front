package com.smartcity.controller;

import com.smartcity.model.Technician;
import com.smartcity.model.Skill;
import com.smartcity.service.TechnicianService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/technicians")
@CrossOrigin(origins = "*")
public class TechnicianController {

    @Autowired
    private TechnicianService technicianService;

    @Autowired
    private com.smartcity.repository.InterventionRepository interventionRepository;

    @Autowired
    private com.smartcity.service.InterventionService interventionService;

    @GetMapping
    public ResponseEntity<List<Technician>> getAllTechnicians() {
        List<Technician> technicians = technicianService.getAllTechnicians();
        return ResponseEntity.ok(technicians);
    }

    @GetMapping("/available")
    public ResponseEntity<List<Technician>> getAvailableTechnicians() {
        List<Technician> technicians = technicianService.getAvailableTechnicians();
        return ResponseEntity.ok(technicians);
    }

    @GetMapping("/{technicianId}")
    public ResponseEntity<Technician> getTechnicianById(@PathVariable String technicianId) {
        return technicianService.getTechnicianById(technicianId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Technician> createTechnician(@RequestBody Technician technician) {
        Technician createdTechnician = technicianService.createTechnician(technician);
        return ResponseEntity.ok(createdTechnician);
    }

    @PutMapping("/{technicianId}")
    public ResponseEntity<Technician> updateTechnician(@PathVariable String technicianId,
            @RequestBody Technician technicianDetails) {
        Technician updatedTechnician = technicianService.updateTechnician(technicianId, technicianDetails);
        return ResponseEntity.ok(updatedTechnician);
    }

    @DeleteMapping("/{technicianId}")
    public ResponseEntity<Void> deleteTechnician(@PathVariable String technicianId) {
        technicianService.deleteTechnician(technicianId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{technicianId}/skills")
    public ResponseEntity<?> updateSkills(@PathVariable String technicianId,
            @RequestBody com.smartcity.dto.SkillsUpdateRequest request) {

        // IDOR Protection
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String email = ((UserDetails) authentication.getPrincipal()).getUsername();
            currentUserId = technicianService.getTechnicianByEmail(email)
                    .map(Technician::getTechnicianId)
                    .orElse(null);
        }

        if (currentUserId == null || !currentUserId.equals(technicianId)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body("You are not authorized to update this technician's skills.");
        }

        Technician updatedTechnician = technicianService.updateTechnicianSkills(technicianId, request.getSkills());
        return ResponseEntity.ok(updatedTechnician);
    }

    @PutMapping("/{technicianId}/availability")
    public ResponseEntity<Technician> updateAvailability(@PathVariable String technicianId,
            @RequestParam boolean available) {
        Technician updatedTechnician = technicianService.updateAvailability(technicianId, available);
        return ResponseEntity.ok(updatedTechnician);
    }

    @PutMapping("/{technicianId}/workload")
    public ResponseEntity<Technician> updateWorkload(@PathVariable String technicianId,
            @RequestParam int workload) {
        Technician updatedTechnician = technicianService.updateWorkload(technicianId, workload);
        return ResponseEntity.ok(updatedTechnician);
    }

    @PatchMapping("/{technicianId}/location")
    public ResponseEntity<?> updateLocation(@PathVariable String technicianId,
            @RequestBody com.smartcity.dto.LocationUpdateRequest request) {

        // 1. IDOR Protection
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = null;

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String email = ((UserDetails) authentication.getPrincipal()).getUsername();
            currentUserId = technicianService.getTechnicianByEmail(email)
                    .map(Technician::getTechnicianId)
                    .orElse(null);
        }

        if (currentUserId == null || !currentUserId.equals(technicianId)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body("You are not authorized to update this technician's location.");
        }

        // 2. Validation
        double latitude = request.getLat();
        double longitude = request.getLng();

        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            return ResponseEntity.badRequest()
                    .body("Invalid coordinates. Latitude must be between -90 and 90, Longitude between -180 and 180.");
        }

        org.springframework.data.mongodb.core.geo.GeoJsonPoint location = new org.springframework.data.mongodb.core.geo.GeoJsonPoint(
                longitude, latitude);

        Technician updatedTechnician = technicianService.updateLocation(technicianId, location);
        return ResponseEntity.ok(updatedTechnician);
    }

    // ==================== INTERVENTION MANAGEMENT ====================

    @GetMapping("/me/interventions")
    public ResponseEntity<?> getMyInterventions() {
        String technicianId = getAuthenticatedTechnicianId();

        if (technicianId == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body("User not found or not a technician");
        }

        List<com.smartcity.model.Intervention> interventions = interventionRepository
                .findByAssignedTechnicianId(technicianId);
        return ResponseEntity.ok(interventions);
    }

    @PatchMapping("/interventions/{id}/status")
    public ResponseEntity<?> updateInterventionStatus(
            @PathVariable String id,
            @RequestBody java.util.Map<String, String> statusUpdate) {

        String newStatus = statusUpdate.get("status");
        if (newStatus == null) {
            return ResponseEntity.badRequest().body("Status is required");
        }

        String technicianId = getAuthenticatedTechnicianId();
        if (technicianId == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body("User not found or not a technician");
        }

        return interventionRepository.findById(id).map(intervention -> {
            // IDOR Check
            if (!technicianId.equals(intervention.getAssignedTechnicianId())) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                        .body("You are not authorized to update this intervention.");
            }

            // Validate Status Transition
            String currentStatus = intervention.getStatus();
            if (!isValidTransition(currentStatus, newStatus)) {
                return ResponseEntity.badRequest()
                        .body("Invalid status transition from " + currentStatus + " to " + newStatus);
            }

            java.util.Map<String, Object> result = interventionService.updateStatus(id, newStatus, technicianId);

            if ((boolean) result.get("success")) {
                return ResponseEntity.ok(result.get("intervention"));
            } else {
                return ResponseEntity.badRequest().body(result.get("message"));
            }

        }).orElse(ResponseEntity.notFound().build());
    }

    // ==================== HELPERS ====================

    private String getAuthenticatedTechnicianId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return null;
        }

        if (!(authentication.getPrincipal() instanceof UserDetails)) {
            return null;
        }

        String email = ((UserDetails) authentication.getPrincipal()).getUsername();

        return technicianService.getTechnicianByEmail(email)
                .map(Technician::getTechnicianId)
                .orElse(null);
    }

    private boolean isValidTransition(String currentStatus, String newStatus) {
        if (currentStatus == null)
            return false;

        currentStatus = currentStatus.toUpperCase();
        newStatus = newStatus.toUpperCase();

        if (currentStatus.equals("PENDING") && newStatus.equals("IN_PROGRESS"))
            return true;
        if (currentStatus.equals("IN_PROGRESS") && newStatus.equals("COMPLETED"))
            return true;

        return false;
    }

    @GetMapping("/test")
    public String test() {
        return "Technician API is working!";
    }
}