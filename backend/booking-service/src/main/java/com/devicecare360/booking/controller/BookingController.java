package com.devicecare360.booking.controller;

import com.devicecare360.booking.document.Booking;
import com.devicecare360.booking.service.BookingService;
import com.devicecare360.shared.dto.ApiResponse;
import jakarta.ws.rs.PATCH;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService service;

    @PostMapping
    public ResponseEntity<ApiResponse<Booking>> createBooking(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Booking booking) {
        booking.setUserId(userId);
        Booking created = service.createBooking(booking);
        return ResponseEntity.ok(ApiResponse.success(created, "Booking created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Booking>> getBookingById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(service.getBookingById(id)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Booking>>> getBookingsByUser(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.success(service.getBookingsByUserId(userId)));
    }

    @GetMapping("/technician/{technicianId}")
    public ResponseEntity<ApiResponse<List<Booking>>> getBookingsByTechnician(@PathVariable String technicianId) {
        return ResponseEntity.ok(ApiResponse.success(service.getBookingsByTechnicianId(technicianId)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Booking>> updateStatus(
            @PathVariable String id,
            @RequestParam String status,
            @RequestParam(required = false) String reason) {
        Booking updated = service.updateStatus(id, status, reason);
        return ResponseEntity.ok(ApiResponse.success(updated, "Booking status updated to " + status));
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<ApiResponse<Booking>> reschedule(
            @PathVariable String id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newTime) {
        Booking updated = service.reschedule(id, newTime);
        return ResponseEntity.ok(ApiResponse.success(updated, "Booking rescheduled successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBooking(@PathVariable String id) {
        service.deleteBooking(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Booking deleted successfully"));
    }
}
