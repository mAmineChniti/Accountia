package com.accountia.invoice.domain.entity;

import com.accountia.invoice.domain.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Immutable audit trail entry for every invoice status change.
 *
 * <p>A new row is inserted (never updated) each time an invoice transitions
 * to a new status. This provides full lifecycle visibility for compliance
 * and debugging — e.g. "who marked this PAID and when?"
 */
@Entity
@Table(name = "invoice_status_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    /** The invoice whose status changed. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    /** Previous status before this transition (null when the invoice was first created). */
    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 20)
    private InvoiceStatus oldStatus;

    /** Status after the transition. */
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 20)
    private InvoiceStatus newStatus;

    /** Email of the user (or "SYSTEM" for the overdue scheduler) who made the change. */
    @Column(name = "changed_by", nullable = false, length = 150)
    private String changedBy;

    /** Optional human-readable reason for the transition (e.g. dispute reason). */
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    /** When the change happened. */
    @Column(name = "changed_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant changedAt = Instant.now();
}
