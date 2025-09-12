-- 创建用户表
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description TEXT,
    avatar VARCHAR(500),
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    subscribe_external_messages BOOLEAN NOT NULL DEFAULT FALSE,
    max_concurrent_devices INTEGER NOT NULL DEFAULT 5,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 创建邮箱唯一索引
CREATE UNIQUE INDEX idx_users_email ON users(email) WHERE deleted = FALSE;

-- 创建状态索引
CREATE INDEX idx_users_status ON users(status);

-- 创建创建时间索引
CREATE INDEX idx_users_create_time ON users(create_time);

-- 添加约束
ALTER TABLE users ADD CONSTRAINT chk_users_status 
    CHECK (status IN ('ACTIVE', 'INACTIVE', 'BANNED'));

ALTER TABLE users ADD CONSTRAINT chk_users_max_devices
    CHECK (max_concurrent_devices > 0 AND max_concurrent_devices <= 10);

-- 添加表注释
COMMENT ON TABLE users IS '用户表';
COMMENT ON COLUMN users.id IS '用户ID (UUID格式)';
COMMENT ON COLUMN users.name IS '用户名称';
COMMENT ON COLUMN users.description IS '用户描述';
COMMENT ON COLUMN users.avatar IS '头像URL';
COMMENT ON COLUMN users.email IS '邮箱地址';
COMMENT ON COLUMN users.password IS '加密密码';
COMMENT ON COLUMN users.status IS '用户状态：ACTIVE-正常，INACTIVE-禁用，BANNED-封禁';
COMMENT ON COLUMN users.subscribe_external_messages IS '是否订阅站外消息';
COMMENT ON COLUMN users.max_concurrent_devices IS '最大并发设备数';
COMMENT ON COLUMN users.create_time IS '创建时间';
COMMENT ON COLUMN users.update_time IS '更新时间';
COMMENT ON COLUMN users.deleted IS '是否删除';