package com.accountia.business.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight DTO returned by the internal business endpoint.
 * Used by other microservices (e.g. expense-ms via Feign) to validate businessId.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessSummaryDTO {

    /** Unique identifier of the business. */
    private String id;

    /** Display name of the business. */
    private String name;

    /** Whether the business is currently active. */
    private boolean active;
}
