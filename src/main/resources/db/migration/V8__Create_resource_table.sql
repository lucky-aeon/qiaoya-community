-- 创建资源表
CREATE TABLE resources (
    id VARCHAR(36) PRIMARY KEY,
    file_key VARCHAR(255) NOT NULL COMMENT '文件在S3中的键值',
    size BIGINT NOT NULL COMMENT '文件大小（字节）',
    format VARCHAR(50) NOT NULL COMMENT '文件格式（扩展名）',
    user_id VARCHAR(36) NOT NULL COMMENT '上传用户ID',
    resource_type VARCHAR(20) NOT NULL COMMENT '资源类型（IMAGE、VIDEO、DOCUMENT、AUDIO、OTHER）',
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除'
);

-- 创建索引
CREATE INDEX idx_resources_user_id ON resources(user_id);
CREATE INDEX idx_resources_resource_type ON resources(resource_type);
CREATE INDEX idx_resources_create_time ON resources(create_time);
CREATE UNIQUE INDEX idx_resources_file_key ON resources(file_key);

-- 添加注释
COMMENT ON TABLE resources IS '资源文件表';