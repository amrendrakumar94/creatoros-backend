-- CreatorOS initial schema for a fresh database.
-- Every domain table is tenant-scoped by creator_id so one creator can never read another's records.

CREATE TABLE creator (
    id                       BIGINT         NOT NULL AUTO_INCREMENT,
    email                    VARCHAR(255)   NOT NULL,
    password_hash            VARCHAR(255)   NOT NULL,
    status                   VARCHAR(20)    NOT NULL,
    role                     VARCHAR(20)    NOT NULL,

    name                     VARCHAR(150)   NOT NULL,
    handle                   VARCHAR(100)   NOT NULL,
    avatar                   VARCHAR(500)   NULL,
    phone                    VARCHAR(30)    NULL,
    creator_type             VARCHAR(40)    NULL,
    is_gst_registered        BIT(1)         NOT NULL,
    gstin                    VARCHAR(15)    NULL,
    pan                      VARCHAR(10)    NULL,
    trade_name               VARCHAR(200)   NULL,
    address                  VARCHAR(500)   NULL,
    city                     VARCHAR(120)   NULL,
    state                    VARCHAR(60)    NULL,
    state_code               VARCHAR(2)     NULL,
    pincode                  VARCHAR(10)    NULL,
    monthly_revenue_estimate DECIMAL(15, 2) NOT NULL,
    team_size                VARCHAR(50)    NULL,

    bank_name                VARCHAR(150)   NULL,
    account_number           VARCHAR(50)    NULL,
    ifsc_code                VARCHAR(20)    NULL,
    upi_id                   VARCHAR(100)   NULL,
    swift_code               VARCHAR(20)    NULL,

    onboarding_completed     BIT(1)         NOT NULL,
    created_at               TIMESTAMP(6)   NOT NULL,
    updated_at               TIMESTAMP(6)   NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_creator_email UNIQUE (email),
    CONSTRAINT uk_creator_handle UNIQUE (handle)
) ENGINE = InnoDB;

CREATE TABLE creator_platform (
    creator_id BIGINT      NOT NULL,
    platform   VARCHAR(30) NOT NULL,
    PRIMARY KEY (creator_id, platform),
    CONSTRAINT fk_creator_platform_creator FOREIGN KEY (creator_id) REFERENCES creator (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE TABLE document_counter (
    creator_id     BIGINT      NOT NULL,
    doc_type       VARCHAR(20) NOT NULL,
    financial_year VARCHAR(9)  NOT NULL,
    last_sequence  INT         NOT NULL DEFAULT 0,
    PRIMARY KEY (creator_id, doc_type, financial_year),
    CONSTRAINT fk_document_counter_creator FOREIGN KEY (creator_id) REFERENCES creator (id) ON DELETE CASCADE
) ENGINE = InnoDB;

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

    exclusivity_days        INT            NOT NULL DEFAULT 0,
    paid_ads_allowed        BIT(1)         NOT NULL DEFAULT b'0',
    whitelisting_allowed    BIT(1)         NOT NULL DEFAULT b'0',
    territory               VARCHAR(150)   NULL,

    negotiation_notes       TEXT           NULL,
    payment_terms           VARCHAR(40)    NOT NULL,

    created_at              TIMESTAMP(6)   NOT NULL,
    updated_at              TIMESTAMP(6)   NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_brand_deal_number UNIQUE (creator_id, deal_number),
    CONSTRAINT fk_brand_deal_creator FOREIGN KEY (creator_id) REFERENCES creator (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_brand_deal_creator ON brand_deal (creator_id);

CREATE TABLE deal_tag (
    deal_id BIGINT      NOT NULL,
    tag     VARCHAR(60) NOT NULL,
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
    created_at           TIMESTAMP(6)   NOT NULL,
    updated_at           TIMESTAMP(6)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_expense_creator FOREIGN KEY (creator_id) REFERENCES creator (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_expense_creator ON expense (creator_id);

CREATE TABLE invoice (
    id                      BIGINT         NOT NULL AUTO_INCREMENT,
    creator_id              BIGINT         NOT NULL,
    deal_id                 BIGINT         NULL,

    invoice_number          VARCHAR(30)    NOT NULL,
    financial_year          VARCHAR(9)     NOT NULL,
    sequence_in_year        INT            NOT NULL,

    status                  VARCHAR(20)    NOT NULL,
    issue_date              DATE           NOT NULL,
    due_date                DATE           NOT NULL,
    payment_terms           VARCHAR(40)    NOT NULL,

    supplier_legal_name     VARCHAR(200)   NOT NULL,
    supplier_gstin          VARCHAR(15)    NULL,
    supplier_pan            VARCHAR(10)    NULL,
    supplier_address        VARCHAR(500)   NULL,
    supplier_city           VARCHAR(120)   NULL,
    supplier_state          VARCHAR(60)    NULL,
    supplier_state_code     VARCHAR(2)     NULL,
    supplier_pincode        VARCHAR(10)    NULL,

    buyer_name              VARCHAR(200)   NOT NULL,
    buyer_legal_name        VARCHAR(200)   NULL,
    buyer_gstin             VARCHAR(15)    NULL,
    buyer_email             VARCHAR(255)   NULL,
    buyer_address           VARCHAR(500)   NULL,
    buyer_city              VARCHAR(120)   NULL,
    buyer_state             VARCHAR(60)    NULL,
    buyer_state_code        VARCHAR(2)     NULL,
    buyer_pincode           VARCHAR(10)    NULL,

    place_of_supply_state   VARCHAR(60)    NULL,
    place_of_supply_code    VARCHAR(2)     NULL,
    inter_state             BIT(1)         NOT NULL DEFAULT b'0',
    reverse_charge          BIT(1)         NOT NULL DEFAULT b'0',

    subtotal                DECIMAL(15, 2) NOT NULL DEFAULT 0,
    discount_amount         DECIMAL(15, 2) NOT NULL DEFAULT 0,
    cgst_rate               DECIMAL(5, 2)  NOT NULL DEFAULT 0,
    cgst_amount             DECIMAL(15, 2) NOT NULL DEFAULT 0,
    sgst_rate               DECIMAL(5, 2)  NOT NULL DEFAULT 0,
    sgst_amount             DECIMAL(15, 2) NOT NULL DEFAULT 0,
    igst_rate               DECIMAL(5, 2)  NOT NULL DEFAULT 0,
    igst_amount             DECIMAL(15, 2) NOT NULL DEFAULT 0,
    total_tax               DECIMAL(15, 2) NOT NULL DEFAULT 0,
    invoice_total           DECIMAL(15, 2) NOT NULL DEFAULT 0,

    tds_section             VARCHAR(20)    NOT NULL,
    tds_rate                DECIMAL(5, 2)  NOT NULL DEFAULT 0,
    tds_amount              DECIMAL(15, 2) NOT NULL DEFAULT 0,
    net_receivable          DECIMAL(15, 2) NOT NULL DEFAULT 0,
    amount_paid             DECIMAL(15, 2) NOT NULL DEFAULT 0,
    balance_due             DECIMAL(15, 2) NOT NULL DEFAULT 0,

    notes                   TEXT           NULL,
    terms                   TEXT           NULL,

    created_at              TIMESTAMP(6)   NOT NULL,
    updated_at              TIMESTAMP(6)   NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_invoice_number UNIQUE (creator_id, invoice_number),
    CONSTRAINT uk_invoice_sequence UNIQUE (creator_id, financial_year, sequence_in_year),
    CONSTRAINT fk_invoice_creator FOREIGN KEY (creator_id) REFERENCES creator (id) ON DELETE CASCADE,
    CONSTRAINT fk_invoice_deal FOREIGN KEY (deal_id) REFERENCES brand_deal (id) ON DELETE SET NULL
) ENGINE = InnoDB;

CREATE INDEX idx_invoice_creator ON invoice (creator_id);
CREATE INDEX idx_invoice_creator_status ON invoice (creator_id, status);
CREATE INDEX idx_invoice_creator_due ON invoice (creator_id, due_date);

CREATE TABLE invoice_line_item (
    id             BIGINT         NOT NULL AUTO_INCREMENT,
    invoice_id     BIGINT         NOT NULL,
    description    VARCHAR(500)   NOT NULL,
    sac_code       VARCHAR(10)    NULL,
    quantity       DECIMAL(10, 2) NOT NULL DEFAULT 1,
    unit           VARCHAR(20)    NULL,
    rate           DECIMAL(15, 2) NOT NULL,
    gst_rate       DECIMAL(5, 2)  NOT NULL DEFAULT 18.00,
    taxable_amount DECIMAL(15, 2) NOT NULL,
    sort_order     INT            NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_invoice_line_invoice FOREIGN KEY (invoice_id) REFERENCES invoice (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_invoice_line_invoice ON invoice_line_item (invoice_id);

CREATE TABLE invoice_payment (
    id           BIGINT         NOT NULL AUTO_INCREMENT,
    creator_id   BIGINT         NOT NULL,
    invoice_id   BIGINT         NOT NULL,
    amount       DECIMAL(15, 2) NOT NULL,
    received_on  DATE           NOT NULL,
    method       VARCHAR(30)    NOT NULL,
    reference    VARCHAR(120)   NULL,
    tds_withheld DECIMAL(15, 2) NOT NULL DEFAULT 0,
    notes        TEXT           NULL,
    created_at   TIMESTAMP(6)   NOT NULL,
    updated_at   TIMESTAMP(6)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_invoice_payment_creator FOREIGN KEY (creator_id) REFERENCES creator (id) ON DELETE CASCADE,
    CONSTRAINT fk_invoice_payment_invoice FOREIGN KEY (invoice_id) REFERENCES invoice (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_invoice_payment_creator ON invoice_payment (creator_id);
CREATE INDEX idx_invoice_payment_invoice ON invoice_payment (invoice_id);
