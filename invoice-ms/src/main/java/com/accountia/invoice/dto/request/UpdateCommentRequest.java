package com.accountia.invoice.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code PATCH /comments/{id}}.
 */
public record UpdateCommentRequest(

        @NotBlank(message = "businessId is required")
        String businessId,

        @NotBlank(message = "Comment body is required")
        String body
) {}
