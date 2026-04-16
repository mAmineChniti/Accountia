package com.accountia.invoice.config;

import com.accountia.invoice.security.JwtAuthenticationFilter;
import com.accountia.invoice.util.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("!ci")
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil);

        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/invoice/register", "/api/invoice/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/invoice/health").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/invoice/password-reset/**").permitAll()
                .requestMatchers("/actuator/**").authenticated()
                .requestMatchers("/api/invoice/**").authenticated()
                .anyRequest().authenticated()
            )
            // OAuth2 Resource Server for Keycloak tokens
            .oauth2ResourceServer(oauth2 -> oauth2
                    .bearerTokenResolver(request -> {
                        String header = request.getHeader("Authorization");
                        if (header != null && header.startsWith("Bearer ")) {
                            String token = header.substring(7);
                            // If it's an RS token (Keycloak), let OAuth2 handle it
                            if (token.startsWith("eyJhbGciOiJSUz")) {
                                return token;
                            }
                        }
                        return null;
                    })
                    .jwt(org.springframework.security.config.Customizer.withDefaults())
            )
            .addFilterBefore(jwtAuthenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public org.springframework.security.authentication.AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
