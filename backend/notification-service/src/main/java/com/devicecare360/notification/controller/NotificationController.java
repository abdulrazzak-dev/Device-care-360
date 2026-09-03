package com.devicecare360.notification.controller;

import com.devicecare360.notification.document.NotificationRecord;
import com.devicecare360.notification.service.NotificationService;
import com.devicecare360.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationRecord>>> getMyNotifications(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(ApiResponse.success(service.getUserNotifications(userId)));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationRecord>> markAsRead(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(service.markAsRead(id), "Marked as read"));
    }
}
