-- 创建套餐-菜单关联表（预留，暂无菜单功能）
CREATE TABLE subscription_plan_menus (
    id VARCHAR(36) PRIMARY KEY,
    subscription_plan_id VARCHAR(36) NOT NULL COMMENT '套餐ID',
    menu_id VARCHAR(36) NOT NULL COMMENT '菜单ID',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '逻辑删除标记',
    
    UNIQUE KEY uk_plan_menu_deleted (subscription_plan_id, menu_id, deleted),
    KEY idx_subscription_plan_id (subscription_plan_id),
    KEY idx_menu_id (menu_id),
    KEY idx_create_time (create_time),
    
    -- 外键约束（仅套餐ID，菜单表暂不存在）
    CONSTRAINT fk_subscription_plan_menus_plan_id 
        FOREIGN KEY (subscription_plan_id) 
        REFERENCES subscription_plans(id) 
        ON DELETE CASCADE
);

-- 添加表注释
ALTER TABLE subscription_plan_menus COMMENT = '套餐-菜单关联表，定义套餐可访问哪些菜单（预留功能）';