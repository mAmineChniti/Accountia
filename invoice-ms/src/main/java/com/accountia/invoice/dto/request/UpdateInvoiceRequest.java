package com.accountia.invoice.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

/**
 * Request body for {@code PATCH /invoices/issued/{id}}.
 *
 * <p>Only description, paymentTerms, and dueDate can be updated
 * (and only while the invoice is still DRAFT).
 * The frontend's {@code UpdateInvoiceInput} type maps to this exactly.
 */
public record UpdateInvoiceRequest(

        @NotBlank(message = "businessId is required")
        String businessId,

        /** New description — null means "do not change". */
        String description,

        /** New payment terms — null means "do not change". */
        String paymentTerms,

        /** New due date — null means "do not change". */
        LocalDate dueDate
) {}
