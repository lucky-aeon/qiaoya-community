-- 新增：CDK 价格/套餐策略；订单 extra(JSON)
-- 数据库：PostgreSQL

-- 1) cdk_codes 表增加覆盖价格与套餐策略
ALTER TABLE cdk_codes
    ADD COLUMN IF NOT EXISTS price NUMERIC(12,2) NULL,
    ADD COLUMN IF NOT EXISTS subscription_strategy VARCHAR(32) NULL;

COMMENT ON COLUMN cdk_codes.price IS 'CDK自定义价格（覆盖商品原价，升级补差/折扣）';
COMMENT ON COLUMN cdk_codes.subscription_strategy IS '套餐策略：UPGRADE/购买PURCHASE，仅对套餐型CDK有效';

-- 可选：查询优化（非必需）
-- CREATE INDEX IF NOT EXISTS idx_cdk_codes_subscription_strategy ON cdk_codes(subscription_strategy);

-- 2) orders 表增加 extra JSONB 承载结构化扩展信息
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS extra JSONB;

COMMENT ON COLUMN orders.extra IS '订单额外信息（JSON），如acquisitionType、subscriptionStrategy、cdkPrice等';

