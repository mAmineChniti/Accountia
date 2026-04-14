package com.accountia.invoice.domain.entity;

import com.accountia.invoice.domain.enums.RecipientType;
import com.accountia.invoice.domain.enums.RecurringFrequency;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * A recurring invoice schedule that auto-generates invoices on a set frequency.
 *
 * <p>Line items and generated invoice IDs are stored as JSON text blobs for simplicity
 * (avoids extra join tables for what are relatively small payloads).
 * The service layer serializes/deserializes them using Jackson.
 *
 * <p>The {@code @EnableScheduling} nightly job checks {@code nextRunAt}
 * and fires the invoice generation when the date is due.
 */
@Entity
@Table(name = "recurring_invoices")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringInvoice {

    @Id
    @UuidGenerator
    @Column(name = "id", length = 36, updatable = false, nullable = false)
    private String id;

    @Column(name = "business_id", nullable = false, length = 36)
    private String businessId;

    /** Human-readable schedule name (e.g. "Monthly Hosting Fee"). */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false, length = 20)
    private RecurringFrequency frequency;

    /** active | paused | cancelled | completed */
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "active";

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /** never | after_occurrences | by_date */
    @Column(name = "end_condition", nullable = false, length = 30)
    @Builder.Default
    private String endCondition = "never";

    @Column(name = "max_occurrences")
    private Integer maxOccurrences;

    @Column(name = "occurrence_count", nullable = false)
    @Builder.Default
    private int occurrenceCount = 0;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "next_run_at")
    private Instant nextRunAt;

    @Column(name = "last_run_at")
    private Instant lastRunAt;

    /** Computed from line items — stored for quick display. */
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "currency", nullable = false, length = 5)
    @Builder.Default
    private String currency = "TND";

    /** Number of days after issuedDate before the generated invoice is due. */
    @Column(name = "due_days_from_issue", nullable = false)
    @Builder.Default
    private int dueDaysFromIssue = 30;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "payment_terms", length = 100)
    private String paymentTerms;

    /** If true, generated invoices are auto-transitioned from DRAFT to ISSUED. */
    @Column(name = "auto_issue", nullable = false)
    @Builder.Default
    private boolean autoIssue = true;

    // ── Recipient (same structure as Invoice, stored flat) ────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_type", nullable = false, length = 30)
    private RecipientType recipientType;

    @Column(name = "recipient_platform_id", length = 36)
    private String recipientPlatformId;

    @Column(name = "recipient_email", length = 150)
    private String recipientEmail;

    @Column(name = "recipient_display_name", length = 150)
    private String recipientDisplayName;

    // ── JSON blobs ────────────────────────────────────────────────────────────

    /** JSON array: [{productId, productName, quantity, unitPrice, amount, description}] */
    @Column(name = "line_items_json", columnDefinition = "TEXT")
    private String lineItemsJson;

    /** JSON array of generated invoice UUIDs: ["uuid1","uuid2",...] */
    @Column(name = "generated_invoice_ids", columnDefinition = "TEXT")
    @Builder.Default
    private String generatedInvoiceIds = "[]";

    @Column(name = "created_by", length = 150)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();
}
