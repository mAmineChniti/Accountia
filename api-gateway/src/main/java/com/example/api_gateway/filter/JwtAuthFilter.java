package com.example.api_gateway.filter;

import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import reactor.core.publisher.Mono;

/**
 * Global filter that strips any client-supplied user headers to prevent spoofing.
 * JWT validation is now handled by individual microservices.
 */
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Strip any client-supplied X-User-* headers to prevent spoofing
        ServerHttpRequest sanitized = exchange.getRequest().mutate()
            .headers(headers -> {
                headers.remove("X-User-Id");
                headers.remove("X-User-Email");
                headers.remove("X-User-Name");
                headers.remove("X-User-Roles");
            })
            .build();
        return chain.filter(exchange.mutate().request(sanitized).build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 5;
    }
}
