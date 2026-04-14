package com.accountia.invoice.domain.entity;

import com.accountia.invoice.domain.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * A receipt represents an invoice as seen by its recipient.
 *
 * <p>When a PLATFORM_BUSINESS or PLATFORM_INDIVIDUAL invoice is issued,
 * the service creates a receipt in the recipient's inbox. This lets
 * recipients see the invoice in their "received" list without having
 * direct access to the issuer's invoice record.
 *
 * <p>Key fields:
 * <ul>
 *   <li>{@code recipientBusinessId} — set when recipient is a platform business.</li>
 *   <li>{@code recipientUserId} — set when recipient is a platform individual.</li>
 *   <li>{@code recipientViewed} — flipped to true when the recipient opens the invoice.</li>
 *   <li>{@code invoiceStatus} — synced from the parent invoice on each status change.</li>
 * </ul>
 */
@Entity
@Table(name = "invoice_receipts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceReceipt {

    @Id
    @UuidGenerator
    @Column(name = "id", length = 36, updatable = false, nullable = false)
    private String id;

    /** The original invoice this receipt was created from. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    /** Issuer's tenant database name (for cross-tenant display). */
    @Column(name = "issuer_tenant_db_name", nullable = false, length = 100)
    private String issuerTenantDatabaseName;

    @Column(name = "issuer_business_id", nullable = false, length = 36)
    private String issuerBusinessId;

    /** Issuer's business display name (snapshot at time of issue). */
    @Column(name = "issuer_business_name", nullable = false, length = 150)
    private String issuerBusinessName;

    /** Duplicated for quick display without joining the invoice table. */
    @Column(name = "invoice_number", nullable = false, length = 30)
    private String invoiceNumber;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 3)
    private BigDecimal totalAmount;

    @Column(name = "currency", nullable = false, length = 5)
    private String currency;

    @Column(name = "issued_date", nullable = false)
    private LocalDate issuedDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    /** Kept in sync with the parent Invoice status when it changes. */
    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_status", nullable = false, length = 20)
    private InvoiceStatus invoiceStatus;

    /** True once the recipient has fetched the invoice details. */
    @Column(name = "recipient_viewed", nullable = false)
    @Builder.Default
    private boolean recipientViewed = false;

    @Column(name = "recipient_viewed_at")
    private Instant recipientViewedAt;

    /** Set when recipient type is PLATFORM_BUSINESS. Used to filter by businessId. */
    @Column(name = "recipient_business_id", length = 36)
    private String recipientBusinessId;

    /** Set when recipient type is PLATFORM_INDIVIDUAL. Used to filter by userId. */
    @Column(name = "recipient_user_id", length = 36)
    private String recipientUserId;

    /** When this receipt's data was last synced from the parent invoice. */
    @Column(name = "last_synced_at", nullable = false)
    @Builder.Default
    private Instant lastSyncedAt = Instant.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
