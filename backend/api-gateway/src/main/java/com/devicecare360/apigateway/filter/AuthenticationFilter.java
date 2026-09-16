package com.devicecare360.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
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

import java.security.Key;
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
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Invalid Authorization header format for path: {}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.substring(7);
            try {
                Claims claims = extractAllClaims(token);
                String username = claims.getSubject();
                String role = claims.get("role", String.class);
                String userId = claims.get("userId", String.class);

                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("X-User-Name", username != null ? username : "")
                        .header("X-User-Role", role != null ? role : "")
                        .header("X-User-Id", userId != null ? userId : "")
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            } catch (Exception e) {
                log.warn("Invalid JWT token for path: {}. Error: {}", path, e.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }

    private boolean isOpenEndpoint(String path) {
        return OPEN_API_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private Claims extractAllClaims(String token) {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        Key key = Keys.hmacShaKeyFor(keyBytes);
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static class Config {
    }
}