package com.accountia.invoice.dto.response;

import java.util.List;

/**
 * Per-row result from a bulk invoice import.
 * Maps to the frontend's {@code BulkImportResultItem} interface.
 */
public record BulkImportResultItem(
        int itemNumber,         // 1-based row number in the file
        boolean success,
        String message,
        String itemId,          // created invoice UUID (null if failed)
        List<String> errors     // validation error messages
) {}
