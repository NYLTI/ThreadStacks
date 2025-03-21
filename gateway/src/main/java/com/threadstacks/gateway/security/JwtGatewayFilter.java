package com.threadstacks.gateway.security;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Component
public class JwtGatewayFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
            .filter(principal -> principal instanceof JwtAuthenticationToken)
            .cast(JwtAuthenticationToken.class)
            .flatMap(authToken -> {
                Jwt jwt = (Jwt) authToken.getCredentials();

                String username = jwt.getClaim("preferred_username");
                String roles = authToken.getAuthorities().stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));

                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("X-User-Name", username)
                        .header("X-User-Roles", roles)
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            }).switchIfEmpty(chain.filter(exchange));
    }
}
