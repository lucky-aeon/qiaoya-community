-- 创建课程表
CREATE TABLE courses (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    tech_stack JSON,
    project_url VARCHAR(500),
    tags JSON,
    rating DECIMAL(3,2) DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    author_id VARCHAR(36) NOT NULL,
    total_reading_time INTEGER DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 创建索引
CREATE INDEX idx_courses_author_id ON courses(author_id);
CREATE INDEX idx_courses_status ON courses(status);
CREATE INDEX idx_courses_rating ON courses(rating);
CREATE INDEX idx_courses_create_time ON courses(create_time);

-- 添加表注释
COMMENT ON TABLE courses IS '课程表';
COMMENT ON COLUMN courses.id IS '课程ID (UUID格式)';
COMMENT ON COLUMN courses.title IS '课程标题';
COMMENT ON COLUMN courses.description IS '课程简介';
COMMENT ON COLUMN courses.tech_stack IS '技术栈（JSON数组）';
COMMENT ON COLUMN courses.project_url IS '项目地址';
COMMENT ON COLUMN courses.tags IS '标签（JSON数组）';
COMMENT ON COLUMN courses.rating IS '课程评分（0.00-5.00）';
COMMENT ON COLUMN courses.status IS '状态：PENDING-待更新，IN_PROGRESS-更新中，COMPLETED-已完成';
COMMENT ON COLUMN courses.author_id IS '作者用户ID';
COMMENT ON COLUMN courses.total_reading_time IS '总阅读时长（分钟）';
COMMENT ON COLUMN courses.create_time IS '创建时间';
COMMENT ON COLUMN courses.update_time IS '更新时间';
COMMENT ON COLUMN courses.deleted IS '是否删除';