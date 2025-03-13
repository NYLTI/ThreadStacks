package com.threadstacks.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.threadstacks.gateway.security.JwtAuthenticationFilter;

@Configuration
public class GatewayConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public GatewayConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("user-service", r -> r.path("/user/**")
                .filters(f -> f.filter(jwtAuthenticationFilter))
                .uri("lb://USER-SERVICE"))
            .route("thread-service", r -> r.path("/thread/**")
                .filters(f -> f.filter(jwtAuthenticationFilter))
                .uri("lb://THREAD-SERVICE"))
            .route("room-service", r -> r.path("/room/**")
                    .filters(f -> f.filter(jwtAuthenticationFilter))
                    .uri("lb://ROOM-SERVICE"))
            .build();
    }
}