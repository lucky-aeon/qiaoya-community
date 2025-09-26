-- 将中间表改为物理删除：移除逻辑删除字段
ALTER TABLE IF EXISTS subscription_plan_menus DROP COLUMN IF EXISTS deleted;
ALTER TABLE IF EXISTS subscription_plan_permissions DROP COLUMN IF EXISTS deleted;

