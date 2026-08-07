-- CreatorOS initial schema: authentication, creator profile, brand deals and expenses.
-- Every domain table is tenant-scoped by creator_id so one creator can never read another's records.

CREATE TABLE creator (
    id                       BIGINT         NOT NULL AUTO_INCREMENT,
    email                    VARCHAR(255)   NOT NULL,
    password_hash            VARCHAR(255)   NOT NULL,
    status                   VARCHAR(20)    NOT NULL,
    role                     VARCHAR(20)    NOT NULL,

    -- Profile (mirrors CreatorProfile in the frontend's src/types/creatorOS.ts)
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
    pincode                  VARCHAR(10)    NULL,
    monthly_revenue_estimate DECIMAL(15, 2) NOT NULL,
    team_size                VARCHAR(50)    NULL,

    -- Bank details (embedded)
    bank_name                VARCHAR(150)   NULL,
    account_number           VARCHAR(50)    NULL,
    ifsc_code                VARCHAR(20)    NULL,
    upi_id                   VARCHAR(100)   NULL,
    swift_code               VARCHAR(20)    NULL,

    onboarding_completed     BIT(1)         NOT NULL,
    created_at               DATETIME(6)    NOT NULL,
    updated_at               DATETIME(6)    NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_creator_email UNIQUE (email),
    CONSTRAINT uk_creator_handle UNIQUE (handle)
) ENGINE = InnoDB;

-- @ElementCollection for CreatorProfile.platforms: PlatformType[]
CREATE TABLE creator_platform (
    creator_id BIGINT      NOT NULL,
    platform   VARCHAR(30) NOT NULL,
    PRIMARY KEY (creator_id, platform),
    CONSTRAINT fk_creator_platform_creator FOREIGN KEY (creator_id) REFERENCES creator (id) ON DELETE CASCADE
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

    -- Usage rights (embedded)
    exclusivity_days        INT            NOT NULL DEFAULT 0,
    paid_ads_allowed        BIT(1)         NOT NULL DEFAULT b'0',
    whitelisting_allowed    BIT(1)         NOT NULL DEFAULT b'0',
    territory               VARCHAR(150)   NULL,

    negotiation_notes       TEXT           NULL,
    payment_terms           VARCHAR(40)    NOT NULL,

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
