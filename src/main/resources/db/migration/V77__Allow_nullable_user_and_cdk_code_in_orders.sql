-- 允许服务订单不依赖用户和CDK
-- PostgreSQL

ALTER TABLE orders
    ALTER COLUMN user_id DROP NOT NULL,
    ALTER COLUMN cdk_code DROP NOT NULL;

COMMENT ON COLUMN orders.user_id IS '用户ID，关联users表，可为空（如后台手工录入服务订单）';
COMMENT ON COLUMN orders.cdk_code IS '关联的CDK激活码，可为空（如后台手工录入服务订单）';
