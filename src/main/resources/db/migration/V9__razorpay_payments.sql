ALTER TABLE invoice
    ADD COLUMN razorpay_payment_link_id  VARCHAR(64)  NULL,
    ADD COLUMN razorpay_payment_link_url VARCHAR(255) NULL;

ALTER TABLE invoice_payment
    ADD COLUMN razorpay_payment_id VARCHAR(64) NULL;

CREATE UNIQUE INDEX uk_invoice_payment_razorpay_payment_id ON invoice_payment (razorpay_payment_id);

ALTER TABLE creator_subscription
    ADD COLUMN razorpay_payment_link_id  VARCHAR(64)  NULL,
    ADD COLUMN razorpay_payment_link_url VARCHAR(255) NULL,
    ADD COLUMN razorpay_payment_id       VARCHAR(64)  NULL;

CREATE UNIQUE INDEX uk_creator_subscription_razorpay_payment_id ON creator_subscription (razorpay_payment_id);
