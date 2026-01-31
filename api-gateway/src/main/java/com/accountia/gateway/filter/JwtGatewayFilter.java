package com.accountia.gateway.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class JwtGatewayFilter implements GlobalFilter, Ordered {

    private final Algorithm algorithm;

    public JwtGatewayFilter(@Value("${security.jwt.secret:changeitchangeitchangeitchangeit}") String secret) {
        this.algorithm = Algorithm.HMAC256(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (path.startsWith("/api/auth") || path.startsWith("/actuator")) {
            return chain.filter(exchange);
        }
        String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        String token = auth.substring(7);
        try {
            DecodedJWT jwt = JWT.require(algorithm).build().verify(token);
            Object tenant = jwt.getClaim("tenantId").as(Object.class);
            Object userId = jwt.getClaim("userId").as(Object.class);
            Object roles = jwt.getClaim("roles").as(Object.class);
            ServerHttpRequest req = exchange.getRequest().mutate()
                    .header("X-Tenant-Id", String.valueOf(tenant))
                    .header("X-User-Id", String.valueOf(userId))
                    .header("X-Roles", String.valueOf(roles))
                    .build();
            return chain.filter(exchange.mutate().request(req).build());
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
