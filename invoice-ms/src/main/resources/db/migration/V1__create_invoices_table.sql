-- V1: Main invoices table
-- Uses UUID primary key (matches frontend's string id expectation)
-- Recipient is stored denormalized (embedded) to avoid cross-service join
CREATE TABLE invoices (
    id                  VARCHAR(36)     NOT NULL PRIMARY KEY,  -- UUID
    issuer_business_id  VARCHAR(36)     NOT NULL,
    invoice_number      VARCHAR(30)     NOT NULL UNIQUE,       -- INV-2026-00001

    -- Status lifecycle: DRAFT -> ISSUED -> VIEWED -> PAID | PARTIAL | OVERDUE | DISPUTED -> VOIDED | ARCHIVED
    status              VARCHAR(20)     NOT NULL DEFAULT 'DRAFT',

    -- Financial totals (computed from line items in service layer)
    total_amount        DECIMAL(15,3)   NOT NULL DEFAULT 0,
    amount_paid         DECIMAL(15,3)   NOT NULL DEFAULT 0,
    currency            VARCHAR(5)      NOT NULL DEFAULT 'TND',

    -- Dates
    issued_date         DATE            NOT NULL,
    due_date            DATE            NOT NULL,
    last_status_change_at DATETIME      NULL,

    -- Optional fields
    description         TEXT            NULL,
    payment_terms       VARCHAR(100)    NULL,

    -- Recipient (denormalized: other microservices own their entities)
    recipient_type           VARCHAR(30)  NOT NULL,   -- EXTERNAL | PLATFORM_BUSINESS | PLATFORM_INDIVIDUAL
    recipient_platform_id    VARCHAR(36)  NULL,
    recipient_tenant_db_name VARCHAR(100) NULL,
    recipient_email          VARCHAR(150) NULL,
    recipient_display_name   VARCHAR(150) NULL,
    recipient_resolution_status VARCHAR(20) NULL,     -- RESOLVED | PENDING | CLAIMED | NEVER_RESOLVED
    recipient_last_resolution_attempt DATETIME NULL,

    -- Soft delete
    is_deleted          BOOLEAN         NOT NULL DEFAULT FALSE,
    deleted_at          DATETIME        NULL,

    -- JPA Auditing
    created_by          VARCHAR(150)    NULL,
    last_modified_by    VARCHAR(150)    NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Performance indexes
    INDEX idx_issuer_business  (issuer_business_id),
    INDEX idx_status            (issuer_business_id, status),
    INDEX idx_issued_date       (issuer_business_id, issued_date),
    INDEX idx_due_date          (issuer_business_id, due_date),
    INDEX idx_not_deleted       (issuer_business_id, is_deleted),
    INDEX idx_recipient_email   (recipient_email),
    INDEX idx_recipient_pid     (recipient_platform_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
