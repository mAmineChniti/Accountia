package com.accountia.invoice.dto.response;

import java.util.List;

/**
 * Paginated list of received invoice receipts.
 * Maps to the frontend's {@code ReceivedInvoiceListResponse} interface.
 */
public record ReceivedInvoiceListResponse(
        List<InvoiceReceiptResponse> receipts,
        long total,
        long filteredTotal,
        int page,
        int limit,
        int totalPages
) {}
