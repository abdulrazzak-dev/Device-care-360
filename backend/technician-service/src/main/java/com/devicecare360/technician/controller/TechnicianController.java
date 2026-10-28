package com.devicecare360.technician.controller;

import com.devicecare360.shared.dto.ApiResponse;
import com.devicecare360.shared.security.JwtUtils;
import com.devicecare360.technician.document.TechnicianProfile;
import com.devicecare360.technician.dto.CertificateUploadResponse;
import com.devicecare360.technician.dto.TechnicianProfileDTO;
import com.devicecare360.technician.dto.TechnicianStatusUpdateRequest;
import com.devicecare360.technician.dto.TechnicianUpdateRequest;
import com.devicecare360.technician.service.TechnicianService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/technicians")
@RequiredArgsConstructor
@Slf4j
public class TechnicianController {

    private final TechnicianService service;

    @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String jwtSecret;

    /**
     * Internal/Downstream registration from auth-service.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<TechnicianProfile>> register(@RequestBody TechnicianProfile profile) {
        TechnicianProfile created = service.registerTechnician(profile);
        return ResponseEntity.ok(ApiResponse.success(created, "Technician registered successfully"));
    }

    /**
     * Public listing: Only approved technicians are returned.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TechnicianProfile>>> getAll(
            @RequestParam(required = false) String specialization) {
        if (specialization != null && !specialization.isBlank()) {
            return ResponseEntity.ok(ApiResponse.success(service.searchTechnicians(specialization, null, null, null)));
        }
        return ResponseEntity.ok(ApiResponse.success(service.getAllTechnicians()));
    }

    /**
     * Public search: Defaults to approved technicians.
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<TechnicianProfile>>> search(
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String serviceArea,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.success(service.searchTechnicians(specialization, serviceArea, available, status)));
    }

    /**
     * Authenticated technician retrieves own profile.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<TechnicianProfile>> getMyProfile(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        String userId = resolveUserId(headerUserId, authHeader);
        return ResponseEntity.ok(ApiResponse.success(service.getMyProfile(userId)));
    }

    /**
     * Authenticated technician updates own editable professional information.
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<TechnicianProfile>> updateMyProfile(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody TechnicianUpdateRequest updateRequest) {
        String userId = resolveUserId(headerUserId, authHeader);
        TechnicianProfile updated = service.updateMyProfile(userId, updateRequest);
        return ResponseEntity.ok(ApiResponse.success(updated, "Technician profile updated successfully"));
    }

    /**
     * Dedicated certificate upload endpoint for authenticated technician.
     */
    @PostMapping(value = "/me/certificates", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CertificateUploadResponse>> uploadCertificate(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam("file") MultipartFile file) throws IOException {
        String userId = resolveUserId(headerUserId, authHeader);
        CertificateUploadResponse response = service.uploadCertificate(userId, file);
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }

    /**
     * Lookup technician by ID.
     * Note: "me" is routed to getMyProfile to prevent route collision.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TechnicianProfile>> getById(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        if ("me".equalsIgnoreCase(id)) {
            String userId = resolveUserId(headerUserId, authHeader);
            return ResponseEntity.ok(ApiResponse.success(service.getMyProfile(userId)));
        }
        return ResponseEntity.ok(ApiResponse.success(service.getTechnicianById(id)));
    }

    /**
     * Admin review / status update endpoint.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TechnicianProfile>> updateStatus(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String adminId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody TechnicianStatusUpdateRequest request) {
        validateAdminRole(role);
        String reviewer = adminId != null && !adminId.isBlank() ? adminId : "ADMIN";
        TechnicianProfile updated = service.updateVerificationStatus(id, reviewer, request.getStatus(), request.getRejectionReason());
        return ResponseEntity.ok(ApiResponse.success(updated, "Technician status updated to " + request.getStatus()));
    }

    /**
     * Admin verification alias for backward compatibility.
     */
    @PutMapping("/{id}/verify")
    public ResponseEntity<ApiResponse<TechnicianProfile>> verifyTechnician(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String adminId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestParam String status,
            @RequestParam(required = false) String reason) {
        validateAdminRole(role);
        String reviewer = adminId != null && !adminId.isBlank() ? adminId : "ADMIN";
        TechnicianProfile updated = service.updateVerificationStatus(id, reviewer, status, reason);
        return ResponseEntity.ok(ApiResponse.success(updated, "Technician status updated to " + status));
    }

    /**
     * Generic update endpoint for internal backward compatibility.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TechnicianProfile>> update(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String adminId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestBody Map<String, Object> body) {
        if (body.containsKey("verificationStatus")) {
            validateAdminRole(role);
            String status = String.valueOf(body.get("verificationStatus"));
            String reason = body.containsKey("rejectionReason") ? String.valueOf(body.get("rejectionReason")) : null;
            TechnicianProfile updated = service.updateVerificationStatus(id, adminId != null ? adminId : "ADMIN", status, reason);
            return ResponseEntity.ok(ApiResponse.success(updated, "Technician verification status updated"));
        }

        TechnicianProfile profile = service.getTechnicianById(id);
        if (body.containsKey("fullName")) profile.setFullName((String) body.get("fullName"));
        if (body.containsKey("phone")) profile.setPhone((String) body.get("phone"));
        if (body.containsKey("available")) profile.setAvailable(Boolean.valueOf(String.valueOf(body.get("available"))));
        if (body.containsKey("hourlyRate")) profile.setHourlyRate(Double.valueOf(String.valueOf(body.get("hourlyRate"))));
        TechnicianProfile updated = service.updateTechnician(id, profile);
        return ResponseEntity.ok(ApiResponse.success(updated, "Technician profile updated successfully"));
    }

    @PutMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<TechnicianProfile>> setAvailability(
            @PathVariable String id,
            @RequestParam boolean available) {
        TechnicianProfile updated = service.setAvailability(id, available);
        return ResponseEntity.ok(ApiResponse.success(updated, "Availability updated"));
    }

    private String resolveUserId(String headerUserId, String authHeader) {
        if (headerUserId != null && !headerUserId.isBlank()) {
            return headerUserId.trim();
        }
        if (authHeader != null && authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            String token = authHeader.substring(7).trim();
            if ((token.startsWith("\"") && token.endsWith("\"")) || (token.startsWith("'") && token.endsWith("'"))) {
                token = token.substring(1, token.length() - 1).trim();
            }
            try {
                Claims claims = JwtUtils.extractAllClaims(token, jwtSecret);
                String userId = claims.get("userId", String.class);
                if (userId != null && !userId.isBlank()) {
                    return userId;
                }
                return claims.getSubject();
            } catch (Exception e) {
                log.warn("Failed to extract claims from Bearer token: {}", e.getMessage());
            }
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid authorization credentials");
    }

    private void validateAdminRole(String role) {
        if (role == null || !"ADMIN".equalsIgnoreCase(role.trim())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators are authorized to perform this operation");
        }
    }
}
