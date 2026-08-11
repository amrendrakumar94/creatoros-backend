ALTER TABLE creator_subscription
    ADD COLUMN trial_ends_at TIMESTAMP(6) NULL AFTER plan,
    ADD COLUMN subscribed_at TIMESTAMP(6) NULL AFTER trial_ends_at;

-- Backfill a trial window for existing FREE rows before renaming plan values.
UPDATE creator_subscription SET trial_ends_at = DATE_ADD(created_at, INTERVAL 30 DAY) WHERE plan = 'FREE';
UPDATE creator_subscription SET subscribed_at = upgraded_at WHERE upgraded_at IS NOT NULL;

UPDATE creator_subscription SET plan = 'TRIAL' WHERE plan = 'FREE';
UPDATE creator_subscription SET plan = 'SUBSCRIPTION' WHERE plan = 'PRO';

ALTER TABLE creator_subscription
    DROP COLUMN upgraded_at,
    MODIFY plan VARCHAR(20) NOT NULL DEFAULT 'TRIAL';
