CREATE TABLE creator_subscription (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    creator_id  BIGINT       NOT NULL,
    plan        VARCHAR(20)  NOT NULL DEFAULT 'FREE',
    upgraded_at TIMESTAMP(6) NULL,
    created_at  TIMESTAMP(6) NOT NULL,
    updated_at  TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_creator_subscription_creator UNIQUE (creator_id),
    CONSTRAINT fk_creator_subscription_creator FOREIGN KEY (creator_id) REFERENCES creator (id) ON DELETE CASCADE
) ENGINE = InnoDB;

INSERT INTO creator_subscription (creator_id, plan, created_at, updated_at)
SELECT id, 'FREE', NOW(6), NOW(6)
FROM creator;
