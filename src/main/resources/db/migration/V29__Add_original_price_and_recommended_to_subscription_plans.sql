-- 为subscription_plans表新增原价和是否推荐字段

ALTER TABLE subscription_plans
    ADD COLUMN original_price DECIMAL(10,2) DEFAULT 0.00,
    ADD COLUMN recommended BOOLEAN DEFAULT FALSE;

-- 添加字段注释
COMMENT ON COLUMN subscription_plans.original_price IS '套餐原价';
COMMENT ON COLUMN subscription_plans.recommended IS '是否推荐该套餐';

-- 将历史数据的原价回填为当前售价，避免为0
UPDATE subscription_plans SET original_price = price WHERE original_price = 0.00;
