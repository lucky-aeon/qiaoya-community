-- 兜底迁移：确保存在 subscription_plan_menus 表（历史库可能缺失 V11）
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables 
        WHERE table_schema = current_schema() AND table_name = 'subscription_plan_menus'
    ) THEN
        -- 创建表（与旧版本结构一致，包含 deleted 字段；后续迁移会移除此列）
        EXECUTE $DDL$
        CREATE TABLE subscription_plan_menus (
            id VARCHAR(36) PRIMARY KEY,
            subscription_plan_id VARCHAR(36) NOT NULL,
            menu_id VARCHAR(36) NOT NULL,
            create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            deleted BOOLEAN DEFAULT FALSE
        )
        $DDL$;

        -- 表与列注释
        EXECUTE $DDL$ COMMENT ON TABLE subscription_plan_menus IS '套餐-菜单关联表，定义套餐可访问哪些菜单（预留功能）' $DDL$;
        EXECUTE $DDL$ COMMENT ON COLUMN subscription_plan_menus.subscription_plan_id IS '套餐ID' $DDL$;
        EXECUTE $DDL$ COMMENT ON COLUMN subscription_plan_menus.menu_id IS '菜单ID（作为菜单码使用）' $DDL$;
        EXECUTE $DDL$ COMMENT ON COLUMN subscription_plan_menus.create_time IS '创建时间' $DDL$;
        EXECUTE $DDL$ COMMENT ON COLUMN subscription_plan_menus.update_time IS '更新时间' $DDL$;
        EXECUTE $DDL$ COMMENT ON COLUMN subscription_plan_menus.deleted IS '逻辑删除标记' $DDL$;
    END IF;

    -- 创建唯一索引（若不存在）
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes 
        WHERE schemaname = current_schema() AND indexname = 'uk_plan_menu'
    ) THEN
        IF EXISTS (
            SELECT 1 FROM information_schema.tables 
            WHERE table_schema = current_schema() AND table_name = 'subscription_plan_menus'
        ) THEN
            EXECUTE 'CREATE UNIQUE INDEX uk_plan_menu ON subscription_plan_menus (subscription_plan_id, menu_id)';
        END IF;
    END IF;
END$$;
