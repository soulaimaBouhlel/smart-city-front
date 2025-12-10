package com.smartcity.controller;

import com.smartcity.model.*;
import com.smartcity.repository.InterventionRepository;
import com.smartcity.repository.EquipmentRepository;
import com.smartcity.repository.TechnicianRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/maintenance/interventions")
public class MaintenanceAdminInterventionController {

    @Autowired
    private InterventionRepository interventionRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private TechnicianRepository technicianRepository;

    @Autowired
    private com.smartcity.service.INotificationService notificationService; // Use interface

    // ==================== CREATE INTERVENTION (MaintenanceAdmin Version)
    // ====================

    @PostMapping
    @PreAuthorize("hasRole('MAINTENANCE_ADMIN')")
    public ResponseEntity<?> createIntervention(@Valid @RequestBody CreateInterventionRequest request) {
        try {
            // Validate equipment exists
            if (request.getEquipmentId() != null &&
                    !equipmentRepository.existsById(request.getEquipmentId())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Equipment not found with id: " + request.getEquipmentId()));
            }

            // Create intervention
            Intervention intervention = Intervention.builder()
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .interventionType(request.getInterventionType())
                    .priority(request.getPriority() != null ? request.getPriority().name() : null)
                    .status(Intervention.Status.PENDING.name())
                    .location(request.getLocation())
                    .address(request.getAddress())
                    .photoUrl(request.getPhotoUrl())
                    .equipmentId(request.getEquipmentId())
                    .assignedTechnicianId(request.getAssignedTechnicianId())
                    .priorityScore(calculatePriorityScore(request))
                    .createdDate(LocalDateTime.now())
                    .build();

            Intervention saved = interventionRepository.save(intervention);

            // Send notification if technician is assigned
            if (request.getAssignedTechnicianId() != null) {
                sendAssignmentNotification(saved, request.getAssignedTechnicianId());
            }

            // Log action
            System.out.println("[MAINTENANCE_ADMIN] Created intervention: " + saved.getInterventionId() +
                    " - " + saved.getTitle());

            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to create intervention: " + e.getMessage()));
        }
    }

    // ==================== GET OPERATIONS ====================

    @GetMapping
    @PreAuthorize("hasRole('MAINTENANCE_ADMIN')")
    public ResponseEntity<List<Intervention>> getAllInterventions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String equipmentId) {

        List<Intervention> interventions;

        if (status != null) {
            interventions = interventionRepository
                    .findByStatus(Intervention.Status.valueOf(status.toUpperCase()).name());
        } else if (priority != null) {
            interventions = interventionRepository
                    .findByPriority(Intervention.Priority.valueOf(priority.toUpperCase()).name());
        } else if (equipmentId != null) {
            interventions = interventionRepository.findByEquipmentId(equipmentId);
        } else {
            interventions = interventionRepository.findAll();
        }

        return ResponseEntity.ok(interventions);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MAINTENANCE_ADMIN')")
    public ResponseEntity<?> getIntervention(@PathVariable String id) {
        return interventionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/unassigned")
    @PreAuthorize("hasRole('MAINTENANCE_ADMIN')")
    public ResponseEntity<List<Intervention>> getUnassignedInterventions() {
        List<Intervention> unassigned = interventionRepository.findByAssignedTechnicianIdIsNull();
        return ResponseEntity.ok(unassigned);
    }

    // ==================== UPDATE OPERATIONS ====================

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MAINTENANCE_ADMIN')")
    public ResponseEntity<?> updateIntervention(
            @PathVariable String id,
            @Valid @RequestBody UpdateInterventionRequest request) {

        return interventionRepository.findById(id).map(existing -> {
            // Update fields
            if (request.getTitle() != null)
                existing.setTitle(request.getTitle());
            if (request.getDescription() != null)
                existing.setDescription(request.getDescription());
            if (request.getInterventionType() != null)
                existing.setInterventionType(request.getInterventionType());
            if (request.getPriority() != null) {
                existing.setPriority(request.getPriority().name());
                existing.setPriorityScore(calculatePriorityScore(existing));
            }
            if (request.getStatus() != null) {
                existing.setStatus(request.getStatus().name());
                if (request.getStatus() == Intervention.Status.COMPLETED && existing.getCompletedDate() == null) {
                    existing.setCompletedDate(LocalDateTime.now());
                }
            }
            if (request.getPhotoUrl() != null)
                existing.setPhotoUrl(request.getPhotoUrl());

            Intervention updated = interventionRepository.save(existing);

            // Log update
            System.out.println("[MAINTENANCE_ADMIN] Updated intervention: " + id);

            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ==================== TECHNICIAN ASSIGNMENT ====================

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasRole('MAINTENANCE_ADMIN')")
    public ResponseEntity<?> assignTechnician(
            @PathVariable String id,
            @RequestBody AssignTechnicianRequest request) {

        return interventionRepository.findById(id).map(intervention -> {
            // Validate technician exists
            if (!technicianRepository.existsById(request.getTechnicianId())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Technician not found with id: " + request.getTechnicianId()));
            }

            // Update intervention
            intervention.setAssignedTechnicianId(request.getTechnicianId());
            intervention.setStatus(Intervention.Status.IN_PROGRESS.name());

            Intervention saved = interventionRepository.save(intervention);

            // Send notification to technician
            sendAssignmentNotification(saved, request.getTechnicianId());

            // Log assignment
            System.out.println("[MAINTENANCE_ADMIN] Assigned technician " + request.getTechnicianId() +
                    " to intervention " + id);

            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/unassign")
    @PreAuthorize("hasRole('MAINTENANCE_ADMIN')")
    public ResponseEntity<?> unassignTechnician(@PathVariable String id) {
        return interventionRepository.findById(id).map(intervention -> {
            String previousTechnicianId = intervention.getAssignedTechnicianId();

            intervention.setAssignedTechnicianId(null);
            if (Intervention.Status.IN_PROGRESS.name().equals(intervention.getStatus())) {
                intervention.setStatus(Intervention.Status.PENDING.name());
            }

            Intervention saved = interventionRepository.save(intervention);

            // Log unassignment
            System.out.println("[MAINTENANCE_ADMIN] Unassigned technician " + previousTechnicianId +
                    " from intervention " + id);

            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ==================== STATUS MANAGEMENT ====================

    @PatchMapping("/{id}/start")
    @PreAuthorize("hasRole('MAINTENANCE_ADMIN')")
    public ResponseEntity<?> startIntervention(@PathVariable String id) {
        return interventionRepository.findById(id).map(intervention -> {
            intervention.setStatus(Intervention.Status.IN_PROGRESS.name());
            Intervention saved = interventionRepository.save(intervention);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('MAINTENANCE_ADMIN')")
    public ResponseEntity<?> completeIntervention(
            @PathVariable String id,
            @RequestBody CompleteInterventionRequest request) {

        return interventionRepository.findById(id).map(intervention -> {
            intervention.setStatus(Intervention.Status.COMPLETED.name());
            intervention.setCompletedDate(LocalDateTime.now());

            if (request.getCompletionNotes() != null) {
                // Store completion notes (you might want to add a field for this)
                System.out.println("Completion notes for " + id + ": " + request.getCompletionNotes());
            }
            if (request.getFinalPhotoUrl() != null) {
                intervention.setPhotoUrl(request.getFinalPhotoUrl());
            }

            Intervention saved = interventionRepository.save(intervention);

            // Notify relevant parties
            sendCompletionNotification(saved);

            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('MAINTENANCE_ADMIN')")
    public ResponseEntity<?> cancelIntervention(
            @PathVariable String id,
            @RequestParam(required = false) String reason) {

        return interventionRepository.findById(id).map(intervention -> {
            intervention.setStatus(Intervention.Status.CANCELLED.name());

            if (reason != null) {
                System.out.println("Cancellation reason for " + id + ": " + reason);
            }

            Intervention saved = interventionRepository.save(intervention);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ==================== STATISTICS ====================

    @GetMapping("/stats")
    @PreAuthorize("hasRole('MAINTENANCE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Count by status
        for (Intervention.Status status : Intervention.Status.values()) {
            stats.put(status.name().toLowerCase(),
                    interventionRepository.findByStatus(status.name()).size());
        }

        // Count by priority
        Map<String, Integer> priorityStats = new HashMap<>();
        for (Intervention.Priority priority : Intervention.Priority.values()) {
            priorityStats.put(priority.name(),
                    interventionRepository.findByPriority(priority.name()).size());
        }
        stats.put("byPriority", priorityStats);

        // Total count
        stats.put("total", interventionRepository.count());

        // Recent interventions (last 7 days)
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<Intervention> recent = interventionRepository.findAll().stream()
                .filter(i -> i.getCreatedDate() != null && i.getCreatedDate().isAfter(weekAgo))
                .toList();
        stats.put("recentLast7Days", recent.size());

        return ResponseEntity.ok(stats);
    }

    // ==================== SEARCH ====================

    @GetMapping("/search")
    @PreAuthorize("hasRole('MAINTENANCE_ADMIN')")
    public ResponseEntity<List<Intervention>> searchInterventions(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority) {

        List<Intervention> allInterventions = interventionRepository.findAll();
        List<Intervention> results = new ArrayList<>();

        for (Intervention intervention : allInterventions) {
            boolean matches = true;

            // Search in title and description
            if (query != null && !query.isEmpty()) {
                String searchQuery = query.toLowerCase();
                boolean titleMatch = intervention.getTitle() != null &&
                        intervention.getTitle().toLowerCase().contains(searchQuery);
                boolean descMatch = intervention.getDescription() != null &&
                        intervention.getDescription().toLowerCase().contains(searchQuery);
                matches = matches && (titleMatch || descMatch);
            }

            // Filter by type
            if (type != null && !type.isEmpty()) {
                matches = matches && intervention.getInterventionType() != null &&
                        intervention.getInterventionType().equalsIgnoreCase(type);
            }

            // Filter by status
            if (status != null && !status.isEmpty()) {
                matches = matches && intervention.getStatus() != null &&
                        intervention.getStatus().equalsIgnoreCase(status.toUpperCase());
            }

            // Filter by priority
            if (priority != null && !priority.isEmpty()) {
                matches = matches && intervention.getPriority() != null &&
                        intervention.getPriority().equalsIgnoreCase(priority.toUpperCase());
            }

            if (matches) {
                results.add(intervention);
            }
        }

        return ResponseEntity.ok(results);
    }

    // ==================== BULK OPERATIONS ====================

    @PostMapping("/bulk-create")
    @PreAuthorize("hasRole('MAINTENANCE_ADMIN')")
    public ResponseEntity<?> bulkCreateInterventions(@Valid @RequestBody List<CreateInterventionRequest> requests) {
        List<Intervention> created = new ArrayList<>();

        for (CreateInterventionRequest request : requests) {
            try {
                Intervention intervention = Intervention.builder()
                        .title(request.getTitle())
                        .description(request.getDescription())
                        .interventionType(request.getInterventionType())
                        .priority(request.getPriority() != null ? request.getPriority().name() : null)
                        .status(Intervention.Status.PENDING.name())
                        .location(request.getLocation())
                        .address(request.getAddress())
                        .equipmentId(request.getEquipmentId())
                        .priorityScore(calculatePriorityScore(request))
                        .createdDate(LocalDateTime.now())
                        .build();

                created.add(interventionRepository.save(intervention));
            } catch (Exception e) {
                System.err.println("Failed to create intervention: " + request.getTitle() + " - " + e.getMessage());
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "created", created.size(),
                        "totalRequested", requests.size(),
                        "interventions", created));
    }

    // ==================== HELPER METHODS ====================

    private Integer calculatePriorityScore(CreateInterventionRequest request) {
        int score = 0;

        if (request.getPriority() != null) {
            switch (request.getPriority()) {
                case CRITICAL:
                    score += 100;
                    break;
                case HIGH:
                    score += 75;
                    break;
                case MEDIUM:
                    score += 50;
                    break;
                case LOW:
                    score += 25;
                    break;
            }
        }

        if (request.getInterventionType() != null &&
                (request.getInterventionType().toLowerCase().contains("emergency") ||
                        request.getInterventionType().toLowerCase().contains("urgent"))) {
            score += 30;
        }

        return score;
    }

    private Integer calculatePriorityScore(Intervention intervention) {
        int score = 0;

        if (intervention.getPriority() != null) {
            try {
                switch (Intervention.Priority.valueOf(intervention.getPriority())) {
                    case CRITICAL:
                        score += 100;
                        break;
                    case HIGH:
                        score += 75;
                        break;
                    case MEDIUM:
                        score += 50;
                        break;
                    case LOW:
                        score += 25;
                        break;
                }
            } catch (IllegalArgumentException e) {
                // Handle invalid priority string
            }
        }

        if (intervention.getInterventionType() != null &&
                (intervention.getInterventionType().toLowerCase().contains("emergency") ||
                        intervention.getInterventionType().toLowerCase().contains("urgent"))) {
            score += 30;
        }

        return score;
    }

    private void sendAssignmentNotification(Intervention intervention, String technicianId) {
        try {
            // Use the correct Notification builder from your model
            Notification notification = Notification.builder()
                    .id(UUID.randomUUID().toString())
                    .title("New Intervention Assigned")
                    .message("You have been assigned to intervention: " + intervention.getTitle())
                    .type(Notification.NotificationType.SYSTEM) // Changed from ASSIGNMENT to SYSTEM
                    .recipientId(technicianId)
                    .timestamp(LocalDateTime.now())
                    .build();

            notificationService.sendNotification(notification);
        } catch (Exception e) {
            System.err.println("Failed to send assignment notification: " + e.getMessage());
        }
    }

    private void sendCompletionNotification(Intervention intervention) {
        // Notify the assigned technician and maintenance admin
        if (intervention.getAssignedTechnicianId() != null) {
            try {
                Notification notification = Notification.builder()
                        .id(UUID.randomUUID().toString())
                        .title("Intervention Completed")
                        .message("Intervention '" + intervention.getTitle() + "' has been marked as completed")
                        .type(Notification.NotificationType.SYSTEM)
                        .recipientId(intervention.getAssignedTechnicianId())
                        .timestamp(LocalDateTime.now())
                        .build();

                notificationService.sendNotification(notification);
            } catch (Exception e) {
                System.err.println("Failed to send completion notification: " + e.getMessage());
            }
        }

        System.out.println("Intervention completed: " + intervention.getInterventionId());
    }

    // ==================== DTO CLASSES ====================

    public static class CreateInterventionRequest {
        private String title;
        private String description;
        private String interventionType;
        private Intervention.Priority priority;
        private org.springframework.data.mongodb.core.geo.GeoJsonPoint location;
        private String address;
        private String photoUrl;
        private String equipmentId;
        private String assignedTechnicianId;

        // Getters and setters
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getInterventionType() {
            return interventionType;
        }

        public void setInterventionType(String interventionType) {
            this.interventionType = interventionType;
        }

        public Intervention.Priority getPriority() {
            return priority;
        }

        public void setPriority(Intervention.Priority priority) {
            this.priority = priority;
        }

        public org.springframework.data.mongodb.core.geo.GeoJsonPoint getLocation() {
            return location;
        }

        public void setLocation(org.springframework.data.mongodb.core.geo.GeoJsonPoint location) {
            this.location = location;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getPhotoUrl() {
            return photoUrl;
        }

        public void setPhotoUrl(String photoUrl) {
            this.photoUrl = photoUrl;
        }

        public String getEquipmentId() {
            return equipmentId;
        }

        public void setEquipmentId(String equipmentId) {
            this.equipmentId = equipmentId;
        }

        public String getAssignedTechnicianId() {
            return assignedTechnicianId;
        }

        public void setAssignedTechnicianId(String assignedTechnicianId) {
            this.assignedTechnicianId = assignedTechnicianId;
        }
    }

    public static class UpdateInterventionRequest {
        private String title;
        private String description;
        private String interventionType;
        private Intervention.Priority priority;
        private Intervention.Status status;
        private String photoUrl;

        // Getters and setters
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getInterventionType() {
            return interventionType;
        }

        public void setInterventionType(String interventionType) {
            this.interventionType = interventionType;
        }

        public Intervention.Priority getPriority() {
            return priority;
        }

        public void setPriority(Intervention.Priority priority) {
            this.priority = priority;
        }

        public Intervention.Status getStatus() {
            return status;
        }

        public void setStatus(Intervention.Status status) {
            this.status = status;
        }

        public String getPhotoUrl() {
            return photoUrl;
        }

        public void setPhotoUrl(String photoUrl) {
            this.photoUrl = photoUrl;
        }
    }

    public static class AssignTechnicianRequest {
        private String technicianId;
        private String assignmentNotes;

        // Getters and setters
        public String getTechnicianId() {
            return technicianId;
        }

        public void setTechnicianId(String technicianId) {
            this.technicianId = technicianId;
        }

        public String getAssignmentNotes() {
            return assignmentNotes;
        }

        public void setAssignmentNotes(String assignmentNotes) {
            this.assignmentNotes = assignmentNotes;
        }
    }

    public static class CompleteInterventionRequest {
        private String completionNotes;
        private String finalPhotoUrl;

        // Getters and setters
        public String getCompletionNotes() {
            return completionNotes;
        }

        public void setCompletionNotes(String completionNotes) {
            this.completionNotes = completionNotes;
        }

        public String getFinalPhotoUrl() {
            return finalPhotoUrl;
        }

        public void setFinalPhotoUrl(String finalPhotoUrl) {
            this.finalPhotoUrl = finalPhotoUrl;
        }
    }
}