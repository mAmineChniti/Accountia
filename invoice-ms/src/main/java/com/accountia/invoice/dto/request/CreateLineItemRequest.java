package com.accountia.invoice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * One line item inside a CreateInvoiceRequest.
 * Amount (quantity × unitPrice) is computed server-side.
 */
public record CreateLineItemRequest(

        @NotBlank(message = "Product ID is required")
        String productId,

        @NotBlank(message = "Product name is required")
        String productName,

        @NotNull(message = "Quantity is required")
        @Min(value = 0, message = "Quantity must be non-negative")
        BigDecimal quantity,

        @NotNull(message = "Unit price is required")
        @PositiveOrZero(message = "Unit price must be non-negative")
        BigDecimal unitPrice,

        /** Optional description for this specific line (overrides product description). */
        String description
) {}
