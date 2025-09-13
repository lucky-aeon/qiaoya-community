-- 创建章节表
CREATE TABLE chapters (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    course_id VARCHAR(36) NOT NULL,
    author_id VARCHAR(36) NOT NULL,
    sort_order INTEGER NOT NULL,
    reading_time INTEGER DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 创建索引
CREATE INDEX idx_chapters_course_id ON chapters(course_id);
CREATE INDEX idx_chapters_author_id ON chapters(author_id);
CREATE INDEX idx_chapters_sort_order ON chapters(course_id, sort_order);
CREATE INDEX idx_chapters_create_time ON chapters(create_time);

-- 添加表注释
COMMENT ON TABLE chapters IS '课程章节表';
COMMENT ON COLUMN chapters.id IS '章节ID (UUID格式)';
COMMENT ON COLUMN chapters.title IS '章节标题';
COMMENT ON COLUMN chapters.content IS '章节内容';
COMMENT ON COLUMN chapters.course_id IS '所属课程ID';
COMMENT ON COLUMN chapters.author_id IS '作者用户ID';
COMMENT ON COLUMN chapters.sort_order IS '排序序号';
COMMENT ON COLUMN chapters.reading_time IS '预计阅读时长（分钟）';
COMMENT ON COLUMN chapters.create_time IS '创建时间';
COMMENT ON COLUMN chapters.update_time IS '更新时间';
COMMENT ON COLUMN chapters.deleted IS '是否删除';