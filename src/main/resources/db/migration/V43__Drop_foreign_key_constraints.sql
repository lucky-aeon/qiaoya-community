-- 删除所有外键约束
-- 这个迁移文件用于移除项目中的所有外键约束以提高性能

-- 删除 update_log_changes 表的外键约束
ALTER TABLE update_log_changes DROP CONSTRAINT IF EXISTS update_log_changes_update_log_id_fkey;

-- 删除 follows 表的外键约束
ALTER TABLE follows DROP CONSTRAINT IF EXISTS fk_follows_follower;

-- 删除 orders 表的外键约束
ALTER TABLE orders DROP CONSTRAINT IF EXISTS fk_orders_user_id;
ALTER TABLE orders DROP CONSTRAINT IF EXISTS fk_orders_cdk_code;
