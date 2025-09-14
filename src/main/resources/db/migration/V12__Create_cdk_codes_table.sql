CREATE TABLE cdk_codes (
                           id VARCHAR(36) PRIMARY KEY,
                           code VARCHAR(32) UNIQUE NOT NULL,
                           cdk_type VARCHAR(20) NOT NULL,
                           target_id VARCHAR(36) NOT NULL,
                           batch_id VARCHAR(36),
                           status VARCHAR(20) DEFAULT 'ACTIVE',
                           used_by_user_id VARCHAR(36),
                           used_time TIMESTAMP,
                           create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           deleted BOOLEAN DEFAULT FALSE
);

COMMENT ON TABLE cdk_codes IS 'CDK码表，支持套餐CDK和课程CDK统一管理';
COMMENT ON COLUMN cdk_codes.code IS 'CDK激活码';
COMMENT ON COLUMN cdk_codes.cdk_type IS 'CDK类型：SUBSCRIPTION_PLAN/COURSE';
COMMENT ON COLUMN cdk_codes.target_id IS '目标ID：套餐ID或课程ID';
COMMENT ON COLUMN cdk_codes.status IS 'CDK状态：ACTIVE/USED/DISABLED';
COMMENT ON COLUMN cdk_codes.used_by_user_id IS '使用者用户ID';
COMMENT ON COLUMN cdk_codes.used_time IS '使用时间';
COMMENT ON COLUMN cdk_codes.create_time IS '创建时间';
COMMENT ON COLUMN cdk_codes.update_time IS '更新时间';
COMMENT ON COLUMN cdk_codes.deleted IS '逻辑删除标记';
