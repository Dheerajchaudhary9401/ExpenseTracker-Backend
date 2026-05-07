package com.spendsmart.gateway.config;

import com.spendsmart.gateway.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class GatewayConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                .route("auth-service-docs", r -> r
                        .path("/v3/api-docs/auth-service")
                        .filters(f -> f.rewritePath("/v3/api-docs/auth-service", "/v3/api-docs"))
                        .uri("http://localhost:8081"))

                .route("category-service-docs", r -> r
                        .path("/v3/api-docs/category-service")
                        .filters(f -> f.rewritePath("/v3/api-docs/category-service", "/v3/api-docs"))
                        .uri("http://localhost:8082"))

                .route("expense-service-docs", r -> r
                        .path("/v3/api-docs/expense-service")
                        .filters(f -> f.rewritePath("/v3/api-docs/expense-service", "/v3/api-docs"))
                        .uri("http://localhost:8083"))

                .route("income-service-docs", r -> r
                        .path("/v3/api-docs/income-service")
                        .filters(f -> f.rewritePath("/v3/api-docs/income-service", "/v3/api-docs"))
                        .uri("http://localhost:8084"))

                .route("budget-service-docs", r -> r
                        .path("/v3/api-docs/budget-service")
                        .filters(f -> f.rewritePath("/v3/api-docs/budget-service", "/v3/api-docs"))
                        .uri("http://localhost:8085"))

                .route("analytics-service-docs", r -> r
                        .path("/v3/api-docs/analytics-service")
                        .filters(f -> f.rewritePath("/v3/api-docs/analytics-service", "/v3/api-docs"))
                        .uri("http://localhost:8086"))

                .route("auth-public", r -> r
                        .path("/auth/register", "/auth/login", "/auth/refresh", "/auth/validate")
                        .uri("http://localhost:8081"))

                .route("auth-protected", r -> r
                        .path("/auth/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8081"))

                .route("category-service", r -> r
                        .path("/category/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8082"))

                .route("expense-service", r -> r
                        .path("/expense/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8083"))

                .route("income-service", r -> r
                        .path("/income/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8084"))

                .route("budget-service", r -> r
                        .path("/budget/**", "/recurring/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8085"))

                .route("analytics-service", r -> r
                        .path("/analytics/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("http://localhost:8086"))

                .build();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .authorizeExchange(exchange -> exchange
                        // Swagger resources — always public
                        .pathMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/webjars/**"
                        ).permitAll()
                        .anyExchange().permitAll()
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}