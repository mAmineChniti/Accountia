package com.accountia.invoice.dto.response;

import java.util.List;

/**
 * Paginated list of recurring invoice schedules.
 * Maps to the frontend's {@code RecurringInvoiceListResponse} interface.
 */
public record RecurringInvoiceListResponse(
        List<RecurringInvoiceResponse> schedules,
        long total,
        int page,
        int limit,
        int totalPages
) {}
