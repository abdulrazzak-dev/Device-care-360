package com.devicecare360.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.UUID;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        ServerHttpRequest request = exchange.getRequest();

        // 1. Resolve or generate Correlation ID
        String correlationId = request.getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        ServerHttpRequest modifiedRequest = request.mutate()
                .header(CORRELATION_ID_HEADER, correlationId)
                .build();

        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(modifiedRequest)
                .build();

        // Attach correlation ID to response headers
        modifiedExchange.getResponse().getHeaders().add(CORRELATION_ID_HEADER, correlationId);

        String finalCorrelationId = correlationId;
        String method = request.getMethod().name();
        String path = request.getURI().getPath();

        return chain.filter(modifiedExchange)
                .doFinally(signalType -> {
                    long duration = System.currentTimeMillis() - startTime;
                    Route route = modifiedExchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
                    URI routeUri = route != null ? route.getUri() : null;
                    String routeId = route != null ? route.getId() : "unknown";
                    URI targetUri = modifiedExchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);

                    HttpStatusCode status = modifiedExchange.getResponse().getStatusCode();
                    int statusCode = status != null ? status.value() : 0;

                    log.info("[GATEWAY-ROUTING] correlationId={} method={} path={} routeId={} targetBaseUri={} forwardedUrl={} status={} durationMs={}",
                            finalCorrelationId, method, path, routeId, routeUri, targetUri, statusCode, duration);
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
