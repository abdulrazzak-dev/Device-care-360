package com.devicecare360.apigateway.config;

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
import reactor.core.publisher.Mono;

@Configuration
public class CorsConfig {

    private static final String ALLOWED_HEADERS = "x-requested-with, authorization, Content-Type, Authorization, credential, X-XSRF-TOKEN, X-User-Id, X-User-Role, X-User-Name";
    private static final String ALLOWED_METHODS = "GET, PUT, POST, DELETE, OPTIONS, PATCH";
    private static final String MAX_AGE = "3600";

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public WebFilter corsFilter() {
        return (ServerWebExchange ctx, WebFilterChain chain) -> {
            ServerHttpRequest request = ctx.getRequest();
            String origin = request.getHeaders().getOrigin();

            // Vercel அல்லது localhost-ஆக இருந்தால் CORS தலைப்புகளை இணைக்கவும்
            if (origin != null && (origin.endsWith(".vercel.app") || origin.contains("localhost"))) {
                ServerHttpResponse response = ctx.getResponse();
                HttpHeaders headers = response.getHeaders();
                headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
                headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, ALLOWED_METHODS);
                headers.set(HttpHeaders.ACCESS_CONTROL_MAX_AGE, MAX_AGE);
                headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, ALLOWED_HEADERS);
                headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");

                // Preflight OPTIONS கோரிக்கையாக இருந்தால் உடனடியாக 200 OK கொடுத்து முடிக்கவும்
                if (request.getMethod() == HttpMethod.OPTIONS) {
                    response.setStatusCode(HttpStatus.OK);
                    return Mono.empty();
                }
            }

            return chain.filter(ctx);
        };
    }
}