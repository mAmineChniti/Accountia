package com.accountia.invoice.domain.enums;

/**
 * Tracks whether the platform could find an account matching the recipient's email.
 *
 * <ul>
 *   <li>{@code RESOLVED}      – platformId was found and stored</li>
 *   <li>{@code PENDING}       – lookup not yet attempted</li>
 *   <li>{@code CLAIMED}       – the user has since registered; linked</li>
 *   <li>{@code NEVER_RESOLVED}– no matching account found after retries</li>
 * </ul>
 */
public enum RecipientResolutionStatus {
    RESOLVED,
    PENDING,
    CLAIMED,
    NEVER_RESOLVED
}
