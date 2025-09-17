-- 扩展用户活动日志表以支持业务操作追踪
-- 为支持更丰富的业务操作，扩展用户活动日志表

-- 添加业务相关字段
ALTER TABLE user_activity_logs 
ADD COLUMN target_type VARCHAR(50),          -- 目标类型：POST、COURSE、USER等
ADD COLUMN target_id VARCHAR(36),            -- 目标对象ID
ADD COLUMN request_method VARCHAR(10),       -- HTTP请求方法
ADD COLUMN request_path VARCHAR(500),        -- 请求路径
ADD COLUMN execution_time_ms INTEGER,        -- 执行时间（毫秒）
ADD COLUMN session_id VARCHAR(64),           -- 会话ID
ADD COLUMN context_data JSONB;               -- 扩展上下文数据（JSON格式）

-- 为新字段添加索引
CREATE INDEX idx_user_activity_logs_target ON user_activity_logs(target_type, target_id);
CREATE INDEX idx_user_activity_logs_session ON user_activity_logs(session_id);
CREATE INDEX idx_user_activity_logs_request_path ON user_activity_logs(request_path);
CREATE INDEX idx_user_activity_logs_request_method ON user_activity_logs(request_method);

-- 添加字段注释
COMMENT ON COLUMN user_activity_logs.target_type IS '目标类型：POST、COURSE、USER等';
COMMENT ON COLUMN user_activity_logs.target_id IS '目标对象ID';
COMMENT ON COLUMN user_activity_logs.request_method IS 'HTTP请求方法：GET、POST等';
COMMENT ON COLUMN user_activity_logs.request_path IS '请求路径';
COMMENT ON COLUMN user_activity_logs.execution_time_ms IS '接口执行时间（毫秒）';
COMMENT ON COLUMN user_activity_logs.session_id IS '用户会话ID';
COMMENT ON COLUMN user_activity_logs.context_data IS '扩展上下文数据，JSON格式存储';