package com.devicecare360.user.controller;

import com.devicecare360.shared.dto.ApiResponse;
import com.devicecare360.user.document.UserProfile;
import com.devicecare360.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserProfile>> createUserProfile(
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @RequestBody UserProfile profile) {
        log.info("[CorrelationId: {}] Creating user profile for userId: {}, username: {}", 
                correlationId, profile.getId(), profile.getUsername());
        UserProfile created = userService.createUserProfile(profile);
        return ResponseEntity.ok(ApiResponse.success(created, "User profile created successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfile>> getMyProfile(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {
        log.info("[CorrelationId: {}] Fetching profile for authenticated userId: {}", correlationId, userId);
        UserProfile profile = userService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserProfile>> getUserById(
            @PathVariable String id,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {
        log.info("[CorrelationId: {}] Fetching profile by ID: {}", correlationId, id);
        UserProfile profile = userService.getUserProfile(id);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserProfile>> updateUser(
            @PathVariable String id,
            @RequestBody UserProfile updateRequest,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {
        log.info("[CorrelationId: {}] Updating profile for ID: {}", correlationId, id);
        UserProfile updated = userService.updateUserProfile(id, updateRequest);
        return ResponseEntity.ok(ApiResponse.success(updated, "Profile updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable String id,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {
        log.info("[CorrelationId: {}] Deleting profile for ID: {}", correlationId, id);
        userService.deleteUserProfile(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }
}
