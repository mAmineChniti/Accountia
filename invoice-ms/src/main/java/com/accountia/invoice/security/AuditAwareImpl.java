package com.accountia.invoice.security;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Provides the current user's identity to JPA Auditing.
 *
 * <p>When Spring saves/updates an entity annotated with {@code @CreatedBy}
 * or {@code @LastModifiedBy}, it calls {@link #getCurrentAuditor()} to get
 * the value to store. We return the authenticated user's email (JWT subject).
 *
 * <p>This bean is activated by {@code @EnableJpaAuditing} in InvoiceApplication.
 */
@Component
public class AuditAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // If no authentication (e.g. CI tests or public endpoints), use "SYSTEM"
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.of("SYSTEM");
        }

        // The JWT filter sets the email as the principal name
        String principal = auth.getName();
        return Optional.ofNullable(principal).filter(s -> !s.isBlank());
    }
}
