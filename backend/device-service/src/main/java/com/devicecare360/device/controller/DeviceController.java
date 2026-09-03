package com.devicecare360.device.controller;

import com.devicecare360.device.document.UserDevice;
import com.devicecare360.device.service.DeviceService;
import com.devicecare360.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(deviceService.getCategories()));
    }

    @GetMapping("/brands")
    public ResponseEntity<ApiResponse<List<String>>> getBrands(@RequestParam String category) {
        return ResponseEntity.ok(ApiResponse.success(deviceService.getBrands(category)));
    }

    @GetMapping("/issues")
    public ResponseEntity<ApiResponse<List<String>>> getCommonIssues(@RequestParam String category) {
        return ResponseEntity.ok(ApiResponse.success(deviceService.getCommonIssues(category)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserDevice>> createDevice(@RequestHeader("X-User-Id") String userId, @RequestBody UserDevice device) {
        device.setUserId(userId);
        UserDevice created = deviceService.createDevice(device);
        return ResponseEntity.ok(ApiResponse.success(created, "Device registered successfully"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<UserDevice>>> getDevicesByUser(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.success(deviceService.getDevicesByUserId(userId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDevice>> getDeviceById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(deviceService.getDeviceById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDevice>> updateDevice(@PathVariable String id, @RequestBody UserDevice updateRequest) {
        UserDevice updated = deviceService.updateDevice(id, updateRequest);
        return ResponseEntity.ok(ApiResponse.success(updated, "Device updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDevice(@PathVariable String id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Device deleted successfully"));
    }
}
