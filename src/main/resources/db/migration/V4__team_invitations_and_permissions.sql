CREATE TABLE team_invitation (
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    creator_id            BIGINT       NOT NULL,
    email                 VARCHAR(255) NOT NULL,
    invite_token          VARCHAR(100) NOT NULL,
    role                  VARCHAR(20)  NOT NULL,
    expires_on            DATE         NOT NULL,
    accepted_at           TIMESTAMP(6) NULL,
    accepted_by_creator_id BIGINT      NULL,
    revoked               BIT(1)       NOT NULL DEFAULT b'0',
    created_at            TIMESTAMP(6) NOT NULL,
    updated_at            TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_team_invitation_token UNIQUE (invite_token),
    CONSTRAINT fk_team_invitation_creator FOREIGN KEY (creator_id) REFERENCES creator (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_team_invitation_creator ON team_invitation (creator_id);
CREATE INDEX idx_team_invitation_email ON team_invitation (email);

CREATE TABLE team_member (
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    creator_id            BIGINT       NOT NULL,
    member_creator_id     BIGINT       NOT NULL,
    email                 VARCHAR(255) NOT NULL,
    name                  VARCHAR(150) NOT NULL,
    role                  VARCHAR(20)  NOT NULL,
    active                BIT(1)       NOT NULL DEFAULT b'1',
    invited_by_creator_id BIGINT       NOT NULL,
    created_at            TIMESTAMP(6) NOT NULL,
    updated_at            TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_team_member_creator FOREIGN KEY (creator_id) REFERENCES creator (id) ON DELETE CASCADE,
    CONSTRAINT fk_team_member_member_creator FOREIGN KEY (member_creator_id) REFERENCES creator (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE UNIQUE INDEX uk_team_member_creator_member ON team_member (creator_id, member_creator_id);
CREATE INDEX idx_team_member_creator ON team_member (creator_id);

CREATE TABLE team_member_permission (
    team_member_id BIGINT      NOT NULL,
    permission     VARCHAR(40) NOT NULL,
    PRIMARY KEY (team_member_id, permission),
    CONSTRAINT fk_team_member_permission_member FOREIGN KEY (team_member_id) REFERENCES team_member (id) ON DELETE CASCADE
) ENGINE = InnoDB;
