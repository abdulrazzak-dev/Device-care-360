package com.devicecare360.repairguide.controller;

import com.devicecare360.repairguide.document.RepairGuide;
import com.devicecare360.repairguide.service.RepairGuideService;
import com.devicecare360.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repair-guides")
@RequiredArgsConstructor
public class RepairGuideController {

    private final RepairGuideService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RepairGuide>>> getGuides(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand) {
        return ResponseEntity.ok(ApiResponse.success(service.getAllGuides(category, brand)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RepairGuide>> getGuideById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(service.getGuideById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RepairGuide>> createGuide(@RequestBody RepairGuide guide) {
        RepairGuide created = service.createGuide(guide);
        return ResponseEntity.ok(ApiResponse.success(created, "Repair guide created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RepairGuide>> updateGuide(@PathVariable String id, @RequestBody RepairGuide guide) {
        RepairGuide updated = service.updateGuide(id, guide);
        return ResponseEntity.ok(ApiResponse.success(updated, "Repair guide updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGuide(@PathVariable String id) {
        service.deleteGuide(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Repair guide deleted successfully"));
    }
}
