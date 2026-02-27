package com.example.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * JWT-based security configuration for API Gateway.
 * Uses Spring Security 6 with WebFlux for reactive stack.
 * 
 * Note: This config is NOT loaded in CI profile - see SecurityConfigCI for CI mode.
 */
@Configuration
@EnableWebFluxSecurity
@Profile("!ci")
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                // Health endpoints public
                .pathMatchers("/actuator/health", "/actuator/health/**").permitAll()
                // Swagger/OpenAPI endpoints public
                .pathMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // Auth endpoints public
                .pathMatchers("/api/auth/**").permitAll()
                // All other API endpoints require authentication
                .pathMatchers("/api/**").authenticated()
                // Any other requests authenticated
                .anyExchange().authenticated()
            );
        
        return http.build();
    }
}
