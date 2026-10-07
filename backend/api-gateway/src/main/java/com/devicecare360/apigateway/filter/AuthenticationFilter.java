package com.devicecare360.apigateway.filter;

import com.devicecare360.shared.security.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.reactive.CorsUtils;

import java.util.List;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String jwtSecret;

    private static final List<String> OPEN_API_ENDPOINTS = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/validate",
            "/api/troubleshooting",
            "/api/troubleshoot",
            "/api/diagnose",
            "/api/ai",
            "/api/devices/brands",
            "/api/devices/issues",
            "/api/devices/categories",
            "/api/repair-guides",
            "/api/technicians",
            "/v3/api-docs",
            "/swagger-ui",
            "/actuator"
    );

    public AuthenticationFilter() {
        super(Config.class);
    }

    @PostConstruct
    public void init() {
        String cleanSecret = JwtUtils.sanitizeSecret(jwtSecret);
        String fingerprint = JwtUtils.getSecretFingerprint(cleanSecret);
        log.info("API-Gateway JWT Security configured: algorithm={}, secretLength={}, sha256Fingerprint={}",
                JwtUtils.SIGNATURE_ALGORITHM, cleanSecret.length(), fingerprint);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // 1. Immediately bypass authentication for OPTIONS (preflight), CorsUtils, and open endpoints
            if (HttpMethod.OPTIONS.equals(request.getMethod())
                    || CorsUtils.isPreFlightRequest(request)
                    || (path != null && (path.startsWith("/api/auth/") || isOpenEndpoint(path)))) {
                return chain.filter(exchange);
            }

            // 2. Authorization Header check
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                log.warn("Missing Authorization header for path: {}", path);
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.trim().regionMatches(true, 0, "Bearer ", 0, 7)) {
                log.warn("Invalid Authorization header format for path: {}", path);
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.trim().substring(7).trim();
            if ((token.startsWith("\"") && token.endsWith("\"") && token.length() >= 2) ||
                (token.startsWith("'") && token.endsWith("'") && token.length() >= 2)) {
                token = token.substring(1, token.length() - 1).trim();
            }

            if (token.isEmpty()) {
                log.warn("Empty Bearer token for path: {}", path);
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            try {
                Claims claims = JwtUtils.extractAllClaims(token, jwtSecret);
                String username = claims.getSubject();
                String role = claims.get("role", String.class);
                String userId = claims.get("userId", String.class);

                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("X-User-Name", username != null ? username : "")
                        .header("X-User-Role", role != null ? role : "")
                        .header("X-User-Id", userId != null ? userId : "")
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                log.warn("JWT token expired for path: {}. Expiration: {}", path, e.getClaims() != null ? e.getClaims().getExpiration() : "unknown");
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            } catch (io.jsonwebtoken.security.SecurityException | io.jsonwebtoken.MalformedJwtException | io.jsonwebtoken.UnsupportedJwtException | IllegalArgumentException e) {
                log.warn("Invalid JWT token for path: {}. Error: {}", path, e.getMessage());
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            } catch (Exception e) {
                log.warn("JWT validation failed for path: {}. Error: {}", path, e.getMessage());
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private reactor.core.publisher.Mono<Void> onError(org.springframework.web.server.ServerWebExchange exchange, HttpStatus status) {
        org.springframework.http.server.reactive.ServerHttpResponse response = exchange.getResponse();
        if (!response.isCommitted()) {
            response.setStatusCode(status);
            return response.setComplete();
        }
        return reactor.core.publisher.Mono.empty();
    }

    private boolean isOpenEndpoint(String path) {
        if (path == null) {
            return false;
        }
        return OPEN_API_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    public static class Config {
    }
}