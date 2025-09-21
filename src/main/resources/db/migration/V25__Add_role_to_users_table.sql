-- 为用户表添加角色字段
ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';

-- 创建角色索引
CREATE INDEX idx_users_role ON users(role);

-- 添加角色约束
ALTER TABLE users ADD CONSTRAINT chk_users_role
    CHECK (role IN ('USER', 'ADMIN'));

-- 添加字段注释
COMMENT ON COLUMN users.role IS '用户角色：USER-普通用户，ADMIN-管理员';