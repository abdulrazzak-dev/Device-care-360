package com.devicecare360.technician.controller;

import com.devicecare360.shared.dto.ApiResponse;
import com.devicecare360.technician.document.TechnicianProfile;
import com.devicecare360.technician.service.TechnicianService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/technicians")
@RequiredArgsConstructor
public class TechnicianController {

    private final TechnicianService service;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<TechnicianProfile>> register(@RequestBody TechnicianProfile profile) {
        TechnicianProfile created = service.registerTechnician(profile);
        return ResponseEntity.ok(ApiResponse.success(created, "Technician registered successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TechnicianProfile>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllTechnicians()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TechnicianProfile>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(service.getTechnicianById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TechnicianProfile>> update(@PathVariable String id, @RequestBody TechnicianProfile profile) {
        TechnicianProfile updated = service.updateTechnician(id, profile);
        return ResponseEntity.ok(ApiResponse.success(updated, "Technician profile updated successfully"));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<TechnicianProfile>>> search(
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String serviceArea,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.success(service.searchTechnicians(specialization, serviceArea, available, status)));
    }

    @PutMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<TechnicianProfile>> setAvailability(
            @PathVariable String id,
            @RequestParam boolean available) {
        TechnicianProfile updated = service.setAvailability(id, available);
        return ResponseEntity.ok(ApiResponse.success(updated, "Availability updated"));
    }
}
