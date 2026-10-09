package com.devicecare360.apigateway.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class LoggingFilterTest {

    private final LoggingFilter loggingFilter = new LoggingFilter();

    @Test
    @DisplayName("LoggingFilter generates correlation ID and attaches to request and response")
    void testLoggingFilterGeneratesCorrelationId() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/troubleshooting/analyze")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        Route mockRoute = Route.async()
                .id("ai-troubleshooting-service")
                .uri(URI.create("https://device-care-360--ai-troubleshooting-service.onrender.com"))
                .order(0)
                .predicate(swe -> true)
                .build();
        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, mockRoute);
        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, URI.create("https://device-care-360--ai-troubleshooting-service.onrender.com/api/troubleshooting/analyze"));

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        GatewayFilterChain chain = ex -> {
            chainCalled.set(true);
            String corrId = ex.getRequest().getHeaders().getFirst(LoggingFilter.CORRELATION_ID_HEADER);
            assertNotNull(corrId, "Correlation ID should be present on request");
            assertFalse(corrId.isBlank());
            return Mono.empty();
        };

        StepVerifier.create(loggingFilter.filter(exchange, chain))
                .verifyComplete();

        assertTrue(chainCalled.get());
        assertTrue(exchange.getResponse().getHeaders().containsKey(LoggingFilter.CORRELATION_ID_HEADER));
    }

    @Test
    @DisplayName("LoggingFilter preserves incoming correlation ID")
    void testLoggingFilterPreservesExistingCorrelationId() {
        String existingCorrId = "custom-corr-id-999";
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/troubleshooting/analyze")
                .header(LoggingFilter.CORRELATION_ID_HEADER, existingCorrId)
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        GatewayFilterChain chain = ex -> {
            chainCalled.set(true);
            assertEquals(existingCorrId, ex.getRequest().getHeaders().getFirst(LoggingFilter.CORRELATION_ID_HEADER));
            return Mono.empty();
        };

        StepVerifier.create(loggingFilter.filter(exchange, chain))
                .verifyComplete();

        assertTrue(chainCalled.get());
        assertEquals(existingCorrId, exchange.getResponse().getHeaders().getFirst(LoggingFilter.CORRELATION_ID_HEADER));
    }
}
