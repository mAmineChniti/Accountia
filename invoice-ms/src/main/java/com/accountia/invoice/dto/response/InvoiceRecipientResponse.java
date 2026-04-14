package com.accountia.invoice.dto.response;

import com.accountia.invoice.domain.enums.RecipientResolutionStatus;
import com.accountia.invoice.domain.enums.RecipientType;

import java.time.Instant;

/**
 * Recipient data returned inside an invoice response.
 * Maps to the frontend's {@code InvoiceRecipientResponseDto} interface.
 */
public record InvoiceRecipientResponse(
        RecipientType type,
        String platformId,
        String tenantDatabaseName,
        String email,
        String displayName,
        RecipientResolutionStatus resolutionStatus,
        Instant lastResolutionAttempt
) {}
