package com.accountia.invoice.dto.response;

import java.math.BigDecimal;

/** A line item inside a recurring invoice schedule response. */
public record RecurringLineItemResponse(
        String productId,
        String productName,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal amount,
        String description
) {}
