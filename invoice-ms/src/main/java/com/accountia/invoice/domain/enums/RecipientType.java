package com.accountia.invoice.domain.enums;

/**
 * Identifies whether the invoice recipient is outside the platform
 * or is a registered Accountia business / individual user.
 *
 * <ul>
 *   <li>{@code EXTERNAL} – a contact outside the platform (email + display name required)</li>
 *   <li>{@code PLATFORM_BUSINESS} – a registered Accountia business (looked up by email or platformId)</li>
 *   <li>{@code PLATFORM_INDIVIDUAL} – a registered Accountia user (looked up by email)</li>
 * </ul>
 *
 * When the type is {@code PLATFORM_BUSINESS} or {@code PLATFORM_INDIVIDUAL},
 * the service attempts to resolve the recipient's {@code platformId} and
 * creates an {@link com.accountia.invoice.domain.entity.InvoiceReceipt} in their inbox.
 */
public enum RecipientType {
    EXTERNAL,
    PLATFORM_BUSINESS,
    PLATFORM_INDIVIDUAL
}
