CREATE TABLE user_subscriptions (
                                    id VARCHAR(36) PRIMARY KEY,
                                    user_id VARCHAR(36) NOT NULL,
                                    subscription_plan_id VARCHAR(36) NOT NULL,
                                    start_time TIMESTAMP NOT NULL,
                                    end_time TIMESTAMP NOT NULL,
                                    status VARCHAR(20) DEFAULT 'ACTIVE',
                                    cdk_code VARCHAR(32),
                                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    deleted BOOLEAN DEFAULT FALSE
);

-- 表注释
COMMENT ON TABLE user_subscriptions IS '用户订阅表，记录用户的套餐订阅关系和有效期';

-- 列注释
COMMENT ON COLUMN user_subscriptions.user_id IS '用户ID';
COMMENT ON COLUMN user_subscriptions.subscription_plan_id IS '套餐ID';
COMMENT ON COLUMN user_subscriptions.start_time IS '开始时间';
COMMENT ON COLUMN user_subscriptions.end_time IS '结束时间';
COMMENT ON COLUMN user_subscriptions.status IS '订阅状态：ACTIVE/EXPIRED/CANCELLED';
COMMENT ON COLUMN user_subscriptions.cdk_code IS '激活时使用的CDK码';
COMMENT ON COLUMN user_subscriptions.create_time IS '创建时间';
COMMENT ON COLUMN user_subscriptions.update_time IS '更新时间';
COMMENT ON COLUMN user_subscriptions.deleted IS '逻辑删除标记';
