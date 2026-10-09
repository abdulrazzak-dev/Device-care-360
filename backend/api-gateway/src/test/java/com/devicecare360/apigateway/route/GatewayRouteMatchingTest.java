package com.devicecare360.apigateway.route;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false"
})
class GatewayRouteMatchingTest {

    @Autowired
    private RouteLocator routeLocator;

    @Test
    @DisplayName("Verify that all critical production routes are loaded by Spring Cloud Gateway")
    void testRoutesAreLoaded() {
        StepVerifier.create(routeLocator.getRoutes().collectList())
                .assertNext(routes -> {
                    assertTrue(routes.stream().anyMatch(r -> "ai-troubleshooting-service".equals(r.getId())));
                    assertTrue(routes.stream().anyMatch(r -> "ai-troubleshooting-diagnose-alias".equals(r.getId())));
                    assertTrue(routes.stream().anyMatch(r -> "ai-troubleshooting-troubleshoot-alias".equals(r.getId())));
                    assertTrue(routes.stream().anyMatch(r -> "device-categories-public".equals(r.getId())));
                    assertTrue(routes.stream().anyMatch(r -> "device-brands-public".equals(r.getId())));
                    assertTrue(routes.stream().anyMatch(r -> "device-issues-public".equals(r.getId())));
                })
                .verifyComplete();
    }
}
