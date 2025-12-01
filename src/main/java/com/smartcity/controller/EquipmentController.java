package com.smartcity.controller;

import com.smartcity.model.Equipment;
import com.smartcity.model.EquipmentType;
import com.smartcity.model.Status;
import com.smartcity.service.EquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    // POST: Create new equipment
    @PostMapping
    public ResponseEntity<Equipment> createEquipment(@RequestBody Equipment equipment) {
        Equipment createdEquipment = equipmentService.createEquipment(equipment);
        return new ResponseEntity<>(createdEquipment, HttpStatus.CREATED);
    }

    // GET: Retrieve all equipment (Similar to InteractiveMap's o_displayMap/List<Equipment>)
    @GetMapping
    public ResponseEntity<List<Equipment>> getAllEquipment() {
        List<Equipment> equipment = equipmentService.getAllEquipment();
        return ResponseEntity.ok(equipment);
    }

    // GET: Retrieve equipment by ID
    @GetMapping("/{id}")
    public ResponseEntity<Equipment> getEquipmentById(@PathVariable String id) {
        return equipmentService.getEquipmentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT: Update existing equipment
    @PutMapping("/{id}")
    public ResponseEntity<Equipment> updateEquipment(@PathVariable String id, @RequestBody Equipment equipmentDetails) {
        try {
            Equipment updatedEquipment = equipmentService.updateEquipment(id, equipmentDetails);
            return ResponseEntity.ok(updatedEquipment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // DELETE: Delete equipment
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable String id) {
        equipmentService.deleteEquipment(id);
        return ResponseEntity.noContent().build();
    }

    // GET: Filter by Status (Similar to InteractiveMap's o_filterByStatus)
    @GetMapping("/filter/status/{status}")
    public ResponseEntity<List<Equipment>> filterByStatus(@PathVariable Status status) {
        List<Equipment> equipment = equipmentService.filterByStatus(status);
        return ResponseEntity.ok(equipment);
    }

    // GET: Filter by Type (Similar to InteractiveMap's o_filterByType)
    @GetMapping("/filter/type/{type}")
    public ResponseEntity<List<Equipment>> filterByType(@PathVariable EquipmentType type) {
        List<Equipment> equipment = equipmentService.filterByType(type);
        return ResponseEntity.ok(equipment);
    }
}