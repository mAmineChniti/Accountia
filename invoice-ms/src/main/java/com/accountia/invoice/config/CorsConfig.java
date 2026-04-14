package com.accountia.invoice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * Cross-Origin Resource Sharing (CORS) configuration.
 *
 * <p>The Next.js frontend runs on {@code localhost:3000} (dev) and the production
 * domain. We allow it to call this service directly in Sprint 1.
 * In Sprint 2, CORS will be handled at the API Gateway level instead.
 *
 * <p>All origins are allowed for development simplicity. In production,
 * replace {@code allowedOriginPatterns("*")} with the specific frontend URL.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow the Next.js frontend (and any other origin in dev)
        config.setAllowedOriginPatterns(List.of("*"));

        // Methods used by the frontend service layer
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Headers sent by the frontend (Authorization carries the JWT)
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));

        // Expose Authorization header so the frontend can read it on redirect responses
        config.setExposedHeaders(List.of("Authorization"));

        // Allow cookies / auth headers (needed for Bearer token flow)
        config.setAllowCredentials(true);

        // Cache preflight response for 1 hour to reduce OPTIONS overhead
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
