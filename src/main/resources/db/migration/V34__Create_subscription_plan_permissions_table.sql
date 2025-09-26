-- 套餐-接口权限关联表（存储套餐拥有哪些功能权限码）
CREATE TABLE subscription_plan_permissions (
    id VARCHAR(36) PRIMARY KEY,
    subscription_plan_id VARCHAR(36) NOT NULL,
    permission_code VARCHAR(100) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

-- 表与列注释
COMMENT ON TABLE subscription_plan_permissions IS '套餐-权限关联表，定义套餐拥有哪些接口/功能权限码';
COMMENT ON COLUMN subscription_plan_permissions.subscription_plan_id IS '套餐ID';
COMMENT ON COLUMN subscription_plan_permissions.permission_code IS '权限码（PlanPermission）';
COMMENT ON COLUMN subscription_plan_permissions.create_time IS '创建时间';
COMMENT ON COLUMN subscription_plan_permissions.update_time IS '更新时间';
COMMENT ON COLUMN subscription_plan_permissions.deleted IS '逻辑删除标记';

-- 惟一约束，避免重复绑定
CREATE UNIQUE INDEX uk_plan_permission ON subscription_plan_permissions (subscription_plan_id, permission_code);

