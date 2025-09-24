-- 为用户订阅表添加唯一约束，确保每个用户只能有一个有效套餐
-- 这个约束可以防止并发创建重复套餐的问题

-- 添加唯一索引：每个用户只能有一个有效的订阅记录
CREATE UNIQUE INDEX uk_user_active_subscription
ON user_subscriptions (user_id)
WHERE deleted = false AND end_time > NOW();

-- 索引注释
COMMENT ON INDEX uk_user_active_subscription IS '用户有效订阅唯一约束：确保每个用户只能有一个有效的套餐订阅';