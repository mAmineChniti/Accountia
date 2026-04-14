package com.accountia.invoice.exception;

import com.accountia.invoice.domain.enums.InvoiceStatus;

/**
 * Thrown when a requested status transition is not allowed by the state machine.
 * Example: trying to move a PAID invoice back to DRAFT.
 */
public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(InvoiceStatus from, InvoiceStatus to) {
        super(String.format("Cannot transition invoice from %s to %s", from, to));
    }
}
