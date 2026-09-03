package com.devicecare360.payment.controller;

import com.devicecare360.payment.document.PaymentRecord;
import com.devicecare360.payment.service.PaymentService;
import com.devicecare360.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService service;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentRecord>> processPayment(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody PaymentRecord paymentRecord) {
        paymentRecord.setUserId(userId);
        PaymentRecord result = service.processPayment(paymentRecord);
        return ResponseEntity.ok(ApiResponse.success(result, "Payment processed"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentRecord>> getPaymentById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(service.getPaymentById(id)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<PaymentRecord>>> getPaymentsByUser(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.success(service.getPaymentsByUserId(userId)));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<PaymentRecord>> getPaymentByBooking(@PathVariable String bookingId) {
        return ResponseEntity.ok(ApiResponse.success(service.getPaymentByBookingId(bookingId)));
    }
}
