package com.accountia.invoice.controller;

import com.accountia.invoice.dto.request.CreateCommentRequest;
import com.accountia.invoice.dto.request.UpdateCommentRequest;
import com.accountia.invoice.dto.response.CommentListResponse;
import com.accountia.invoice.dto.response.CommentResponse;
import com.accountia.invoice.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for threaded comments on invoices (and other entity types).
 *
 * <p>Comments support:
 * <ul>
 *   <li><strong>Threading</strong> — replies via {@code parentId} in the create request.</li>
 *   <li><strong>Mentions</strong> — {@code @user} mentions stored as a JSON array.</li>
 *   <li><strong>Soft delete</strong> — deleted comments keep their position in the thread
 *       (only the body is hidden); the {@code isDeleted} flag is set to true.</li>
 *   <li><strong>Authorization</strong> — only the comment author can edit or delete their
 *       own comments. The author is determined from the JWT principal.</li>
 * </ul>
 *
 * <p>Comments are generic (entityType + entityId) so they can be attached to any
 * entity type (invoice, recurring schedule, etc.) without schema changes.
 */
@RestController
@RequestMapping("/invoices/comments")
@Tag(name = "Comments", description = "Threaded comments on invoices and other entities")
@SecurityRequirement(name = "bearerAuth")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // ── LIST ──────────────────────────────────────────────────────────────────

    /**
     * Returns all active (non-deleted) comments for a given entity.
     *
     * @param businessId the business context (used for data isolation)
     * @param entityType the type of entity, e.g. "invoice" or "recurring"
     * @param entityId   the UUID of the entity
     */
    @Operation(
            summary = "List comments for an entity",
            description = "Returns all non-deleted comments for the given entity (e.g., invoice). " +
                          "Comments are returned in database order (creation ASC). " +
                          "Threaded replies are identified by their parentId field."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment list"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    })
    @GetMapping
    public ResponseEntity<CommentListResponse> getComments(
            @Parameter(description = "Business UUID", required = true) @RequestParam String businessId,
            @Parameter(description = "Entity type, e.g. 'invoice'", required = true) @RequestParam String entityType,
            @Parameter(description = "Entity UUID", required = true) @RequestParam String entityId) {
        return ResponseEntity.ok(commentService.getComments(businessId, entityType, entityId));
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    /**
     * Creates a new comment (or a reply to an existing comment via parentId).
     *
     * <p>The author is determined from the JWT token (Spring Security principal).
     * The {@code mentions} field accepts a list of user identifiers (@mentions).
     */
    @Operation(
            summary = "Post a comment",
            description = "Creates a new comment on an entity. Set parentId to reply to another comment. " +
                          "The comment author is taken from the current user's JWT token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Comment created",
                    content = @Content(schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    })
    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @Valid @RequestBody CreateCommentRequest request) {
        CommentResponse response = commentService.createComment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    /**
     * Edits the body of an existing comment.
     *
     * <p>Only the original author can edit their comment. The {@code isEdited} flag
     * is set to true to indicate the comment was modified after posting.
     */
    @Operation(
            summary = "Edit a comment",
            description = "Updates the text of an existing comment. " +
                          "Only the comment author can edit. Sets the isEdited flag to true."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment updated"),
            @ApiResponse(responseCode = "400", description = "Not the author of this comment"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<CommentResponse> updateComment(
            @Parameter(description = "Comment UUID") @PathVariable String id,
            @Valid @RequestBody UpdateCommentRequest request) {
        return ResponseEntity.ok(commentService.updateComment(id, request));
    }

    // ── DELETE (SOFT) ─────────────────────────────────────────────────────────

    /**
     * Soft-deletes a comment by setting {@code isDeleted=true}.
     *
     * <p>The comment remains in the database to preserve thread structure.
     * Only the original author can delete their comment.
     */
    @Operation(
            summary = "Delete a comment (soft delete)",
            description = "Marks the comment as deleted. Only the comment author can delete. " +
                          "The comment record is kept to preserve thread structure."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Comment deleted"),
            @ApiResponse(responseCode = "400", description = "Not the author of this comment"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "Comment UUID") @PathVariable String id,
            @Parameter(description = "Business UUID", required = true) @RequestParam String businessId) {
        commentService.deleteComment(id, businessId);
        return ResponseEntity.noContent().build();
    }
}
