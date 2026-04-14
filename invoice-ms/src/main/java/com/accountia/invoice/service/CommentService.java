package com.accountia.invoice.service;

import com.accountia.invoice.domain.entity.InvoiceComment;
import com.accountia.invoice.dto.request.CreateCommentRequest;
import com.accountia.invoice.dto.request.UpdateCommentRequest;
import com.accountia.invoice.dto.response.CommentListResponse;
import com.accountia.invoice.dto.response.CommentResponse;
import com.accountia.invoice.exception.ResourceNotFoundException;
import com.accountia.invoice.repository.InvoiceCommentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Manages threaded comments on invoices (and other entity types).
 *
 * <p>Comments are soft-deleted (isDeleted flag) so thread structure is preserved
 * even when a specific comment is removed. The list response filters out deleted comments.
 *
 * <p>Authorization: a user can only edit/delete their own comments
 * (checked via authorId == currentUserEmail). Admins bypass this in Sprint 2.
 */
@Service
public class CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentService.class);

    private final InvoiceCommentRepository commentRepository;
    private final InvoiceMapper mapper;

    public CommentService(InvoiceCommentRepository commentRepository, InvoiceMapper mapper) {
        this.commentRepository = commentRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public CommentListResponse getComments(String businessId, String entityType, String entityId) {
        List<InvoiceComment> comments = commentRepository.findActiveComments(
                businessId, entityType, entityId);
        return new CommentListResponse(
                entityId,
                entityType,
                comments.stream().map(mapper::toCommentResponse).toList()
        );
    }

    @Transactional
    public CommentResponse createComment(CreateCommentRequest request) {
        String authorEmail = getCurrentUserEmail();

        InvoiceComment comment = InvoiceComment.builder()
                .businessId(request.businessId())
                .entityType(request.entityType())
                .entityId(request.entityId())
                .authorId(authorEmail)
                .authorName(authorEmail)    // In Sprint 2, resolve real name from auth-ms
                .body(request.body())
                .parentId(request.parentId())
                .mentions(mapper.serializeMentions(request.mentions()))
                .build();

        InvoiceComment saved = commentRepository.save(comment);
        log.info("Comment created on {} {} by {}", request.entityType(), request.entityId(), authorEmail);
        return mapper.toCommentResponse(saved);
    }

    @Transactional
    public CommentResponse updateComment(String id, UpdateCommentRequest request) {
        InvoiceComment comment = commentRepository.findByIdAndBusinessId(id, request.businessId())
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + id));

        // Authorization: only the author can edit
        String currentUser = getCurrentUserEmail();
        if (!comment.getAuthorId().equals(currentUser)) {
            throw new IllegalArgumentException("You can only edit your own comments");
        }

        comment.setBody(request.body());
        comment.setEdited(true);
        comment.setUpdatedAt(Instant.now());

        return mapper.toCommentResponse(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(String id, String businessId) {
        InvoiceComment comment = commentRepository.findByIdAndBusinessId(id, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + id));

        String currentUser = getCurrentUserEmail();
        if (!comment.getAuthorId().equals(currentUser)) {
            throw new IllegalArgumentException("You can only delete your own comments");
        }

        comment.setDeleted(true);
        comment.setUpdatedAt(Instant.now());
        commentRepository.save(comment);
    }

    private String getCurrentUserEmail() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "anonymous";
        }
    }
}
