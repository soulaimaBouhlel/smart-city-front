package com.smartcity.service;

import com.smartcity.model.Technician;
import com.smartcity.model.Skill;
import com.smartcity.repository.TechnicianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TechnicianService {

    private final TechnicianRepository technicianRepository;
    private final INotificationService notificationService;

    public List<Technician> getAllTechnicians() {
        return technicianRepository.findAll();
    }

    public List<Technician> getAvailableTechnicians() {
        return technicianRepository.findByAvailableTrue();
    }

    public Optional<Technician> getTechnicianById(String technicianId) {
        return technicianRepository.findById(technicianId);
    }

    public Technician createTechnician(Technician technician) {
        Technician savedTechnician = technicianRepository.save(technician);

        // Send notification
        com.smartcity.model.Notification notification = com.smartcity.model.Notification.builder()
                .id(java.util.UUID.randomUUID().toString())
                .title("Technician Created")
                .message("New technician " + savedTechnician.getName() + " has been added to the system")
                .type(com.smartcity.model.Notification.NotificationType.SYSTEM)
                .recipientId("admin")
                .timestamp(java.time.LocalDateTime.now())
                .build();
        notificationService.sendNotification(notification);

        return savedTechnician;
    }

    public Technician updateTechnician(String technicianId, Technician technicianDetails) {
        return technicianRepository.findById(technicianId)
                .map(technician -> {
                    technician.setName(technicianDetails.getName());
                    technician.setEmail(technicianDetails.getEmail());
                    technician.setPhone(technicianDetails.getPhone());
                    technician.setSkills(technicianDetails.getSkills());
                    technician.setLocation(technicianDetails.getLocation());
                    technician.setAvailable(technicianDetails.getAvailable());
                    technician.setCurrentWorkload(technicianDetails.getCurrentWorkload());
                    technician.setRating(technicianDetails.getRating());

                    return technicianRepository.save(technician);
                })
                .orElseThrow(() -> new RuntimeException("Technician not found with id: " + technicianId));
    }

    public void deleteTechnician(String technicianId) {
        technicianRepository.deleteById(technicianId);
    }

    public Technician updateTechnicianSkills(String technicianId, Set<Skill> skills) {
        return technicianRepository.findById(technicianId)
                .map(technician -> {
                    technician.setSkills(skills);
                    return technicianRepository.save(technician);
                })
                .orElseThrow(() -> new RuntimeException("Technician not found with id: " + technicianId));
    }

    public Technician updateAvailability(String technicianId, boolean available) {
        return technicianRepository.findById(technicianId)
                .map(technician -> {
                    technician.setAvailable(available);
                    return technicianRepository.save(technician);
                })
                .orElseThrow(() -> new RuntimeException("Technician not found with id: " + technicianId));
    }

    public Technician updateWorkload(String technicianId, int workload) {
        return technicianRepository.findById(technicianId)
                .map(technician -> {
                    technician.setCurrentWorkload(workload);
                    return technicianRepository.save(technician);
                })
                .orElseThrow(() -> new RuntimeException("Technician not found with id: " + technicianId));
    }

    public Technician updateLocation(String technicianId,
            org.springframework.data.mongodb.core.geo.GeoJsonPoint location) {
        return technicianRepository.findById(technicianId)
                .map(technician -> {
                    technician.setLocation(location);
                    return technicianRepository.save(technician);
                })
                .orElseThrow(() -> new RuntimeException("Technician not found with id: " + technicianId));
    }

    public Optional<Technician> getTechnicianByEmail(String email) {
        return technicianRepository.findByEmailIgnoreCase(email);
    }
}