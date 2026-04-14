package com.accountia.invoice.dto.response;

import java.util.List;

/**
 * Paginated list of issued invoices.
 *
 * <p>Maps to the frontend's {@code InvoiceListResponse} interface.
 * Uses custom pagination fields instead of Spring's Page wrapper because
 * the frontend expects {@code page} (1-based), not Spring's 0-based {@code number}.
 *
 * @param invoices      invoices on the current page
 * @param total         grand total of invoices across all pages (unfiltered)
 * @param filteredTotal total matching the current status/search filter
 * @param page          current page number (1-based, as sent by the frontend)
 * @param limit         page size requested
 * @param totalPages    total number of pages
 */
public record InvoiceListResponse(
        List<InvoiceResponse> invoices,
        long total,
        long filteredTotal,
        int page,
        int limit,
        int totalPages
) {}
