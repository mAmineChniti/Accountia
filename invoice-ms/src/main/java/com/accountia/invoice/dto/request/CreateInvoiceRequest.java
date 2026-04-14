package com.accountia.invoice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * Request body for {@code POST /invoices}.
 *
 * <p>Maps exactly to the frontend's {@code CreateInvoiceInput} TypeScript type.
 * Java records are immutable and do not need Lombok — ideal for request DTOs.
 */
public record CreateInvoiceRequest(

        @NotBlank(message = "businessId is required")
        String businessId,

        @NotNull(message = "issuedDate is required")
        LocalDate issuedDate,

        @NotNull(message = "dueDate is required")
        LocalDate dueDate,

        @NotBlank(message = "currency is required")
        String currency,

        /** Free-text description (optional). */
        String description,

        /** Payment terms string, e.g. "Net 30" (optional). */
        String paymentTerms,

        @NotNull(message = "recipient is required")
        @Valid
        CreateRecipientRequest recipient,

        @NotEmpty(message = "At least one line item is required")
        @Valid
        List<CreateLineItemRequest> lineItems
) {}
