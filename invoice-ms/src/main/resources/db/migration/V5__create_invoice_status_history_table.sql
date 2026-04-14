-- V5: Status audit trail — every status change is recorded here
-- Provides full lifecycle visibility for compliance and debugging
CREATE TABLE invoice_status_history (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    invoice_id      VARCHAR(36)     NOT NULL,
    old_status      VARCHAR(20)     NULL,       -- NULL on first status set
    new_status      VARCHAR(20)     NOT NULL,
    changed_by      VARCHAR(150)    NOT NULL,   -- email from JWT
    reason          TEXT            NULL,       -- optional reason (e.g. dispute reason)
    changed_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_history_invoice
        FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE,

    INDEX idx_history_invoice   (invoice_id),
    INDEX idx_history_date      (changed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
