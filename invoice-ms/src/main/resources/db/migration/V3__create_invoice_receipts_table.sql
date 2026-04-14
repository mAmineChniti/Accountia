-- V3: Invoice receipts (copies sent to platform recipients when an invoice is issued)
-- When business A issues an invoice to platform business B, a receipt is created
-- for B so they can see it in their "received invoices" inbox.
CREATE TABLE invoice_receipts (
    id                          VARCHAR(36)     NOT NULL PRIMARY KEY,  -- UUID
    invoice_id                  VARCHAR(36)     NOT NULL,              -- reference to original invoice
    issuer_tenant_db_name       VARCHAR(100)    NOT NULL,
    issuer_business_id          VARCHAR(36)     NOT NULL,
    issuer_business_name        VARCHAR(150)    NOT NULL,
    invoice_number              VARCHAR(30)     NOT NULL,
    total_amount                DECIMAL(15,3)   NOT NULL,
    currency                    VARCHAR(5)      NOT NULL,
    issued_date                 DATE            NOT NULL,
    due_date                    DATE            NOT NULL,
    invoice_status              VARCHAR(20)     NOT NULL,

    -- Tracking whether the recipient has viewed this invoice
    recipient_viewed            BOOLEAN         NOT NULL DEFAULT FALSE,
    recipient_viewed_at         DATETIME        NULL,

    -- Identifies WHO this receipt belongs to (so we can filter by businessId or userId)
    recipient_business_id       VARCHAR(36)     NULL,   -- set when recipient is PLATFORM_BUSINESS
    recipient_user_id           VARCHAR(36)     NULL,   -- set when recipient is PLATFORM_INDIVIDUAL

    last_synced_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at                  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_receipt_invoice
        FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE,

    INDEX idx_receipt_invoice         (invoice_id),
    INDEX idx_receipt_business        (recipient_business_id),
    INDEX idx_receipt_user            (recipient_user_id),
    INDEX idx_receipt_issuer_business (issuer_business_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
