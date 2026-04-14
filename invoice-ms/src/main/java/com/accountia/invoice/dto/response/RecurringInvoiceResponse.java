package com.accountia.invoice.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * A recurring invoice schedule as returned by the API.
 * Maps to the frontend's {@code RecurringInvoice} interface.
 */
public record RecurringInvoiceResponse(
        String id,
        String businessId,
        String name,
        String frequency,    // "daily" | "weekly" | "monthly" | "quarterly" | "yearly"
        String status,       // "active" | "paused" | "cancelled" | "completed"
        LocalDate startDate,
        String endCondition,
        Integer maxOccurrences,
        int occurrenceCount,
        LocalDate endDate,
        Instant nextRunAt,
        Instant lastRunAt,
        List<RecurringLineItemResponse> lineItems,
        BigDecimal totalAmount,
        String currency,
        int dueDaysFromIssue,
        Map<String, Object> recipient,
        String description,
        String paymentTerms,
        boolean autoIssue,
        List<String> generatedInvoiceIds,
        String createdBy,
        Instant createdAt,
        Instant updatedAt
) {}
