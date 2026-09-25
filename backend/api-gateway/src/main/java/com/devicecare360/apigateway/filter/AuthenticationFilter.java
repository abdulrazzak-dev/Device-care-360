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
            "/api/devices",
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
            if (request.getMethod() == HttpMethod.OPTIONS
                    || CorsUtils.isPreFlightRequest(request)
                    || (path != null && (path.startsWith("/api/auth/") || isOpenEndpoint(path)))) {
                return chain.filter(exchange);
            }

            // 2. Authorization Header check
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                log.warn("Missing Authorization header for path: {}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.trim().startsWith("Bearer ")) {
                log.warn("Invalid Authorization header format for path: {}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.trim().substring(7).trim();
            if ((token.startsWith("\"") && token.endsWith("\"") && token.length() >= 2) ||
                (token.startsWith("'") && token.endsWith("'") && token.length() >= 2)) {
                token = token.substring(1, token.length() - 1).trim();
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
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            } catch (io.jsonwebtoken.security.SecurityException e) {
                log.warn("Invalid JWT signature for path: {}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            } catch (io.jsonwebtoken.MalformedJwtException e) {
                log.warn("Malformed JWT token for path: {}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            } catch (io.jsonwebtoken.UnsupportedJwtException e) {
                log.warn("Unsupported JWT token for path: {}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            } catch (IllegalArgumentException e) {
                log.warn("JWT claims string is empty or invalid for path: {}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            } catch (Exception e) {
                log.warn("JWT validation failed for path: {}. Error: {}", path, e.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }

    private boolean isOpenEndpoint(String path) {
        return OPEN_API_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    public static class Config {
    }
}