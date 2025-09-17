-- 删除email字段，解决业务日志约束冲突
-- 将现有email数据备份到context_data中，然后删除email字段

-- 备份现有email数据到context_data（如果email存在且context_data为空）
UPDATE user_activity_logs 
SET context_data = jsonb_build_object('email', email)
WHERE email IS NOT NULL AND context_data IS NULL;

-- 如果context_data已有数据，则合并email信息
UPDATE user_activity_logs 
SET context_data = context_data || jsonb_build_object('email', email)
WHERE email IS NOT NULL AND context_data IS NOT NULL;

-- 删除email字段相关的索引
DROP INDEX IF EXISTS idx_user_activity_logs_email;

-- 删除email字段
ALTER TABLE user_activity_logs DROP COLUMN email;

-- 更新表注释
COMMENT ON TABLE user_activity_logs IS '用户活动日志表，记录用户登录、注册和业务操作等行为';