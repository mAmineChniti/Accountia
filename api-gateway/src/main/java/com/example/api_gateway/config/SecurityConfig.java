package com.example.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Security configuration for API Gateway with OAuth2 Resource Server (Keycloak).
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
                // Public endpoints - no authentication required
                .pathMatchers("/api/auth/**").permitAll()
                // Only health endpoint public (for K8s probes)
                .pathMatchers("/actuator/health", "/actuator/health/**").permitAll()
                // Other actuator endpoints require authentication
                .pathMatchers("/actuator/**").authenticated()
                .pathMatchers("/api/public/**").permitAll()
                // Require authentication for all other endpoints
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(grantedAuthoritiesExtractor()))
            );
        
        return http.build();
    }

    /**
     * Extracts roles from Keycloak JWT token.
     * Keycloak stores roles in realm_access.roles and resource_access.{client}.roles
     */
    private Converter<Jwt, Mono<AbstractAuthenticationToken>> grantedAuthoritiesExtractor() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }

    /**
     * Converter that extracts Keycloak roles from JWT claims.
     * Extracts both realm roles from realm_access.roles and client roles from resource_access.{client}.roles.
     */
    static class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        
        @Override
        @SuppressWarnings("unchecked")
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            List<String> allRoles = new ArrayList<>();
            
            // Extract realm roles from Keycloak token
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null && !realmAccess.isEmpty()) {
                Object rolesObj = realmAccess.get("roles");
                if (rolesObj instanceof Collection) {
                    ((Collection<?>) rolesObj).forEach(role -> {
                        if (role instanceof String) {
                            allRoles.add((String) role);
                        }
                    });
                }
            }
            
            // Extract client roles from resource_access.{client}.roles
            Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
            if (resourceAccess != null && !resourceAccess.isEmpty()) {
                for (Object clientValue : resourceAccess.values()) {
                    if (clientValue instanceof Map) {
                        Map<String, Object> clientAccess = (Map<String, Object>) clientValue;
                        Object clientRolesObj = clientAccess.get("roles");
                        if (clientRolesObj instanceof Collection) {
                            ((Collection<?>) clientRolesObj).forEach(role -> {
                                if (role instanceof String) {
                                    allRoles.add((String) role);
                                }
                            });
                        }
                    }
                }
            }
            
            if (allRoles.isEmpty()) {
                return Collections.emptyList();
            }
            
            return allRoles.stream()
                    .distinct()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }
    }
}
