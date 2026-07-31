-- CreatorOS Phase 2-5: the creator's operating data.
-- Every table is tenant-scoped by creator_id so one creator can never read another's records.

CREATE TABLE brand_deal (
    id                      BIGINT         NOT NULL AUTO_INCREMENT,
    creator_id              BIGINT         NOT NULL,
    deal_number             VARCHAR(30)    NOT NULL,

    brand_name              VARCHAR(200)   NOT NULL,
    brand_logo              VARCHAR(20)    NULL,
    category                VARCHAR(120)   NULL,
    contact_person          VARCHAR(150)   NULL,
    contact_email           VARCHAR(255)   NULL,
    contact_phone           VARCHAR(30)    NULL,

    amount                  DECIMAL(15, 2) NOT NULL,
    stage                   VARCHAR(30)    NOT NULL,
    platform                VARCHAR(30)    NOT NULL,
    campaign_title          VARCHAR(300)   NULL,
    start_date              DATE           NULL,
    end_date                DATE           NULL,

    -- Usage rights (embedded)
    exclusivity_days        INT            NOT NULL DEFAULT 0,
    paid_ads_allowed        BIT(1)         NOT NULL DEFAULT b'0',
    whitelisting_allowed    BIT(1)         NOT NULL DEFAULT b'0',
    territory               VARCHAR(150)   NULL,

    negotiation_notes       TEXT           NULL,
    payment_terms           VARCHAR(40)    NOT NULL,
    invoice_id              BIGINT         NULL,

    created_at              DATETIME(6)    NOT NULL,
    updated_at              DATETIME(6)    NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_brand_deal_number UNIQUE (creator_id, deal_number),
    CONSTRAINT fk_brand_deal_creator FOREIGN KEY (creator_id) REFERENCES creator (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_brand_deal_creator ON brand_deal (creator_id);

CREATE TABLE deal_tag (
    deal_id BIGINT       NOT NULL,
    tag     VARCHAR(60)  NOT NULL,
    PRIMARY KEY (deal_id, tag),
    CONSTRAINT fk_deal_tag_deal FOREIGN KEY (deal_id) REFERENCES brand_deal (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE TABLE deliverable_item (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    deal_id    BIGINT       NOT NULL,
    type       VARCHAR(40)  NOT NULL,
    title      VARCHAR(300) NULL,
    due_date   DATE         NULL,
    status     VARCHAR(40)  NOT NULL,
    link       VARCHAR(500) NULL,
    sort_order INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_deliverable_deal FOREIGN KEY (deal_id) REFERENCES brand_deal (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_deliverable_deal ON deliverable_item (deal_id);

CREATE TABLE invoice (
    id                       BIGINT         NOT NULL AUTO_INCREMENT,
    creator_id               BIGINT         NOT NULL,
    invoice_number           VARCHAR(30)    NOT NULL,

    brand_name               VARCHAR(200)   NOT NULL,
    brand_gstin              VARCHAR(15)    NULL,
    brand_address            VARCHAR(500)   NULL,

    -- Creator identity and payout details are snapshotted at issue time: a tax document must
    -- keep showing the details that were valid when it was raised.
    creator_name             VARCHAR(200)   NULL,
    creator_gstin            VARCHAR(15)    NULL,
    creator_pan              VARCHAR(10)    NULL,
    bank_name                VARCHAR(150)   NULL,
    account_number           VARCHAR(50)    NULL,
    ifsc_code                VARCHAR(20)    NULL,
    upi_id                   VARCHAR(100)   NULL,

    issue_date               DATE           NOT NULL,
    due_date                 DATE           NOT NULL,

    subtotal                 DECIMAL(15, 2) NOT NULL,
    is_interstate            BIT(1)         NOT NULL,
    cgst_amount              DECIMAL(15, 2) NOT NULL,
    sgst_amount              DECIMAL(15, 2) NOT NULL,
    igst_amount              DECIMAL(15, 2) NOT NULL,
    total_gst                DECIMAL(15, 2) NOT NULL,
    tds_deducted             DECIMAL(15, 2) NOT NULL,
    total_amount             DECIMAL(15, 2) NOT NULL,
    net_receivable           DECIMAL(15, 2) NOT NULL,

    status                   VARCHAR(20)    NOT NULL,
    deal_id                  BIGINT         NULL,
    paid_date                DATE           NULL,
    reminder_sent_count      INT            NOT NULL DEFAULT 0,
    last_reminder_date       DATE           NULL,
    expected_settlement_date DATE           NULL,

    created_at               DATETIME(6)    NOT NULL,
    updated_at               DATETIME(6)    NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_invoice_number UNIQUE (creator_id, invoice_number),
    CONSTRAINT fk_invoice_creator FOREIGN KEY (creator_id) REFERENCES creator (id) ON DELETE CASCADE,
    CONSTRAINT fk_invoice_deal FOREIGN KEY (deal_id) REFERENCES brand_deal (id) ON DELETE SET NULL
) ENGINE = InnoDB;

CREATE INDEX idx_invoice_creator ON invoice (creator_id);

ALTER TABLE brand_deal
    ADD CONSTRAINT fk_brand_deal_invoice FOREIGN KEY (invoice_id) REFERENCES invoice (id) ON DELETE SET NULL;

CREATE TABLE invoice_item (
    id          BIGINT         NOT NULL AUTO_INCREMENT,
    invoice_id  BIGINT         NOT NULL,
    description VARCHAR(500)   NULL,
    sac_code    VARCHAR(20)    NULL,
    quantity    INT            NOT NULL DEFAULT 1,
    unit_price  DECIMAL(15, 2) NOT NULL,
    amount      DECIMAL(15, 2) NOT NULL,
    sort_order  INT            NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_invoice_item_invoice FOREIGN KEY (invoice_id) REFERENCES invoice (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_invoice_item_invoice ON invoice_item (invoice_id);

CREATE TABLE expense (
    id                   BIGINT         NOT NULL AUTO_INCREMENT,
    creator_id           BIGINT         NOT NULL,
    title                VARCHAR(300)   NOT NULL,
    category             VARCHAR(50)    NOT NULL,
    amount               DECIMAL(15, 2) NOT NULL,
    expense_date         DATE           NOT NULL,
    vendor               VARCHAR(200)   NULL,
    gstin                VARCHAR(15)    NULL,
    has_gst_invoice      BIT(1)         NOT NULL DEFAULT b'0',
    gst_claimable_amount DECIMAL(15, 2) NOT NULL DEFAULT 0,
    receipt_url          VARCHAR(500)   NULL,
    payment_method       VARCHAR(50)    NOT NULL,
    notes                TEXT           NULL,
    tax_deductible       BIT(1)         NOT NULL DEFAULT b'1',
    created_at           DATETIME(6)    NOT NULL,
    updated_at           DATETIME(6)    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_expense_creator FOREIGN KEY (creator_id) REFERENCES creator (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_expense_creator ON expense (creator_id);

CREATE TABLE notification (
    id         BIGINT         NOT NULL AUTO_INCREMENT,
    creator_id BIGINT         NOT NULL,
    title      VARCHAR(200)   NOT NULL,
    message    VARCHAR(500)   NULL,
    type       VARCHAR(20)    NOT NULL,
    read_flag  BIT(1)         NOT NULL DEFAULT b'0',
    action_url VARCHAR(30)    NULL,
    amount     DECIMAL(15, 2) NULL,
    created_at DATETIME(6)    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_notification_creator FOREIGN KEY (creator_id) REFERENCES creator (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_notification_creator ON notification (creator_id, created_at);
