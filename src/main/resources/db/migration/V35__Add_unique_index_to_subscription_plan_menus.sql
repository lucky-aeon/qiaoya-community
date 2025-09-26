-- 为套餐-菜单关联表添加唯一索引，防止重复绑定
-- 注意：此处列名 menu_id 作为“菜单码”使用（无需更名为 menu_code）
DO $$
BEGIN
    -- 仅当表存在时才创建索引；索引不存在才创建
    IF EXISTS (
        SELECT 1 FROM information_schema.tables 
        WHERE table_schema = current_schema() AND table_name = 'subscription_plan_menus'
    ) THEN
        IF NOT EXISTS (
            SELECT 1 FROM pg_indexes 
            WHERE schemaname = current_schema() AND indexname = 'uk_plan_menu'
        ) THEN
            EXECUTE 'CREATE UNIQUE INDEX uk_plan_menu ON subscription_plan_menus (subscription_plan_id, menu_id)';
        END IF;
    END IF;
END$$;
