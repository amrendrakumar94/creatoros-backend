ALTER TABLE creator
    ADD COLUMN state      VARCHAR(60) NULL AFTER city,
    ADD COLUMN state_code VARCHAR(2)  NULL AFTER state;

CREATE TABLE document_counter (
    creator_id     BIGINT      NOT NULL,
    doc_type       VARCHAR(20) NOT NULL,
    financial_year VARCHAR(9)  NOT NULL,
    last_sequence  INT         NOT NULL DEFAULT 0,
    PRIMARY KEY (creator_id, doc_type, financial_year),
    CONSTRAINT fk_document_counter_creator FOREIGN KEY (creator_id) REFERENCES creator (id) ON DELETE CASCADE
) ENGINE = InnoDB;

INSERT INTO document_counter (creator_id, doc_type, financial_year, last_sequence)
SELECT creator_id,
       'BRAND_DEAL',
       CASE
           WHEN MONTH(created_at) >= 4 THEN CONCAT(YEAR(created_at), '-', LPAD((YEAR(created_at) + 1) % 100, 2, '0'))
           ELSE CONCAT(YEAR(created_at) - 1, '-', LPAD(YEAR(created_at) % 100, 2, '0'))
       END,
       MAX(CAST(SUBSTRING_INDEX(deal_number, '-', -1) AS UNSIGNED))
FROM brand_deal
GROUP BY 1, 2, 3;
