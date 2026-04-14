-- V4: Comments on invoices (threaded, with @mentions support)
-- entityType can be 'invoice', 'expense', 'purchase_order' (shared service pattern)
CREATE TABLE invoice_comments (
    id              VARCHAR(36)     NOT NULL PRIMARY KEY,
    business_id     VARCHAR(36)     NOT NULL,
    entity_type     VARCHAR(30)     NOT NULL DEFAULT 'invoice',
    entity_id       VARCHAR(36)     NOT NULL,   -- invoice UUID
    author_id       VARCHAR(150)    NOT NULL,   -- user email or userId from JWT
    author_name     VARCHAR(100)    NOT NULL,
    body            TEXT            NOT NULL,
    parent_id       VARCHAR(36)     NULL,       -- for threaded replies
    mentions        TEXT            NULL,       -- JSON array of mentioned user IDs
    is_edited       BOOLEAN         NOT NULL DEFAULT FALSE,
    is_deleted      BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_comment_parent
        FOREIGN KEY (parent_id) REFERENCES invoice_comments(id),

    INDEX idx_comment_entity    (entity_type, entity_id),
    INDEX idx_comment_business  (business_id),
    INDEX idx_comment_parent    (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
