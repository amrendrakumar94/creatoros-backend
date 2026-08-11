ALTER TABLE invoice
    ADD COLUMN scheduled_send_at    TIMESTAMP(6) NULL,
    ADD COLUMN scheduled_send_email VARCHAR(255) NULL,
    ADD COLUMN last_emailed_at      TIMESTAMP(6) NULL;

CREATE INDEX idx_invoice_scheduled_send_at ON invoice (scheduled_send_at);
