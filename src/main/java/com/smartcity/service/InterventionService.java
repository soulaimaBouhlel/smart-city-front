package com.smartcity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InterventionService {

    private final NotificationService notificationService;

    public void createIntervention(String citizenId, String description) {
        // Logic to create intervention...
        notificationService.notifyCitizen(citizenId, "Intervention created: " + description);
    }

    public void assignTechnician(String interventionId, String technicianId) {
        // Logic to assign technician...
        notificationService.notifyTechnician(technicianId, "You have been assigned to intervention: " + interventionId);
    }

    public void updateStatus(String interventionId, String status, String citizenId) {
        // Logic to update status...
        notificationService.notifyCitizen(citizenId, "Intervention " + interventionId + " status changed to: " + status);
    }
}
