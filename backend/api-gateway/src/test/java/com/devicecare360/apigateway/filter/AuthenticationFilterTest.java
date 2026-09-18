package com.devicecare360.apigateway.filter;

import com.devicecare360.shared.security.JwtUtils;
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
        String token = JwtUtils.generateToken("testuser", "ROLE_USER", "user-123", secret);

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

    @Test
    @DisplayName("Expired JWT token returns 401 UNAUTHORIZED")
    void testProtectedEndpointWithExpiredToken() {
        // Create expired token (-120000 ms = 2 minutes in the past)
        String expiredToken = JwtUtils.generateToken("testuser", "ROLE_USER", "user-123", secret, -120000L);

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/users/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        StepVerifier.create(gatewayFilter.filter(exchange, ex -> {
            chainCalled.set(true);
            return Mono.empty();
        })).verifyComplete();

        assertFalse(chainCalled.get(), "Chain filter should NOT be called for expired token");
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Invalid JWT signature returns 401 UNAUTHORIZED")
    void testProtectedEndpointWithInvalidSignature() {
        String wrongSecret = "999E635266556A586E3272357538782F413F4428472B4B6250645367566B5999";
        String tokenWithWrongSig = JwtUtils.generateToken("testuser", "ROLE_USER", "user-123", wrongSecret);

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/users/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenWithWrongSig)
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        StepVerifier.create(gatewayFilter.filter(exchange, ex -> {
            chainCalled.set(true);
            return Mono.empty();
        })).verifyComplete();

        assertFalse(chainCalled.get(), "Chain filter should NOT be called for invalid signature");
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Malformed JWT token returns 401 UNAUTHORIZED")
    void testProtectedEndpointWithMalformedToken() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/users/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer not.a.valid.jwt")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        StepVerifier.create(gatewayFilter.filter(exchange, ex -> {
            chainCalled.set(true);
            return Mono.empty();
        })).verifyComplete();

        assertFalse(chainCalled.get(), "Chain filter should NOT be called for malformed token");
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }
}
