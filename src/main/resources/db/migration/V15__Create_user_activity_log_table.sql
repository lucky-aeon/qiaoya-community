-- 创建用户活动日志表
CREATE TABLE user_activity_logs (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36),  -- 关联用户ID，登录失败时可能为NULL
    email VARCHAR(255) NOT NULL,
    activity_type VARCHAR(50) NOT NULL,  -- 活动类型：LOGIN_SUCCESS, LOGIN_FAILED, REGISTER_SUCCESS等
    browser VARCHAR(500),  -- 浏览器信息
    equipment VARCHAR(100),  -- 设备信息
    ip VARCHAR(45) NOT NULL,  -- IP地址，支持IPv6
    user_agent TEXT,  -- 完整的User-Agent信息
    failure_reason VARCHAR(500),  -- 失败原因，成功时为NULL
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 创建索引优化查询性能
CREATE INDEX idx_user_activity_logs_user_id ON user_activity_logs(user_id);
CREATE INDEX idx_user_activity_logs_email ON user_activity_logs(email);
CREATE INDEX idx_user_activity_logs_activity_type ON user_activity_logs(activity_type);
CREATE INDEX idx_user_activity_logs_created_at ON user_activity_logs(created_at);
CREATE INDEX idx_user_activity_logs_ip ON user_activity_logs(ip);
CREATE INDEX idx_user_activity_logs_deleted ON user_activity_logs(deleted);


-- 添加表注释
COMMENT ON TABLE user_activity_logs IS '用户活动日志表，记录用户登录、注册等行为';
COMMENT ON COLUMN user_activity_logs.id IS '主键ID';
COMMENT ON COLUMN user_activity_logs.user_id IS '用户ID，登录失败时可能为NULL';
COMMENT ON COLUMN user_activity_logs.email IS '用户邮箱';
COMMENT ON COLUMN user_activity_logs.activity_type IS '活动类型：LOGIN_SUCCESS, LOGIN_FAILED, REGISTER_SUCCESS, REGISTER_FAILED等';
COMMENT ON COLUMN user_activity_logs.browser IS '浏览器信息';
COMMENT ON COLUMN user_activity_logs.equipment IS '设备信息';
COMMENT ON COLUMN user_activity_logs.ip IS 'IP地址';
COMMENT ON COLUMN user_activity_logs.user_agent IS '完整的User-Agent信息';
COMMENT ON COLUMN user_activity_logs.failure_reason IS '失败原因，成功时为NULL';
COMMENT ON COLUMN user_activity_logs.created_at IS '创建时间';
COMMENT ON COLUMN user_activity_logs.updated_at IS '更新时间';
COMMENT ON COLUMN user_activity_logs.deleted IS '软删除标记';