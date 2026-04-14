package com.accountia.invoice.dto.request;

import com.accountia.invoice.domain.enums.InvoiceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * Request body for {@code POST /invoices/issued/{id}/transition}.
 *
 * <p>Maps to the frontend's {@code TransitionInvoiceInput} type.
 * {@code amountPaid} is required when transitioning to PARTIAL.
 * The service validates that amountPaid < totalAmount for PARTIAL
 * and sets amountPaid = totalAmount for PAID.
 */
public record TransitionInvoiceRequest(

        @NotBlank(message = "businessId is required")
        String businessId,

        @NotNull(message = "newStatus is required")
        InvoiceStatus newStatus,

        /** How much has been paid — used for PARTIAL and PAID transitions. */
        @PositiveOrZero
        BigDecimal amountPaid,

        /** Optional reason for the transition (shown in status history). */
        String reason
) {}
