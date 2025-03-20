package com.threadstacks.gateway.security;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import reactor.core.publisher.Mono;

@RefreshScope
@Component
public class BasicTokenFilter implements GatewayFilter {

    @Value("${security.basic-token}")
    private String expectedToken;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        
        if (path.startsWith("/auth/login") || path.startsWith("/users/register")) {
            System.out.println("🔹 Applying BasicTokenFilter");
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            System.out.println("🔍 Authorization Header: " + authHeader);

            if (authHeader == null || !authHeader.equals("Basic " + expectedToken)) {
        	 System.err.println("❌ Unauthorized request to " + path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        }
        
        return chain.filter(exchange);
    }
}
