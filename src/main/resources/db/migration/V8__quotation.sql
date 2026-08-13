CREATE TABLE quotation (
    id                      BIGINT         NOT NULL AUTO_INCREMENT,
    creator_id              BIGINT         NOT NULL,
    deal_id                 BIGINT         NULL,

    quotation_number        VARCHAR(30)    NOT NULL,
    financial_year          VARCHAR(9)     NOT NULL,
    sequence_in_year        INT            NOT NULL,

    status                  VARCHAR(20)    NOT NULL,
    issue_date              DATE           NOT NULL,
    valid_until             DATE           NULL,

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
    quotation_total         DECIMAL(15, 2) NOT NULL DEFAULT 0,

    tds_section             VARCHAR(20)    NOT NULL,
    tds_rate                DECIMAL(5, 2)  NOT NULL DEFAULT 0,
    tds_amount              DECIMAL(15, 2) NOT NULL DEFAULT 0,

    notes                   TEXT           NULL,
    terms                   TEXT           NULL,

    scheduled_send_at       TIMESTAMP(6)   NULL,
    scheduled_send_email    VARCHAR(255)   NULL,
    last_emailed_at         TIMESTAMP(6)   NULL,

    converted_invoice_id    BIGINT         NULL,

    created_at              DATETIME(6)    NOT NULL,
    updated_at              DATETIME(6)    NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_quotation_number UNIQUE (creator_id, quotation_number),
    CONSTRAINT uk_quotation_sequence UNIQUE (creator_id, financial_year, sequence_in_year),
    CONSTRAINT fk_quotation_creator FOREIGN KEY (creator_id) REFERENCES creator (id) ON DELETE CASCADE,
    CONSTRAINT fk_quotation_deal FOREIGN KEY (deal_id) REFERENCES brand_deal (id) ON DELETE SET NULL,
    CONSTRAINT fk_quotation_converted_invoice FOREIGN KEY (converted_invoice_id) REFERENCES invoice (id) ON DELETE SET NULL
) ENGINE = InnoDB;

CREATE INDEX idx_quotation_creator ON quotation (creator_id);
CREATE INDEX idx_quotation_creator_status ON quotation (creator_id, status);
CREATE INDEX idx_quotation_scheduled_send_at ON quotation (scheduled_send_at);

CREATE TABLE quotation_line_item (
    id             BIGINT         NOT NULL AUTO_INCREMENT,
    quotation_id   BIGINT         NOT NULL,
    description    VARCHAR(500)   NOT NULL,
    sac_code       VARCHAR(10)    NULL,
    quantity       DECIMAL(10, 2) NOT NULL DEFAULT 1,
    unit           VARCHAR(20)    NULL,
    rate           DECIMAL(15, 2) NOT NULL,
    gst_rate       DECIMAL(5, 2)  NOT NULL DEFAULT 18.00,
    taxable_amount DECIMAL(15, 2) NOT NULL,
    sort_order     INT            NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_quotation_line_quotation FOREIGN KEY (quotation_id) REFERENCES quotation (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_quotation_line_quotation ON quotation_line_item (quotation_id);
