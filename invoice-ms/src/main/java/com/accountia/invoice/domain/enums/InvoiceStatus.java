package com.accountia.invoice.domain.enums;

import java.util.EnumSet;
import java.util.Set;

/**
 * Lifecycle states of an invoice.
 *
 * <p>State machine (enforced in InvoiceService.transition):
 * <pre>
 *   DRAFT  ──► ISSUED  ──► VIEWED  ──► PAID ──► ARCHIVED
 *          └──► VOIDED  └──► PARTIAL ──► PAID
 *                        └──► OVERDUE ──► PAID | DISPUTED
 *                        └──► DISPUTED ──► PAID | VOIDED
 *                        └──► VOIDED ──► ARCHIVED
 * </pre>
 *
 * <p>OVERDUE is also set automatically by the nightly scheduler
 * when dueDate < today and status is still ISSUED or VIEWED.
 */
public enum InvoiceStatus {

    /** Created but not yet sent to the recipient. Editable. */
    DRAFT,

    /** Sent/issued to the recipient. Recipient notified. */
    ISSUED,

    /** Recipient has opened/viewed the invoice. */
    VIEWED,

    /** Fully paid. */
    PAID,

    /** Partially paid (amountPaid < totalAmount). */
    PARTIAL,

    /** Past the due date without full payment. Set by scheduler. */
    OVERDUE,

    /** Recipient raised a dispute. */
    DISPUTED,

    /** Cancelled / voided. No further payments expected. */
    VOIDED,

    /** Closed for archival. Terminal state. */
    ARCHIVED;

    /**
     * Returns the set of statuses reachable from this status.
     * Used by the service layer to validate transition requests.
     *
     * @return immutable set of valid next statuses
     */
    public Set<InvoiceStatus> allowedTransitions() {
        return switch (this) {
            case DRAFT    -> EnumSet.of(ISSUED, VOIDED);
            case ISSUED   -> EnumSet.of(VIEWED, PAID, OVERDUE, DISPUTED, VOIDED);
            case VIEWED   -> EnumSet.of(PAID, PARTIAL, OVERDUE, DISPUTED, VOIDED);
            case PAID     -> EnumSet.of(ARCHIVED);
            case PARTIAL  -> EnumSet.of(PAID, OVERDUE, DISPUTED, VOIDED);
            case OVERDUE  -> EnumSet.of(PAID, PARTIAL, DISPUTED, VOIDED);
            case DISPUTED -> EnumSet.of(PAID, VOIDED);
            case VOIDED   -> EnumSet.of(ARCHIVED);
            case ARCHIVED -> EnumSet.noneOf(InvoiceStatus.class); // terminal
        };
    }

    /** Returns true if the transition to {@code target} is valid from this status. */
    public boolean canTransitionTo(InvoiceStatus target) {
        return allowedTransitions().contains(target);
    }
}
