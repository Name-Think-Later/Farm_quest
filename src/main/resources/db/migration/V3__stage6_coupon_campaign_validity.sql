ALTER TABLE coupon_campaign
    ADD COLUMN valid_from TIMESTAMPTZ,
    ADD COLUMN valid_until TIMESTAMPTZ;
