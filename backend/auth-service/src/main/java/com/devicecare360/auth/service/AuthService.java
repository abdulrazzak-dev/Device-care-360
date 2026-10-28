package com.devicecare360.auth.service;

import com.devicecare360.auth.config.RabbitMQConfig;
import com.devicecare360.auth.document.AuthUser;
import com.devicecare360.auth.dto.AuthResponse;
import com.devicecare360.auth.dto.LoginRequest;
import com.devicecare360.auth.dto.RegisterRequest;
import com.devicecare360.auth.repository.AuthUserRepository;
import com.devicecare360.shared.event.TechnicianRegisteredEvent;
import com.devicecare360.shared.event.UserRegisteredEvent;
import com.devicecare360.shared.security.JwtUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.devicecare360.shared.model.DeviceSpecialization;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final RabbitTemplate rabbitTemplate;

    @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    @Value("${services.user-service.url:${USER_SERVICE_URL:https://device-care-360-user-service.onrender.com}}")
    private String userServiceUrl;

    @Value("${services.technician-service.url:${TECHNICIAN_SERVICE_URL:https://device-care-360-technician-service.onrender.com}}")
    private String technicianServiceUrl;

    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        String cleanSecret = JwtUtils.sanitizeSecret(jwtSecret);
        String fingerprint = JwtUtils.getSecretFingerprint(cleanSecret);
        log.info("AuthService JWT Security configured: algorithm={}, secretLength={}, sha256Fingerprint={}",
                JwtUtils.SIGNATURE_ALGORITHM, cleanSecret.length(), fingerprint);

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        this.restTemplate = new RestTemplate(factory);
    }

    public AuthResponse register(RegisterRequest request) {
        return register(request, "N/A");
    }

    public AuthResponse register(RegisterRequest request, String correlationId) {
        if (authUserRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken: " + request.getUsername());
        }
        if (authUserRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + request.getEmail());
        }

        String role = (request.getRole() != null && !request.getRole().isBlank()) 
                ? request.getRole().toUpperCase() 
                : "USER";

        AuthUser user = AuthUser.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .active(true)
                .build();

        AuthUser savedUser = authUserRepository.save(user);
        log.info("[CorrelationId: {}] AuthUser created with ID: {} and role: {}", correlationId, savedUser.getId(), role);

        // 1. Direct synchronous profile persistence to downstream microservice (user-service / technician-service)
        if ("TECHNICIAN".equals(role)) {
            syncTechnicianProfile(savedUser.getId(), request, correlationId);
        } else {
            syncUserProfile(savedUser.getId(), request, correlationId);
        }

        // 2. Publish async event to RabbitMQ (resilient against broker connection refusal)
        publishRegistrationEvent(savedUser.getId(), savedUser.getUsername(), savedUser.getEmail(), request, role, correlationId);

        String token = JwtUtils.generateToken(savedUser.getUsername(), savedUser.getRole(), savedUser.getId(), jwtSecret, jwtExpiration);

        return AuthResponse.builder()
                .token(token)
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();
    }

    private void syncUserProfile(String userId, RegisterRequest request, String correlationId) {
        String targetUrl = userServiceUrl.replaceAll("/+$", "") + "/api/users";
        log.info("[CorrelationId: {}] Synchronizing user profile with user-service at: {}", correlationId, targetUrl);
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", userId);
            payload.put("username", request.getUsername());
            payload.put("email", request.getEmail());
            payload.put("fullName", request.getFullName());
            payload.put("phone", request.getPhone() != null ? request.getPhone() : "");
            payload.put("status", "ACTIVE");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Correlation-Id", correlationId);
            headers.set("X-User-Id", userId);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(targetUrl, entity, String.class);
            log.info("[CorrelationId: {}] Downstream user-service profile creation status: {}", correlationId, response.getStatusCode());
        } catch (Exception e) {
            log.warn("[CorrelationId: {}] Direct profile synchronization to user-service failed: {}. Profile will be lazily initialized on demand.",
                    correlationId, e.getMessage());
        }
    }

    private void syncTechnicianProfile(String technicianId, RegisterRequest request, String correlationId) {
        String targetUrl = technicianServiceUrl.replaceAll("/+$", "") + "/api/technicians/register";
        log.info("[CorrelationId: {}] Synchronizing technician profile with technician-service at: {}", correlationId, targetUrl);
        try {
            List<String> canonicalSpecializations = Collections.emptyList();
            if (request.getSpecializations() != null && !request.getSpecializations().isEmpty()) {
                canonicalSpecializations = request.getSpecializations().stream()
                        .map(DeviceSpecialization::normalizeToCanonical)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList());
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("id", technicianId);
            payload.put("fullName", request.getFullName());
            payload.put("email", request.getEmail());
            payload.put("phone", request.getPhone() != null ? request.getPhone() : "");
            payload.put("available", true);
            payload.put("verificationStatus", "PENDING");
            payload.put("specializations", canonicalSpecializations);
            payload.put("categories", canonicalSpecializations);
            payload.put("yearsOfExperience", request.getExperienceYears() != null ? request.getExperienceYears() : 1);
            payload.put("experienceYears", request.getExperienceYears() != null ? request.getExperienceYears() : 1);
            payload.put("hourlyRate", request.getHourlyRate() != null ? request.getHourlyRate() : 2500.0);
            payload.put("city", request.getCity() != null ? request.getCity() : "");
            payload.put("serviceArea", request.getServiceArea() != null ? request.getServiceArea() : "");
            payload.put("serviceAreas", request.getServiceArea() != null && !request.getServiceArea().isBlank()
                    ? List.of(request.getServiceArea())
                    : Collections.emptyList());
            payload.put("professionalBio", request.getProfessionalBio() != null ? request.getProfessionalBio() : "");
            if (request.getCertificateUrl() != null && !request.getCertificateUrl().isBlank()) {
                payload.put("certificateUrl", request.getCertificateUrl());
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Correlation-Id", correlationId);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(targetUrl, entity, String.class);
            log.info("[CorrelationId: {}] Downstream technician-service profile creation status: {}", correlationId, response.getStatusCode());
        } catch (Exception e) {
            log.warn("[CorrelationId: {}] Direct profile synchronization to technician-service failed: {}. Profile will be lazily initialized on demand.",
                    correlationId, e.getMessage());
        }
    }

    private void publishRegistrationEvent(String userId, String username, String email, RegisterRequest request, String role, String correlationId) {
        try {
            if ("TECHNICIAN".equals(role)) {
                List<String> canonicalSpecializations = Collections.emptyList();
                if (request.getSpecializations() != null && !request.getSpecializations().isEmpty()) {
                    canonicalSpecializations = request.getSpecializations().stream()
                            .map(DeviceSpecialization::normalizeToCanonical)
                            .filter(Objects::nonNull)
                            .distinct()
                            .collect(Collectors.toList());
                }

                TechnicianRegisteredEvent techEvent = TechnicianRegisteredEvent.builder()
                        .technicianId(userId)
                        .userId(userId)
                        .email(email)
                        .fullName(request.getFullName())
                        .phone(request.getPhone())
                        .specializations(canonicalSpecializations)
                        .serviceAreas(request.getServiceArea() != null && !request.getServiceArea().isBlank()
                                ? List.of(request.getServiceArea())
                                : Collections.emptyList())
                        .yearsOfExperience(request.getExperienceYears() != null ? request.getExperienceYears() : 1)
                        .hourlyRate(request.getHourlyRate() != null ? request.getHourlyRate() : 2500.0)
                        .city(request.getCity())
                        .serviceArea(request.getServiceArea())
                        .professionalBio(request.getProfessionalBio())
                        .certificateUrl(request.getCertificateUrl())
                        .build();
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.TECH_REGISTERED_ROUTING_KEY, techEvent);
                log.info("[CorrelationId: {}] Published TechnicianRegisteredEvent to RabbitMQ successfully", correlationId);
            } else {
                UserRegisteredEvent userEvent = UserRegisteredEvent.builder()
                        .userId(userId)
                        .username(username)
                        .email(email)
                        .fullName(request.getFullName())
                        .role(role)
                        .build();
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.USER_REGISTERED_ROUTING_KEY, userEvent);
                log.info("[CorrelationId: {}] Published UserRegisteredEvent to RabbitMQ successfully", correlationId);
            }
        } catch (Exception e) {
            log.warn("[CorrelationId: {}] RabbitMQ unavailable. Skipped async event publish: {}", correlationId, e.getMessage());
        }
    }

    public AuthResponse login(LoginRequest request) {
        AuthUser user = authUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        String token = JwtUtils.generateToken(user.getUsername(), user.getRole(), user.getId(), jwtSecret, jwtExpiration);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public boolean validateToken(String token) {
        return JwtUtils.validateToken(token, jwtSecret);
    }
}
