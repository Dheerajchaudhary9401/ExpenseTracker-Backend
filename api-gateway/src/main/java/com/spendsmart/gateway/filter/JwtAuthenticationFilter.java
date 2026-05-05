package com.spendsmart.gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter
        extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter() {
        super(Config.class);
        this.jwtUtil = null;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            // Step 1: Get the Authorization header from the incoming request
            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            // Step 2: Check if header is present and starts with "Bearer "
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header");
                return unauthorizedResponse(exchange);
            }

            // Step 3: Extract the raw token (remove "Bearer " prefix)
            String token = authHeader.substring(7);

            // Step 4: Validate the token
            if (!jwtUtil.validateToken(token)) {
                log.warn("Invalid or expired JWT token");
                return unauthorizedResponse(exchange);
            }

            // Step 5: Token is valid — extract user info and add as headers
            String email  = jwtUtil.extractEmail(token);
            int userId = jwtUtil.extractUserId(token);

            log.info("Authenticated request from userId={} email={}", userId, email);

            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(exchange.getRequest().mutate()
                            .header("X-User-Email", email)
                            .header("X-User-Id", String.valueOf(userId))
                            .build())
                    .build();

            // Step 6: Forward the modified request to the next filter / service
            return chain.filter(modifiedExchange);
        };
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        // Add per-route config fields here if needed in the future
    }
}