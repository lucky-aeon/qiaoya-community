CREATE TABLE subscription_plan_menus (
                                         id VARCHAR(36) PRIMARY KEY,
                                         subscription_plan_id VARCHAR(36) NOT NULL,
                                         menu_id VARCHAR(36) NOT NULL,
                                         create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                         update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                         deleted BOOLEAN DEFAULT FALSE
);

-- 表注释
COMMENT ON TABLE subscription_plan_menus IS '套餐-菜单关联表，定义套餐可访问哪些菜单（预留功能）';

-- 列注释
COMMENT ON COLUMN subscription_plan_menus.subscription_plan_id IS '套餐ID';
COMMENT ON COLUMN subscription_plan_menus.menu_id IS '菜单ID';
COMMENT ON COLUMN subscription_plan_menus.create_time IS '创建时间';
COMMENT ON COLUMN subscription_plan_menus.update_time IS '更新时间';
COMMENT ON COLUMN subscription_plan_menus.deleted IS '逻辑删除标记';
