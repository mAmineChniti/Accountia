package com.example.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
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

        @Order(1)
        @Bean
        public SecurityWebFilterChain microservicesSecurityFilterChain(ServerHttpSecurity http) {
                http
                                .securityMatcher(
                                                org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
                                                                .pathMatchers(
                                                                                "/api/auth/**", "/api/business/**",
                                                                                "/api/client/**", "/api/expense/**",
                                                                                "/api/invoice/**", "/api/reporting/**",
                                                                                "/api/*/health", "/actuator/health/**",
                                                                                "/swagger-ui/**", "/v3/api-docs/**"))
                                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                                .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll());

                return http.build();
        }

        @Order(2)
        @Bean
        public SecurityWebFilterChain defaultSecurityFilterChain(ServerHttpSecurity http) {
                http
                                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                                .authorizeExchange(exchanges -> exchanges.anyExchange().authenticated())
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                                                new ReactiveJwtAuthenticationConverterAdapter(
                                                                                jwtAuthenticationConverter()))));

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