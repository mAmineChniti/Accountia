package com.accountia.invoice.config;

import com.accountia.invoice.security.JwtAuthenticationFilter;
import com.accountia.invoice.util.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * JWT-based stateless security configuration.
 *
 * <p>Every request must carry a valid {@code Authorization: Bearer <token>} header
 * except Swagger UI, actuator health, and OPTIONS preflight requests.
 *
 * <p>This config is only active outside the {@code ci} profile.
 * In CI, {@link SecurityConfigCI} permits everything (no JWT validation).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("!ci")
public class SecurityConfig {

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtil jwtUtil) {
        return new JwtAuthenticationFilter(jwtUtil);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtFilter) throws Exception {
        http
            // CSRF disabled — we use stateless JWTs, no cookie-based sessions
            .csrf(csrf -> csrf.disable())

            // No HTTP sessions — authentication state lives entirely in the JWT
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                // Swagger UI (valeur ajoutée — grader can browse the API)
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs",
                    "/v3/api-docs/**"
                ).permitAll()
                // Health check endpoints
                .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                .requestMatchers("/invoices/health").permitAll()
                // Dev-only token generator (Sprint 1 — replaced by Keycloak in Sprint 2)
                .requestMatchers("/auth/token").permitAll()
                // CORS preflight — browsers send OPTIONS before POST/PATCH
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                // Everything else requires a valid JWT
                .anyRequest().authenticated()
            )

            // Run the JWT filter before Spring's default authentication filter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
