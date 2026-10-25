package com.devicecare360.auth.controller;

import com.devicecare360.auth.dto.AuthResponse;
import com.devicecare360.auth.dto.LoginRequest;
import com.devicecare360.auth.dto.RegisterRequest;
import com.devicecare360.auth.service.AuthService;
import com.devicecare360.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @Valid @RequestBody RegisterRequest request) {
        String corrId = (correlationId != null && !correlationId.isBlank()) 
                ? correlationId 
                : UUID.randomUUID().toString();
        log.info("[CorrelationId: {}] POST /api/auth/register received for username: {}, email: {}, role: {}",
                corrId, request.getUsername(), request.getEmail(), request.getRole());
        AuthResponse response = authService.register(request, corrId);
        return ResponseEntity.ok(ApiResponse.success(response, "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @Valid @RequestBody LoginRequest request) {
        String corrId = (correlationId != null && !correlationId.isBlank()) 
                ? correlationId 
                : UUID.randomUUID().toString();
        log.info("[CorrelationId: {}] POST /api/auth/login received for username: {}", corrId, request.getUsername());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "User logged in successfully"));
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validate(@RequestParam String token) {
        boolean isValid = authService.validateToken(token);
        return ResponseEntity.ok(ApiResponse.success(isValid, isValid ? "Token is valid" : "Token is invalid"));
    }
}
