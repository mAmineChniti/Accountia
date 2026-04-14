package com.accountia.invoice.dto.response;

import java.time.Instant;
import java.util.List;

/**
 * Summary returned after a bulk invoice import operation.
 * Maps to the frontend's {@code BulkImportInvoicesResponseDto} interface.
 */
public record BulkImportResponse(
        int totalRecords,
        int successCount,
        int failedCount,
        int warningCount,
        List<BulkImportResultItem> results,
        Instant importStartedAt,
        Instant importCompletedAt,
        long processingTimeMs
) {}
