package com.smartcity.controller;

import com.smartcity.model.Intervention;
import com.smartcity.service.CitizenService;
import com.smartcity.dto.ReportIssueRequest;
import com.smartcity.repository.CitizenRepository;
import com.smartcity.repository.InterventionRepository;
import com.smartcity.util.JwtUtil;
import com.smartcity.model.Citizen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/citizens")
@CrossOrigin(origins = "*")
public class CitizenController {

    @Autowired
    private CitizenService citizenService;

    @Autowired
    private CitizenRepository citizenRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private InterventionRepository interventionRepository;

    /**
     * NEW: Report issue for SPECIFIC EQUIPMENT
     */
    @PostMapping("/report-issue")
    public ResponseEntity<?> reportIssue(@RequestBody ReportIssueRequest request) {

        // 1. Get authenticated user email from SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();
        System.out.println("DEBUG: Authenticated user email: " + email);

        // 2. Find citizen ID
        String citizenId = citizenRepository.findByEmail(email)
                .map(Citizen::getCitizenId)
                .orElseThrow(() -> new RuntimeException("Citizen not found for email: " + email));

        System.out.println("DEBUG: Resolved Citizen ID: " + citizenId);

        try {
            Intervention intervention = citizenService.submitIssueForEquipment(
                    citizenId, // Use the secure ID, ignore request.getCitizenId()
                    request.getEquipmentId(),
                    request.getDescription(),
                    request.getInterventionType());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Issue reported successfully",
                    "intervention", intervention));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()));
        }
    }

    @GetMapping("/interventions")
    public ResponseEntity<?> getMyInterventions() {
        // 1. Get authenticated user email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // 2. Find citizen ID
        String citizenId = citizenRepository.findByEmail(email)
                .map(Citizen::getCitizenId)
                .orElseThrow(() -> new RuntimeException("Citizen not found"));

        // 3. Get interventions
        List<Intervention> interventions = citizenService.getInterventionsForCitizen(citizenId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "interventions", interventions));
    }

    @GetMapping("/interventions/{id}")
    public ResponseEntity<Intervention> getIntervention(@PathVariable String id) {
        return interventionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/test")
    public String test() {
        return "Citizen API is working!";
    }
}