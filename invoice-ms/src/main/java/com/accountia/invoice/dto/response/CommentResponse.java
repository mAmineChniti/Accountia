package com.accountia.invoice.dto.response;

import java.time.Instant;
import java.util.List;

/**
 * A single comment returned by the API.
 * Maps to the frontend's {@code Comment} interface.
 */
public record CommentResponse(
        String id,
        String businessId,
        String entityType,
        String entityId,
        String authorId,
        String authorName,
        String body,
        String parentId,
        List<String> mentions,
        boolean isEdited,
        boolean isDeleted,
        Instant createdAt,
        Instant updatedAt
) {}
