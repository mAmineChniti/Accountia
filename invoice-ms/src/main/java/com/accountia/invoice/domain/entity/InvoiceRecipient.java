package com.accountia.invoice.domain.entity;

import com.accountia.invoice.domain.enums.RecipientResolutionStatus;
import com.accountia.invoice.domain.enums.RecipientType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Embedded recipient block stored inside the {@code invoices} table.
 *
 * <p>Using {@code @Embeddable} (not a separate table) because:
 * <ol>
 *   <li>Recipient info does not change after invoice creation.</li>
 *   <li>Avoids a JOIN on every invoice read.</li>
 *   <li>The "clients" microservice (owned by another team) owns the source of truth;
 *       we only store a denormalized snapshot here.</li>
 * </ol>
 */
@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceRecipient {

    /** Distinguishes external contacts from registered platform users. */
    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_type", nullable = false, length = 30)
    private RecipientType type;

    /** UUID of the business/user on this platform (null for EXTERNAL). */
    @Column(name = "recipient_platform_id", length = 36)
    private String platformId;

    /** Database name of the recipient's tenant schema (set after resolution). */
    @Column(name = "recipient_tenant_db_name", length = 100)
    private String tenantDatabaseName;

    /** Email address — used for platform lookup and external display. */
    @Column(name = "recipient_email", length = 150)
    private String email;

    /** Human-readable name (required for EXTERNAL, optional for platform types). */
    @Column(name = "recipient_display_name", length = 150)
    private String displayName;

    /** Whether the platform found an account matching this recipient's email. */
    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_resolution_status", length = 20)
    private RecipientResolutionStatus resolutionStatus;

    /** When the last resolution attempt was made (for retry scheduling). */
    @Column(name = "recipient_last_resolution_attempt")
    private Instant lastResolutionAttempt;
}
