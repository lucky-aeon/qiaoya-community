-- 目的：允许同一用户存在多条订阅记录（例如免费套餐过期后再次赠送/续费）
-- 数据库：PostgreSQL
-- 变更：移除 user_subscriptions 表上的唯一索引约束

-- 兼容旧索引名（基于 deleted 布尔字段的版本）
DROP INDEX IF EXISTS uk_user_active_subscription;

-- 兼容迁移到 deleted_at 时间戳后的索引名
DROP INDEX IF EXISTS uk_user_active_subscription_v2;

-- 注意：如果后续需要性能优化，可按需要补充普通（非唯一）索引，例如：
-- CREATE INDEX IF NOT EXISTS idx_user_subscriptions_user_id ON user_subscriptions(user_id);

