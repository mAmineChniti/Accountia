package com.accountia.invoice.config;

import com.accountia.invoice.security.AuditAwareImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing configuration.
 *
 * <p>{@code @EnableJpaAuditing} activates Spring's JPA Auditing, which
 * automatically fills {@code @CreatedDate}, {@code @LastModifiedDate},
 * {@code @CreatedBy}, and {@code @LastModifiedBy} annotated fields.
 *
 * <p>The {@link AuditAwareImpl} bean provides the current user's email
 * from the security context to the {@code @CreatedBy} / {@code @LastModifiedBy} fields.
 *
 * <p>Note: {@code @EnableJpaAuditing} is also declared on the main application class
 * for component scanning. This class wires the AuditorAware bean explicitly.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaConfig {

    /**
     * Provides the current authenticated user to JPA Auditing.
     * Spring calls this method each time an entity is created or modified.
     */
    @Bean
    public AuditorAware<String> auditorAware() {
        return new AuditAwareImpl();
    }
}
