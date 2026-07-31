-- CreatorOS Phase 1: authentication + creator profile.
-- Every future domain table (brand_deal, invoice, expense, ...) references creator(id).

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

-- Short-lived one-time codes for signup verification and password reset.
CREATE TABLE otp_token (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    creator_id  BIGINT      NOT NULL,
    code        VARCHAR(6)  NOT NULL,
    purpose     VARCHAR(30) NOT NULL,
    expires_at  DATETIME(6) NOT NULL,
    consumed_at DATETIME(6) NULL,
    attempts    INT         NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_otp_token_creator FOREIGN KEY (creator_id) REFERENCES creator (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_otp_token_creator_purpose ON otp_token (creator_id, purpose);
