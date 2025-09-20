-- 创建更新日志表
CREATE TABLE update_logs (
    id VARCHAR(36) PRIMARY KEY,
    version VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    author_id VARCHAR(36),
    status VARCHAR(20) DEFAULT 'DRAFT',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

-- 创建更新日志变更详情表
CREATE TABLE update_log_changes (
    id VARCHAR(36) PRIMARY KEY,
    update_log_id VARCHAR(36) NOT NULL,
    type VARCHAR(20) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    sort_order INTEGER DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (update_log_id) REFERENCES update_logs(id) ON DELETE CASCADE
);

-- 添加表注释
COMMENT ON TABLE update_logs IS '更新日志表';
COMMENT ON COLUMN update_logs.id IS '主键ID';
COMMENT ON COLUMN update_logs.version IS '版本号';
COMMENT ON COLUMN update_logs.title IS '更新标题';
COMMENT ON COLUMN update_logs.description IS '更新描述';
COMMENT ON COLUMN update_logs.author_id IS '作者ID';
COMMENT ON COLUMN update_logs.status IS '状态：DRAFT-草稿，PUBLISHED-已发布';
COMMENT ON COLUMN update_logs.create_time IS '创建时间';
COMMENT ON COLUMN update_logs.update_time IS '更新时间';
COMMENT ON COLUMN update_logs.deleted IS '逻辑删除标记';

COMMENT ON TABLE update_log_changes IS '更新日志变更详情表';
COMMENT ON COLUMN update_log_changes.id IS '主键ID';
COMMENT ON COLUMN update_log_changes.update_log_id IS '更新日志ID';
COMMENT ON COLUMN update_log_changes.type IS '变更类型：FEATURE-新功能，IMPROVEMENT-改进，BUGFIX-修复，SECURITY-安全，BREAKING-破坏性变更';
COMMENT ON COLUMN update_log_changes.title IS '变更标题';
COMMENT ON COLUMN update_log_changes.description IS '变更描述';
COMMENT ON COLUMN update_log_changes.category IS '变更分类';
COMMENT ON COLUMN update_log_changes.sort_order IS '排序';
COMMENT ON COLUMN update_log_changes.create_time IS '创建时间';
COMMENT ON COLUMN update_log_changes.update_time IS '更新时间';
COMMENT ON COLUMN update_log_changes.deleted IS '逻辑删除标记';