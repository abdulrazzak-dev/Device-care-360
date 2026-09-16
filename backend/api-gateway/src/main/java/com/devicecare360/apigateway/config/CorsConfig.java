package com.devicecare360.apigateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    private static final Logger log = LoggerFactory.getLogger(CorsConfig.class);

    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
            "https://device-care-360.vercel.app",
            "http://localhost:5173",
            "http://localhost:3000",
            "http://localhost:8080"
    );

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public WebFilter corsFilter() {
        return (ServerWebExchange ctx, WebFilterChain chain) -> {
            ServerHttpRequest request = ctx.getRequest();
            HttpHeaders requestHeaders = request.getHeaders();
            String origin = requestHeaders.getOrigin();

            if (origin != null && !origin.trim().isEmpty()) {
                ServerHttpResponse response = ctx.getResponse();
                HttpHeaders responseHeaders = response.getHeaders();

                if (isAllowedOrigin(origin)) {
                    responseHeaders.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
                    responseHeaders.set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, PATCH, DELETE, OPTIONS");
                    responseHeaders.set(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
                    responseHeaders.set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");

                    String reqHeaders = requestHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
                    if (reqHeaders != null && !reqHeaders.trim().isEmpty()) {
                        responseHeaders.set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, reqHeaders);
                    } else {
                        responseHeaders.set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Content-Type, Authorization, Accept, Origin, X-Requested-With, X-User-Id, X-User-Name, X-User-Role");
                    }
                    responseHeaders.set(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
                }

                if (request.getMethod() == HttpMethod.OPTIONS) {
                    log.debug("CORS preflight handled for origin: {}, path: {}", origin, request.getURI().getPath());
                    response.setStatusCode(HttpStatus.OK);
                    return response.setComplete();
                }
            } else if (request.getMethod() == HttpMethod.OPTIONS) {
                // Handle OPTIONS without Origin header gracefully
                ServerHttpResponse response = ctx.getResponse();
                response.setStatusCode(HttpStatus.OK);
                return response.setComplete();
            }

            return chain.filter(ctx);
        };
    }

    private boolean isAllowedOrigin(String origin) {
        if (origin == null) {
            return false;
        }
        if (ALLOWED_ORIGINS.contains(origin)) {
            return true;
        }
        // Match Vercel preview and production subdomains
        return origin.startsWith("https://") && (origin.endsWith(".vercel.app") || origin.contains(".vercel.app"));
    }
}
