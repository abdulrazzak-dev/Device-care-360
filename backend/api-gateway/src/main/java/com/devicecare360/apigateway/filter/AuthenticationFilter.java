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
            HttpMethod method = request.getMethod();

            // 1. Immediately bypass preflight requests
            if (HttpMethod.OPTIONS.equals(method) || CorsUtils.isPreFlightRequest(request)) {
                return chain.filter(exchange);
            }

            boolean isOpen = isStrictlyOpenEndpoint(path, method) || (path != null && path.startsWith("/api/auth/"));

            // 2. Authorization Header check
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.trim().regionMatches(true, 0, "Bearer ", 0, 7)) {
                if (isOpen) {
                    return chain.filter(exchange);
                }
                log.warn("Missing or invalid Authorization header for protected path: {}", path);
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.trim().substring(7).trim();
            if ((token.startsWith("\"") && token.endsWith("\"") && token.length() >= 2) ||
                (token.startsWith("'") && token.endsWith("'") && token.length() >= 2)) {
                token = token.substring(1, token.length() - 1).trim();
            }

            if (token.isEmpty()) {
                if (isOpen) {
                    return chain.filter(exchange);
                }
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
                if (isOpen) {
                    return chain.filter(exchange);
                }
                log.warn("JWT token expired for path: {}. Expiration: {}", path, e.getClaims() != null ? e.getClaims().getExpiration() : "unknown");
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            } catch (Exception e) {
                if (isOpen) {
                    return chain.filter(exchange);
                }
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

    private boolean isStrictlyOpenEndpoint(String path, HttpMethod method) {
        if (path == null) {
            return false;
        }
        // Protected technician routes: /api/technicians/me and sub-routes are NEVER open
        if (path.startsWith("/api/technicians/me")) {
            return false;
        }
        // Admin verification routes are not open
        if (path.contains("/verify") || path.contains("/status")) {
            return false;
        }
        // Public technician browsing
        if (path.equals("/api/technicians") || path.startsWith("/api/technicians/search")) {
            return HttpMethod.GET.equals(method);
        }
        // Public individual technician profile by ID
        if (path.matches("^/api/technicians/[^/]+$")) {
            return HttpMethod.GET.equals(method);
        }
        // Sync registration from auth-service
        if (path.equals("/api/technicians/register")) {
            return HttpMethod.POST.equals(method);
        }

        return OPEN_API_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    public static class Config {
    }
}
