package com.accountia.invoice.dto.request;

import com.accountia.invoice.domain.enums.RecipientType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

/**
 * Recipient sub-object inside CreateInvoiceRequest.
 *
 * <p>Business rules (validated in InvoiceService, not just annotations):
 * <ul>
 *   <li>EXTERNAL: email + displayName required.</li>
 *   <li>PLATFORM_BUSINESS / PLATFORM_INDIVIDUAL: email required for lookup; platformId optional.</li>
 * </ul>
 */
public record CreateRecipientRequest(

        @NotNull(message = "Recipient type is required")
        RecipientType type,

        @Email(message = "Valid email is required")
        @NotNull(message = "Recipient email is required")
        String email,

        /** Required for EXTERNAL recipients. Optional for platform types. */
        String displayName,

        /** Optional: pre-resolved platform user/business UUID. */
        String platformId
) {}
