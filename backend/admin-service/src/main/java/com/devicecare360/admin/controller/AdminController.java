package com.devicecare360.admin.controller;

import com.devicecare360.admin.service.AdminService;
import com.devicecare360.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getDashboardStats()));
    }

    @PutMapping("/technicians/{id}/verify")
    public ResponseEntity<ApiResponse<Void>> verifyTechnician(
            @RequestHeader("X-User-Id") String adminId,
            @PathVariable String id,
            @RequestParam String status) {
        adminService.verifyTechnician(adminId, id, status);
        return ResponseEntity.ok(ApiResponse.success(null, "Technician status updated to " + status));
    }
}
