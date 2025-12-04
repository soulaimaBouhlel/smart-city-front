package com.smartcity.controller;

import com.smartcity.model.Equipment;
import com.smartcity.model.Intervention;
import com.smartcity.repository.EquipmentRepository;
import com.smartcity.repository.InterventionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Controller for seeding sample data for testing the map feature.
 * DELETE THIS IN PRODUCTION!
 */
@Slf4j
@RestController
@RequestMapping("/api/seed")
@RequiredArgsConstructor
public class SeedDataController {

        private final EquipmentRepository equipmentRepository;
        private final InterventionRepository interventionRepository;

        /**
         * POST /api/seed/data
         * Seeds the database with sample equipment and interventions for testing.
         */
        @PostMapping("/data")
        public ResponseEntity<String> seedData() {
                log.info("Seeding sample data...");

                // Clear existing data
                equipmentRepository.deleteAll();
                interventionRepository.deleteAll();

                // Create sample equipment
                List<Equipment> equipments = new ArrayList<>();

                equipments.add(Equipment.builder()
                                .name("Street Light #101")
                                .type("Street Light")
                                .location(new GeoJsonPoint(10.1815, 36.8065))
                                .address("Avenue Habib Bourguiba, Tunis")
                                .lastMaintenance(new Date())
                                .status("Active")
                                .build());

                equipments.add(Equipment.builder()
                                .name("Traffic Signal #42")
                                .type("Traffic Signal")
                                .location(new GeoJsonPoint(10.1820, 36.8070))
                                .address("Place de la République, Tunis")
                                .lastMaintenance(new Date())
                                .status("Active")
                                .build());

                equipments.add(Equipment.builder()
                                .name("Air Quality Sensor #7")
                                .type("Sensor")
                                .location(new GeoJsonPoint(10.1810, 36.8055))
                                .address("Parc du Belvédère, Tunis")
                                .lastMaintenance(new Date())
                                .status("Maintenance")
                                .build());

                equipments.add(Equipment.builder()
                                .name("Street Light #102")
                                .type("Street Light")
                                .location(new GeoJsonPoint(10.1825, 36.8080))
                                .address("Avenue de France, Tunis")
                                .lastMaintenance(new Date())
                                .status("Faulty")
                                .build());

                equipmentRepository.saveAll(equipments);
                log.info("Seeded {} equipment records", equipments.size());

                // Create sample interventions
                List<Intervention> interventions = new ArrayList<>();

                interventions.add(Intervention.builder()
                                .title("Road Repair - Main Street")
                                .description("Pothole repair required on main street")
                                .interventionType("Road Maintenance")
                                .priority(Intervention.Priority.HIGH)
                                .status(Intervention.Status.IN_PROGRESS)
                                .location(new GeoJsonPoint(10.1830, 36.8075))
                                .address("Avenue de la Liberté, Tunis")
                                .createdDate(LocalDateTime.now().minusDays(2))
                                .priorityScore(85)
                                .build());

                interventions.add(Intervention.builder()
                                .title("Street Light Replacement")
                                .description("Replace faulty street light bulb")
                                .interventionType("Equipment Maintenance")
                                .priority(Intervention.Priority.MEDIUM)
                                .status(Intervention.Status.PENDING)
                                .location(new GeoJsonPoint(10.1805, 36.8060))
                                .address("Rue de Marseille, Tunis")
                                .createdDate(LocalDateTime.now().minusDays(1))
                                .priorityScore(60)
                                .build());

                interventions.add(Intervention.builder()
                                .title("Emergency Water Leak")
                                .description("Water main break requires immediate attention")
                                .interventionType("Emergency")
                                .priority(Intervention.Priority.CRITICAL)
                                .status(Intervention.Status.IN_PROGRESS)
                                .location(new GeoJsonPoint(10.1835, 36.8085))
                                .address("Rue de Rome, Tunis")
                                .createdDate(LocalDateTime.now().minusHours(3))
                                .priorityScore(100)
                                .build());

                interventions.add(Intervention.builder()
                                .title("Park Maintenance")
                                .description("Regular park cleaning and maintenance")
                                .interventionType("Routine Maintenance")
                                .priority(Intervention.Priority.LOW)
                                .status(Intervention.Status.COMPLETED)
                                .location(new GeoJsonPoint(10.1800, 36.8050))
                                .address("Jardin Habib Thameur, Tunis")
                                .createdDate(LocalDateTime.now().minusDays(5))
                                .completedDate(LocalDateTime.now().minusDays(3))
                                .priorityScore(30)
                                .build());

                interventionRepository.saveAll(interventions);
                log.info("Seeded {} intervention records", interventions.size());

                return ResponseEntity.ok(String.format(
                                "Successfully seeded %d equipment and %d interventions",
                                equipments.size(), interventions.size()));
        }

        /**
         * DELETE /api/seed/data
         * Clears all equipment and intervention data.
         */
        @DeleteMapping("/data")
        public ResponseEntity<String> clearData() {
                log.info("Clearing all data...");
                equipmentRepository.deleteAll();
                interventionRepository.deleteAll();
                return ResponseEntity.ok("All data cleared");
        }
}
