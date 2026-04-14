package com.accountia.invoice.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;

/**
 * A comment on a business entity (invoice, expense, purchase order).
 *
 * <p>Comments are threaded: a comment with a non-null {@code parentId}
 * is a reply to the parent comment. The frontend renders them nested.
 *
 * <p>{@code mentions} stores a JSON array of mentioned user IDs/emails
 * (e.g. {@code ["user1@co.com","user2@co.com"]}).
 * Stored as plain TEXT for simplicity; parsed by the service layer.
 *
 * <p>Soft-delete: setting {@code isDeleted = true} hides the comment
 * from list responses without breaking thread references.
 */
@Entity
@Table(name = "invoice_comments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceComment {

    @Id
    @UuidGenerator
    @Column(name = "id", length = 36, updatable = false, nullable = false)
    private String id;

    /** The business this comment belongs to (for authorization checks). */
    @Column(name = "business_id", nullable = false, length = 36)
    private String businessId;

    /** Discriminator: "invoice", "expense", or "purchase_order". */
    @Column(name = "entity_type", nullable = false, length = 30)
    @Builder.Default
    private String entityType = "invoice";

    /** UUID of the invoice (or other entity) being commented on. */
    @Column(name = "entity_id", nullable = false, length = 36)
    private String entityId;

    /** Email of the comment author (extracted from JWT). */
    @Column(name = "author_id", nullable = false, length = 150)
    private String authorId;

    /** Display name of the author (extracted from JWT). */
    @Column(name = "author_name", nullable = false, length = 100)
    private String authorName;

    /** The comment text. */
    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    /** For threaded replies: UUID of the parent comment. Null for top-level. */
    @Column(name = "parent_id", length = 36)
    private String parentId;

    /** JSON array of mentioned user IDs: ["id1","id2"]. Stored as raw text. */
    @Column(name = "mentions", columnDefinition = "TEXT")
    private String mentions;

    @Column(name = "is_edited", nullable = false)
    @Builder.Default
    private boolean isEdited = false;

    /** Soft-delete flag — deleted comments are excluded from list responses. */
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();
}
