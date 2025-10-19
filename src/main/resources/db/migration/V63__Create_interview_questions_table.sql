-- 面试题库表
CREATE TABLE interview_questions (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    answer TEXT NOT NULL,
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    category_id VARCHAR(36) NOT NULL,
    author_id VARCHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    publish_time TIMESTAMP NULL,
    extra JSONB NOT NULL DEFAULT '{}'::jsonb,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 索引（按高频访问维度）
CREATE INDEX idx_interview_questions_category ON interview_questions(category_id);
CREATE INDEX idx_interview_questions_author ON interview_questions(author_id);
CREATE INDEX idx_interview_questions_status_pubtime ON interview_questions(status, publish_time);

-- 注释
COMMENT ON TABLE interview_questions IS '面试题库表';
COMMENT ON COLUMN interview_questions.id IS '主键ID (UUID)';
COMMENT ON COLUMN interview_questions.title IS '题目标题';
COMMENT ON COLUMN interview_questions.description IS '题目描述/问题本身';
COMMENT ON COLUMN interview_questions.answer IS '参考答案/要点';
COMMENT ON COLUMN interview_questions.rating IS '评分（1-5）';
COMMENT ON COLUMN interview_questions.category_id IS '分类ID（CategoryType=INTERVIEW）';
COMMENT ON COLUMN interview_questions.author_id IS '作者用户ID';
COMMENT ON COLUMN interview_questions.status IS '状态：DRAFT/PUBLISHED/ARCHIVED';
COMMENT ON COLUMN interview_questions.publish_time IS '发布时间（发布时写入）';
COMMENT ON COLUMN interview_questions.extra IS '扩展字段（低频信息）';
COMMENT ON COLUMN interview_questions.create_time IS '创建时间';
COMMENT ON COLUMN interview_questions.update_time IS '更新时间';
COMMENT ON COLUMN interview_questions.deleted IS '软删除标记';

