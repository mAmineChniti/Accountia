package com.accountia.invoice.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * Request body for {@code POST /comments}.
 * Maps to the CommentsService.createComment call from the frontend.
 */
public record CreateCommentRequest(

        @NotBlank(message = "businessId is required")
        String businessId,

        @NotBlank(message = "entityType is required")
        String entityType,   // "invoice" | "expense" | "purchase_order"

        @NotBlank(message = "entityId is required")
        String entityId,

        @NotBlank(message = "Comment body is required")
        String body,

        /** UUID of the parent comment for threaded replies. Null for top-level. */
        String parentId,

        /** List of mentioned user emails/IDs. */
        List<String> mentions
) {}
