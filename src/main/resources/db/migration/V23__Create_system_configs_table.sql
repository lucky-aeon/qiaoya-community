-- 创建系统配置表
CREATE TABLE system_configs (
    id VARCHAR(36) PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    data TEXT NOT NULL,
    description VARCHAR(200),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 创建类型唯一索引
CREATE UNIQUE INDEX idx_system_configs_type ON system_configs(type) WHERE deleted = FALSE;

-- 创建类型索引
CREATE INDEX idx_system_configs_type_all ON system_configs(type);

-- 添加表注释
COMMENT ON TABLE system_configs IS '系统配置表，存储各种系统级配置信息';
COMMENT ON COLUMN system_configs.id IS '配置ID (UUID格式)';
COMMENT ON COLUMN system_configs.type IS '配置类型，如DEFAULT_SUBSCRIPTION_PLAN等';
COMMENT ON COLUMN system_configs.data IS '配置数据，JSON格式存储';
COMMENT ON COLUMN system_configs.description IS '配置描述';
COMMENT ON COLUMN system_configs.create_time IS '创建时间';
COMMENT ON COLUMN system_configs.update_time IS '更新时间';
COMMENT ON COLUMN system_configs.deleted IS '是否删除';
