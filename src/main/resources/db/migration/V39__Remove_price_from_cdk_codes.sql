-- 移除 CDK 表中的价格字段（价格由绑定的课程/套餐决定）
-- 数据库：PostgreSQL

-- 删除与价格相关的索引（若存在）
DROP INDEX IF EXISTS idx_cdk_codes_price;

-- 删除价格字段
ALTER TABLE cdk_codes
DROP COLUMN IF EXISTS price;

-- 备注：保留 acquisition_type 与 remark 字段

