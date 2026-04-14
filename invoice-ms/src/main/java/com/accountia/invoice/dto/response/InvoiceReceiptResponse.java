package com.accountia.invoice.dto.response;

import com.accountia.invoice.domain.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * An invoice receipt as seen by its recipient.
 * Maps to the frontend's {@code InvoiceReceiptResponseDto} interface.
 */
public record InvoiceReceiptResponse(
        String id,                        // receipt UUID
        String invoiceId,                 // original invoice UUID
        String issuerTenantDatabaseName,
        String issuerBusinessId,
        String issuerBusinessName,
        String invoiceNumber,
        BigDecimal totalAmount,
        String currency,
        LocalDate issuedDate,
        LocalDate dueDate,
        InvoiceStatus invoiceStatus,
        boolean recipientViewed,
        Instant recipientViewedAt,
        Instant lastSyncedAt,
        Instant createdAt
) {}
