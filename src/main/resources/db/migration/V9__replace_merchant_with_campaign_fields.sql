ALTER TABLE coupon_campaign
    ADD COLUMN merchant_name VARCHAR(255),
    ADD COLUMN merchant_address VARCHAR(255);

UPDATE coupon_campaign campaign
SET merchant_name = merchant.name,
    merchant_address = merchant.address
FROM merchant
WHERE campaign.merchant_id = merchant.id;

UPDATE coupon_campaign
SET merchant_name = COALESCE(merchant_name, title)
WHERE merchant_name IS NULL;

ALTER TABLE coupon_campaign
    ALTER COLUMN merchant_name SET NOT NULL;

ALTER TABLE coupon_campaign
    DROP COLUMN merchant_id;

DROP TABLE merchant;
