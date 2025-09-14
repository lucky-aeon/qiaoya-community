CREATE TABLE subscription_plans (
                                    id VARCHAR(36) PRIMARY KEY,
                                    name VARCHAR(100) NOT NULL,
                                    level INTEGER NOT NULL,
                                    validity_months INTEGER NOT NULL,
                                    price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                                    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                                    description VARCHAR(1000),
                                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    deleted BOOLEAN DEFAULT FALSE
);

-- 表注释
COMMENT ON TABLE subscription_plans IS '套餐定义表，存储套餐的基本信息';

-- 列注释
COMMENT ON COLUMN subscription_plans.name IS '套餐名称';
COMMENT ON COLUMN subscription_plans.level IS '套餐级别，用于升级判断';
COMMENT ON COLUMN subscription_plans.validity_months IS '有效期（月）';
COMMENT ON COLUMN subscription_plans.price IS '套餐价格';
COMMENT ON COLUMN subscription_plans.status IS '套餐状态：ACTIVE, INACTIVE';
COMMENT ON COLUMN subscription_plans.description IS '套餐描述';
COMMENT ON COLUMN subscription_plans.create_time IS '创建时间';
COMMENT ON COLUMN subscription_plans.update_time IS '更新时间';
COMMENT ON COLUMN subscription_plans.deleted IS '逻辑删除标记';
