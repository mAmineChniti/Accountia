package com.accountia.invoice.dto.response;

import com.accountia.invoice.domain.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Full invoice data returned by GET/POST/PATCH endpoints.
 *
 * <p>Field names use camelCase to match the frontend's TypeScript interface
 * {@code InvoiceResponse}. Jackson serializes Java records to JSON automatically.
 */
public record InvoiceResponse(
        String id,
        String issuerBusinessId,
        String invoiceNumber,
        InvoiceStatus status,
        BigDecimal totalAmount,
        String currency,
        BigDecimal amountPaid,
        LocalDate issuedDate,
        LocalDate dueDate,
        String description,
        String paymentTerms,
        InvoiceRecipientResponse recipient,
        List<InvoiceLineItemResponse> lineItems,
        String createdBy,
        String lastModifiedBy,
        Instant lastStatusChangeAt,
        Instant createdAt,
        Instant updatedAt
) {}
