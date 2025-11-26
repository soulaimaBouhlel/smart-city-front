package com.smartcity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.smartcity.service.SmartSchedulerService;
import com.smartcity.model.*;
import java.util.*;

@RestController
@RequestMapping("/api/scheduler")
public class SchedulerController {
    
    @Autowired
    private SmartSchedulerService smartSchedulerService;
    
    @GetMapping("/test")
    public String testScheduler() {
        return "<h1>🚀 Smart Scheduler API</h1>" +
               "<p>Available endpoints:</p>" +
               "<ul>" +
               "<li><a href='/api/scheduler/test-urgency'>/api/scheduler/test-urgency</a> - Test urgency analysis</li>" +
               "<li><a href='/api/scheduler/test-assignment'>/api/scheduler/test-assignment</a> - Test technician assignment</li>" +
               "<li><a href='/api/scheduler/analyze?description=problem'>/api/scheduler/analyze?description=PROBLEM</a> - Analyze specific problem</li>" +
               "</ul>";
    }
    
    @GetMapping("/test-urgency")
    public String testUrgencyAnalysis() {
        StringBuilder result = new StringBuilder();
        result.append("<pre style='font-family: Arial; font-size: 14px; line-height: 1.5;'>");
        result.append("🤖 SMART URGENCY ANALYSIS TEST\n\n");
        
        String[] testCases = {
            "CAR ACCIDENT WITH FIRE - PEOPLE TRAPPED - EMERGENCY!!!",
            "Broken traffic light at school crossing, children at risk",
            "Water pipe burst flooding main street URGENT",
            "Park bench needs painting next week",
            "Routine street light maintenance",
            "Gas leak detected, evacuation needed IMMEDIATELY",
            "Nice weather today, everything working fine"
        };
        
        for (int i = 0; i < testCases.length; i++) {
            String problem = testCases[i];
            int urgency = smartSchedulerService.analyzeUrgency(problem);
            
            result.append(String.format(
                "=== TEST %d ===\n" +
                "Problem: %s\n" +
                "Urgency Score: %d/10\n" +
                "Status: ✅ SMART ANALYSIS\n\n",
                i + 1, problem, urgency
            ));
        }
        
        result.append("</pre>");
        return result.toString();
    }
    
    @GetMapping("/test-assignment")
    public String testTechnicianAssignment() {
        StringBuilder result = new StringBuilder();
        result.append("<pre style='font-family: Arial; font-size: 14px; line-height: 1.5;'>");
        result.append("👨‍🔧 SMART TECHNICIAN ASSIGNMENT TEST\n\n");
        
        // Create mock interventions using the Builder pattern
        List<Intervention> testInterventions = Arrays.asList(
            Intervention.builder()
                .interventionId("test1")
                .title("Street Light Repair")
                .description("Broken street light on Main St")
                .interventionType("STREET_LIGHT")
                .priority(Intervention.Priority.HIGH)
                .location(new Location())
                .build(),
                
            Intervention.builder()
                .interventionId("test2")
                .title("Water Pipe Burst")
                .description("Water pipe burst flooding street")
                .interventionType("WATER_LEAK")
                .priority(Intervention.Priority.CRITICAL)
                .location(new Location())
                .build(),
                
            Intervention.builder()
                .interventionId("test3")
                .title("Road Repair")
                .description("Pothole on highway needs repair")
                .interventionType("ROAD_REPAIR")
                .priority(Intervention.Priority.MEDIUM)
                .location(new Location())
                .build()
        );
        
        // Get mock technicians
        List<Technician> technicians = smartSchedulerService.createMockTechnicians();
        
        for (int i = 0; i < testInterventions.size(); i++) {
            Intervention intervention = testInterventions.get(i);
            String bestTech = smartSchedulerService.findBestTechnician(intervention, technicians);
            
            result.append(String.format(
                "=== INTERVENTION %d ===\n" +
                "Title: %s\n" +
                "Type: %s\n" +
                "Best Technician: %s\n\n",
                i + 1, intervention.getTitle(), intervention.getInterventionType(), bestTech
            ));
        }
        
        result.append("</pre>");
        return result.toString();
    }
    
    @GetMapping("/analyze")
    public String analyzeProblem(@RequestParam String description) {
        try {
            int urgency = smartSchedulerService.analyzeUrgency(description);
            
            // Create a mock intervention using Builder pattern
            Intervention intervention = Intervention.builder()
                .interventionId("auto-" + System.currentTimeMillis())
                .title("Auto-generated from description")
                .description(description)
                .interventionType(determineInterventionType(description))
                .priority(mapUrgencyToPriority(urgency))
                .location(new Location())
                .priorityScore(urgency)
                .build();
            
            List<Technician> technicians = smartSchedulerService.createMockTechnicians();
            String bestTech = smartSchedulerService.findBestTechnician(intervention, technicians);
            
            return String.format("<pre>"
                + "🔍 SMART ANALYSIS RESULTS\n"
                + "Problem: %s\n"
                + "Detected Type: %s\n"
                + "Urgency Score: %d/10\n"
                + "Priority: %s\n"
                + "Recommended Technician: %s\n"
                + "Status: ✅ ANALYSIS COMPLETE"
                + "</pre>", description, intervention.getInterventionType(), urgency, 
                intervention.getPriority(), bestTech);
                
        } catch (Exception e) {
            return String.format("<pre>"
                + "❌ ANALYSIS FAILED\n"
                + "Problem: %s\n"
                + "Error: %s\n"
                + "</pre>", description, e.getMessage());
        }
    }
    
    private String determineInterventionType(String description) {
        String lowerDesc = description.toLowerCase();
        if (lowerDesc.contains("light") || lowerDesc.contains("electric")) return "STREET_LIGHT";
        if (lowerDesc.contains("traffic")) return "TRAFFIC_LIGHT";
        if (lowerDesc.contains("water") || lowerDesc.contains("pipe") || lowerDesc.contains("leak")) return "WATER_LEAK";
        if (lowerDesc.contains("road") || lowerDesc.contains("street") || lowerDesc.contains("pavement")) return "ROAD_REPAIR";
        if (lowerDesc.contains("waste") || lowerDesc.contains("garbage")) return "WASTE_ISSUE";
        return "STREET_LIGHT"; // default
    }
    
    private Intervention.Priority mapUrgencyToPriority(int urgency) {
        if (urgency >= 9) return Intervention.Priority.CRITICAL;
        if (urgency >= 7) return Intervention.Priority.HIGH;
        if (urgency >= 5) return Intervention.Priority.MEDIUM;
        return Intervention.Priority.LOW;
    }
}