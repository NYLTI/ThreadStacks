package com.threadstacks.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
	return builder.routes()
		.route("auth-service", r -> r.path("/users/register", "/auth/login").uri("lb://USER-SERVICE"))
		.route("user-service", r -> r.path("/users/**").uri("lb://USER-SERVICE"))
		.route("thread-service", r -> r.path("/threads/**").uri("lb://THREAD-SERVICE"))
		.route("room-service", r -> r.path("/rooms/**").uri("lb://ROOM-SERVICE")).build();
    }
}