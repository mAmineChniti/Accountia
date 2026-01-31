package com.example.api_gateway.filter;

import com.example.api_gateway.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {
    private final JwtUtil jwtUtil;
    private final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final Set<String> whitelistPrefixes = Set.of("/api/auth", "/actuator", "/api/public");

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    private boolean isWhitelisted(URI uri) {
        String p = uri.getPath();
        for (String w : whitelistPrefixes) if (p.startsWith(w)) return true;
        return false;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        URI uri = exchange.getRequest().getURI();
        if (isWhitelisted(uri)) return chain.filter(exchange);

        List<String> auth = exchange.getRequest().getHeaders().getOrEmpty(HttpHeaders.AUTHORIZATION);
        if (auth.isEmpty()) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        String bearer = auth.get(0);
        if (!bearer.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        String token = bearer.substring(7);
        Map<String, Object> claims;
        try {
            claims = jwtUtil.parseClaims(token);
        } catch (Exception e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        ServerHttpRequest.Builder mutated = exchange.getRequest().mutate();
        // propagate common claims as headers
        if (claims.containsKey("userId")) mutated.header("X-User-Id", String.valueOf(claims.get("userId")));
        if (claims.containsKey("tenantId")) mutated.header("X-Tenant-Id", String.valueOf(claims.get("tenantId")));
        if (claims.containsKey("email")) mutated.header("X-User-Email", String.valueOf(claims.get("email")));
        if (claims.containsKey("roles")) mutated.header("X-User-Roles", String.valueOf(claims.get("roles")));

        ServerWebExchange mutatedExchange = exchange.mutate().request(mutated.build()).build();
        return chain.filter(mutatedExchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 5;
    }
}
