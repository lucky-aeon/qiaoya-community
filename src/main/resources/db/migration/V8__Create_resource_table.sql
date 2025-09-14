-- 创建资源表
CREATE TABLE resources (
    id VARCHAR(36) PRIMARY KEY,
    file_key VARCHAR(255) NOT NULL,
    size BIGINT NOT NULL,
    format VARCHAR(50) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    resource_type VARCHAR(20) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 创建索引
CREATE INDEX idx_resources_user_id ON resources(user_id);
CREATE INDEX idx_resources_resource_type ON resources(resource_type);
CREATE INDEX idx_resources_create_time ON resources(create_time);
CREATE UNIQUE INDEX idx_resources_file_key ON resources(file_key);

-- 添加表注释
COMMENT ON TABLE resources IS '资源文件表';
COMMENT ON COLUMN resources.id IS '资源ID (UUID格式)';
COMMENT ON COLUMN resources.file_key IS '文件在S3中的键值';
COMMENT ON COLUMN resources.size IS '文件大小（字节）';
COMMENT ON COLUMN resources.format IS '文件格式（扩展名）';
COMMENT ON COLUMN resources.user_id IS '上传用户ID';
COMMENT ON COLUMN resources.resource_type IS '资源类型（IMAGE、VIDEO、DOCUMENT、AUDIO、OTHER）';
COMMENT ON COLUMN resources.original_name IS '原始文件名';
COMMENT ON COLUMN resources.create_time IS '创建时间';
COMMENT ON COLUMN resources.update_time IS '更新时间';
COMMENT ON COLUMN resources.deleted IS '是否删除';