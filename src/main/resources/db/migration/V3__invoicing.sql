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

    created_at              DATETIME(6)    NOT NULL,
    updated_at              DATETIME(6)    NOT NULL,

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
    created_at   DATETIME(6)    NOT NULL,
    updated_at   DATETIME(6)    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_invoice_payment_creator FOREIGN KEY (creator_id) REFERENCES creator (id) ON DELETE CASCADE,
    CONSTRAINT fk_invoice_payment_invoice FOREIGN KEY (invoice_id) REFERENCES invoice (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_invoice_payment_creator ON invoice_payment (creator_id);
CREATE INDEX idx_invoice_payment_invoice ON invoice_payment (invoice_id);
