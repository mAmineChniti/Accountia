-- V2: Invoice line items (one invoice has many line items)
-- amount = quantity * unitPrice (computed and stored for query efficiency)
CREATE TABLE invoice_line_items (
    id              VARCHAR(36)     NOT NULL PRIMARY KEY,   -- UUID
    invoice_id      VARCHAR(36)     NOT NULL,
    product_id      VARCHAR(36)     NOT NULL,
    product_name    VARCHAR(200)    NOT NULL,
    quantity        DECIMAL(10,2)   NOT NULL DEFAULT 1,
    unit_price      DECIMAL(15,3)   NOT NULL,
    amount          DECIMAL(15,3)   NOT NULL,              -- quantity * unitPrice
    description     TEXT            NULL,
    sort_order      INT             NOT NULL DEFAULT 0,    -- display order in UI

    CONSTRAINT fk_line_item_invoice
        FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE,

    INDEX idx_line_item_invoice (invoice_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
