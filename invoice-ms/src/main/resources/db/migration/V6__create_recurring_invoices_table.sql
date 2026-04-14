-- V6: Recurring invoice schedules
-- A schedule defines how often an invoice should be auto-generated
CREATE TABLE recurring_invoices (
    id                  VARCHAR(36)     NOT NULL PRIMARY KEY,
    business_id         VARCHAR(36)     NOT NULL,
    name                VARCHAR(200)    NOT NULL,
    frequency           VARCHAR(20)     NOT NULL,   -- daily | weekly | monthly | quarterly | yearly
    status              VARCHAR(20)     NOT NULL DEFAULT 'active',  -- active | paused | cancelled | completed

    -- Schedule bounds
    start_date          DATE            NOT NULL,
    end_condition       VARCHAR(30)     NOT NULL DEFAULT 'never',  -- never | after_occurrences | by_date
    max_occurrences     INT             NULL,
    occurrence_count    INT             NOT NULL DEFAULT 0,
    end_date            DATE            NULL,
    next_run_at         DATETIME        NULL,
    last_run_at         DATETIME        NULL,

    -- Invoice template fields
    total_amount        DECIMAL(15,3)   NOT NULL DEFAULT 0,
    currency            VARCHAR(5)      NOT NULL DEFAULT 'TND',
    due_days_from_issue INT             NOT NULL DEFAULT 30,
    description         TEXT            NULL,
    payment_terms       VARCHAR(100)    NULL,
    auto_issue          BOOLEAN         NOT NULL DEFAULT TRUE,

    -- Recipient (same structure as invoices)
    recipient_type          VARCHAR(30)  NOT NULL,
    recipient_platform_id   VARCHAR(36)  NULL,
    recipient_email         VARCHAR(150) NULL,
    recipient_display_name  VARCHAR(150) NULL,

    -- JSON blobs for line items and generated invoice IDs (simpler than join tables)
    line_items_json         TEXT         NULL,
    generated_invoice_ids   TEXT         NULL,   -- JSON array of UUID strings

    created_by          VARCHAR(150)    NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_recurring_business    (business_id),
    INDEX idx_recurring_status      (business_id, status),
    INDEX idx_recurring_next_run    (next_run_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
