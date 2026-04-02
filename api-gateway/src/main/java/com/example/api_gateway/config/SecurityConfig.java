package com.example.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Security config de l'API Gateway avec validation JWT Keycloak.
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
                        // Endpoints publics - authentification
                        .pathMatchers("/api/auth/**").permitAll()
                        // Health checks publics pour tous les microservices
                        .pathMatchers("/api/*/health", "/actuator/health", "/actuator/health/**").permitAll()
                        // Swagger/OpenAPI
                        .pathMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Tous les autres endpoints nécessitent un token Keycloak valide
                        .anyExchange().authenticated()
                )
                // OAuth2 Resource Server avec validation JWT Keycloak
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {})
                );

        return http.build();
    }
}