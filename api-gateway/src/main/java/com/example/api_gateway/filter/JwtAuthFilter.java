package com.example.api_gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import reactor.core.publisher.Mono;

/**
 * Global filter that propagates user claims from JWT token to downstream services.
 * Works with OAuth2 Resource Server security configuration.
 */
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {
    private final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
            .map(securityContext -> securityContext.getAuthentication())
            .filter(auth -> auth instanceof JwtAuthenticationToken)
            .cast(JwtAuthenticationToken.class)
            .flatMap(jwtAuth -> {
                var jwt = jwtAuth.getToken();
                ServerHttpRequest.Builder mutated = exchange.getRequest().mutate();
                
                // Propagate common claims as headers to downstream services
                String subject = jwt.getSubject();
                if (subject != null) {
                    mutated.header("X-User-Id", subject);
                }
                
                String email = jwt.getClaimAsString("email");
                if (email != null) {
                    mutated.header("X-User-Email", email);
                }
                
                String preferredUsername = jwt.getClaimAsString("preferred_username");
                if (preferredUsername != null) {
                    mutated.header("X-User-Name", preferredUsername);
                }
                
                // Extract roles from authorities
                String roles = jwtAuth.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .filter(role -> role.startsWith("ROLE_"))
                    .map(role -> role.substring(5))
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");
                if (!roles.isEmpty()) {
                    mutated.header("X-User-Roles", roles);
                }
                
                ServerWebExchange mutatedExchange = exchange.mutate().request(mutated.build()).build();
                return chain.filter(mutatedExchange);
            })
            .switchIfEmpty(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 5;
    }
}
