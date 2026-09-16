package com.devicecare360.apigateway.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class CorsConfigTest {

    private final CorsConfig corsConfig = new CorsConfig();
    private final WebFilter filter = corsConfig.corsFilter();

    @Test
    @DisplayName("OPTIONS /api/troubleshooting/analyze preflight from Vercel returns HTTP 200 with CORS headers")
    void testPreflightAnalyzeEndpointFromVercel() {
        MockServerHttpRequest request = MockServerHttpRequest
                .method(HttpMethod.OPTIONS, URI.create("https://api-gateway-production-d334.up.railway.app/api/troubleshooting/analyze"))
                .header(HttpHeaders.ORIGIN, "https://device-care-360.vercel.app")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type,authorization")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicBoolean chainCalled = new AtomicBoolean(false);
        WebFilterChain chain = ex -> {
            chainCalled.set(true);
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertFalse(chainCalled.get(), "Chain should not be called for preflight OPTIONS");
        assertEquals(HttpStatus.OK, exchange.getResponse().getStatusCode());
        assertEquals("https://device-care-360.vercel.app",
                exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertEquals("GET, POST, PUT, PATCH, DELETE, OPTIONS",
                exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS));
        assertEquals("true",
                exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
        assertEquals("content-type,authorization",
                exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS));
        assertEquals("3600",
                exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_MAX_AGE));
    }

    @Test
    @DisplayName("OPTIONS /api/auth/login preflight from Vercel returns HTTP 200 with CORS headers")
    void testPreflightLoginEndpointFromVercel() {
        MockServerHttpRequest request = MockServerHttpRequest
                .method(HttpMethod.OPTIONS, URI.create("https://api-gateway-production-d334.up.railway.app/api/auth/login"))
                .header(HttpHeaders.ORIGIN, "https://device-care-360.vercel.app")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicBoolean chainCalled = new AtomicBoolean(false);
        WebFilterChain chain = ex -> {
            chainCalled.set(true);
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertFalse(chainCalled.get());
        assertEquals(HttpStatus.OK, exchange.getResponse().getStatusCode());
        assertEquals("https://device-care-360.vercel.app",
                exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertEquals("true",
                exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
    }

    @Test
    @DisplayName("POST /api/troubleshooting/analyze with CORS Origin passes through and adds CORS headers")
    void testPostAnalyzeWithCorsOrigin() {
        MockServerHttpRequest request = MockServerHttpRequest
                .method(HttpMethod.POST, URI.create("https://api-gateway-production-d334.up.railway.app/api/troubleshooting/analyze"))
                .header(HttpHeaders.ORIGIN, "https://device-care-360.vercel.app")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicBoolean chainCalled = new AtomicBoolean(false);
        WebFilterChain chain = ex -> {
            chainCalled.set(true);
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertTrue(chainCalled.get(), "Chain should be called for normal POST requests");
        assertEquals("https://device-care-360.vercel.app",
                exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertEquals("true",
                exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
    }

    @Test
    @DisplayName("Localhost origins (5173, 3000) are allowed for development")
    void testLocalhostOrigins() {
        MockServerHttpRequest request = MockServerHttpRequest
                .method(HttpMethod.OPTIONS, URI.create("http://localhost:8080/api/troubleshooting/analyze"))
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(HttpStatus.OK, exchange.getResponse().getStatusCode());
        assertEquals("http://localhost:5173",
                exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }
}
