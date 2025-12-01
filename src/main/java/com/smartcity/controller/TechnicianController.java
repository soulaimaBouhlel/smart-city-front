package com.smartcity.controller;

import com.smartcity.model.Technician;
import com.smartcity.model.Skill;
import com.smartcity.service.TechnicianService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/technicians")
@CrossOrigin(origins = "*")
public class TechnicianController {

    @Autowired
    private TechnicianService technicianService;

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

    @PutMapping("/{technicianId}/skills")
    public ResponseEntity<Technician> updateSkills(@PathVariable String technicianId, 
                                                  @RequestBody Set<Skill> skills) {
        Technician updatedTechnician = technicianService.updateTechnicianSkills(technicianId, skills);
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

    @GetMapping("/test")
    public String test() {
        return "Technician API is working!";
    }
}