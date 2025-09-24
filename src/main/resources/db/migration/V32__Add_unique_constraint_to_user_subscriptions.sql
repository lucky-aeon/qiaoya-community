-- 为用户订阅表添加唯一约束，确保每个用户只能有一个未删除的套餐
-- 注意：时间有效性检查将在应用层进行，避免PostgreSQL中volatile函数的限制

-- 添加唯一索引：每个用户只能有一个未删除的订阅记录
CREATE UNIQUE INDEX uk_user_active_subscription
ON user_subscriptions (user_id)
WHERE deleted = false;

-- 索引注释
COMMENT ON INDEX uk_user_active_subscription IS '用户订阅唯一约束：确保每个用户只能有一个未删除的套餐订阅，时间有效性由应用层控制';