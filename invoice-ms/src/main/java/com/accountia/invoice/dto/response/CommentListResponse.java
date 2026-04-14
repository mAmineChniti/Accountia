package com.accountia.invoice.dto.response;

import java.util.List;

/**
 * List of comments for a given entity.
 * Maps to the frontend's {@code CommentListResponse} interface.
 */
public record CommentListResponse(
        String entityId,
        String entityType,
        List<CommentResponse> comments
) {}
