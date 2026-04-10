package com.example.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
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
                        // Lecture: USER ou ADMIN sur tous les domaines métier
                        .pathMatchers(HttpMethod.GET, "/api/business/**", "/api/client/**", "/api/expense/**", "/api/invoice/**", "/api/reporting/**").hasAnyRole("USER", "ADMIN")
                        // Ecriture métier: USER ou ADMIN (ownership vérifiée dans les microservices)
                        .pathMatchers(HttpMethod.POST, "/api/business/**", "/api/client/**", "/api/expense/**").hasAnyRole("USER", "ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/business/**", "/api/client/**", "/api/expense/**").hasAnyRole("USER", "ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/business/**", "/api/client/**", "/api/expense/**").hasAnyRole("USER", "ADMIN")
                        // Invoice / reporting: écriture réservée ADMIN
                        .pathMatchers(HttpMethod.POST, "/api/invoice/**", "/api/reporting/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/invoice/**", "/api/reporting/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/invoice/**", "/api/reporting/**").hasRole("ADMIN")
                        // Tous les autres endpoints nécessitent un token Keycloak valide
                        .anyExchange().authenticated()
                )
                // OAuth2 Resource Server avec validation JWT Keycloak
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter())
                        ))
                );

        return http.build();
    }

        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {
                JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
                converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
                return converter;
        }

        @Bean
        public ReactiveJwtDecoder jwtDecoder(
                        @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkSetUri) {
                return NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();
        }
}