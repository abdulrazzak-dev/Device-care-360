package com.devicecare360.user.controller;

import com.devicecare360.shared.dto.ApiResponse;
import com.devicecare360.user.document.UserProfile;
import com.devicecare360.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfile>> getMyProfile(@RequestHeader("X-User-Id") String userId) {
        UserProfile profile = userService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserProfile>> getUserById(@PathVariable String id) {
        UserProfile profile = userService.getUserProfile(id);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserProfile>> updateUser(@PathVariable String id, @RequestBody UserProfile updateRequest) {
        UserProfile updated = userService.updateUserProfile(id, updateRequest);
        return ResponseEntity.ok(ApiResponse.success(updated, "Profile updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String id) {
        userService.deleteUserProfile(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }
}
