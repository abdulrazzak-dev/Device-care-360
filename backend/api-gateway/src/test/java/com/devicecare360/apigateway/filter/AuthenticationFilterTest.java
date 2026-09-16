package com.devicecare360.apigateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationFilterTest {

    private AuthenticationFilter authenticationFilter;
    private GatewayFilter gatewayFilter;
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        authenticationFilter = new AuthenticationFilter();
        ReflectionTestUtils.setField(authenticationFilter, "jwtSecret", secret);
        gatewayFilter = authenticationFilter.apply(new AuthenticationFilter.Config());
    }

    @Test
    @DisplayName("OPTIONS request bypasses authentication filter completely")
    void testOptionsRequestBypassesAuth() {
        MockServerHttpRequest request = MockServerHttpRequest
                .options("/api/users/profile")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        StepVerifier.create(gatewayFilter.filter(exchange, ex -> {
            chainCalled.set(true);
            return Mono.empty();
        })).verifyComplete();

        assertTrue(chainCalled.get(), "Chain filter should be called for OPTIONS request");
        assertNotEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Open endpoint /api/troubleshooting/analyze bypasses authentication")
    void testOpenEndpointBypassesAuth() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/troubleshooting/analyze")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        StepVerifier.create(gatewayFilter.filter(exchange, ex -> {
            chainCalled.set(true);
            return Mono.empty();
        })).verifyComplete();

        assertTrue(chainCalled.get());
    }

    @Test
    @DisplayName("Protected endpoint without Authorization header returns 401 UNAUTHORIZED")
    void testProtectedEndpointWithoutToken() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/users/profile")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        StepVerifier.create(gatewayFilter.filter(exchange, ex -> {
            chainCalled.set(true);
            return Mono.empty();
        })).verifyComplete();

        assertFalse(chainCalled.get(), "Chain filter should NOT be called without token");
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Protected endpoint with valid JWT token extracts claims and passes to downstream")
    void testProtectedEndpointWithValidToken() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        Key key = Keys.hmacShaKeyFor(keyBytes);
        String token = Jwts.builder()
                .setSubject("testuser")
                .addClaims(Map.of("role", "ROLE_USER", "userId", "user-123"))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/users/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        StepVerifier.create(gatewayFilter.filter(exchange, ex -> {
            chainCalled.set(true);
            assertEquals("testuser", ex.getRequest().getHeaders().getFirst("X-User-Name"));
            assertEquals("ROLE_USER", ex.getRequest().getHeaders().getFirst("X-User-Role"));
            assertEquals("user-123", ex.getRequest().getHeaders().getFirst("X-User-Id"));
            return Mono.empty();
        })).verifyComplete();

        assertTrue(chainCalled.get());
    }
}
