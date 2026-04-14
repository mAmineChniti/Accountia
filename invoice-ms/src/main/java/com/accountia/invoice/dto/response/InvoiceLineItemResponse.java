package com.accountia.invoice.dto.response;

import java.math.BigDecimal;

/**
 * Line item as returned by the API.
 * Maps to the frontend's {@code InvoiceLineItemResponseDto} interface.
 */
public record InvoiceLineItemResponse(
        String id,
        String productId,
        String productName,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal amount,      // quantity * unitPrice
        String description
) {}
