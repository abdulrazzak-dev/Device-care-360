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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

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

    public AuthResponse register(RegisterRequest request) {
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

        if ("TECHNICIAN".equals(role)) {
            TechnicianRegisteredEvent techEvent = TechnicianRegisteredEvent.builder()
                    .technicianId(savedUser.getId())
                    .userId(savedUser.getId())
                    .email(savedUser.getEmail())
                    .fullName(request.getFullName())
                    .phone(request.getPhone())
                    .specializations(Collections.emptyList())
                    .serviceAreas(Collections.emptyList())
                    .build();
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.TECH_REGISTERED_ROUTING_KEY, techEvent);
        } else {
            UserRegisteredEvent userEvent = UserRegisteredEvent.builder()
                    .userId(savedUser.getId())
                    .username(savedUser.getUsername())
                    .email(savedUser.getEmail())
                    .fullName(request.getFullName())
                    .role(role)
                    .build();
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.USER_REGISTERED_ROUTING_KEY, userEvent);
        }

        String token = JwtUtils.generateToken(savedUser.getUsername(), savedUser.getRole(), savedUser.getId(), jwtSecret, jwtExpiration);

        return AuthResponse.builder()
                .token(token)
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();
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
