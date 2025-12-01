package com.smartcity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.smartcity.model.*;
import com.smartcity.repository.TechnicianRepository;
import java.util.*;

@Service
public class SmartSchedulerService {
    
    @Autowired
    private TechnicianRepository technicianRepository;
    
    @Autowired
    private GeminiAIService geminiAIService;
    
    /**
     * Get complete AI analysis for intervention
     */
    public GeminiAIService.InterventionAnalysis analyzeIntervention(String description, String interventionType) {
        return geminiAIService.analyzeIntervention(description, interventionType);
    }
    
    /**
     * Find best technician using AI contextual matching
     */
    public Technician findBestTechnician(Intervention intervention) {
        List<Technician> availableTechnicians = technicianRepository.findByAvailableTrue();
        return geminiAIService.findBestTechnician(intervention, availableTechnicians);
    }
    
    /**
     * Convert AI priority string to Intervention.Priority enum
     */
    public Intervention.Priority mapPriority(String aiPriority) {
        if (aiPriority == null) return Intervention.Priority.MEDIUM;
        
        switch (aiPriority.toUpperCase()) {
            case "CRITICAL": return Intervention.Priority.CRITICAL;
            case "HIGH": return Intervention.Priority.HIGH;
            case "MEDIUM": return Intervention.Priority.MEDIUM;
            case "LOW": return Intervention.Priority.LOW;
            default: return Intervention.Priority.MEDIUM;
        }
    }
}