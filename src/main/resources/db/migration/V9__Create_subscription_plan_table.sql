-- 创建套餐表
CREATE TABLE subscription_plans (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '套餐名称',
    level INTEGER NOT NULL COMMENT '套餐级别，用于升级判断',
    validity_months INTEGER NOT NULL COMMENT '有效期（月）',
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '套餐价格',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '套餐状态：ACTIVE, INACTIVE',
    description VARCHAR(1000) COMMENT '套餐描述',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '逻辑删除标记',
    
    UNIQUE KEY uk_name_deleted (name, deleted),
    KEY idx_level (level),
    KEY idx_status (status),
    KEY idx_create_time (create_time)
);

-- 添加表注释
ALTER TABLE subscription_plans COMMENT = '套餐定义表，存储套餐的基本信息';