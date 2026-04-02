package com.example.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * CI-specific security configuration for API Gateway.
 * Permits all requests without OAuth2/JWT validation for smoke testing.
 * 
 * This configuration is only active when the "ci" profile is enabled.
 * In production, SecurityConfig is used instead with full OAuth2 security.
 */
@Configuration
@EnableWebFluxSecurity
@Profile("ci")
public class SecurityConfigCI {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                // Permit all requests in CI mode for smoke testing
                .anyExchange().permitAll()
            );
        
        return http.build();
    }
}
