-- 创建点赞表
CREATE TABLE likes (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    target_id VARCHAR(36) NOT NULL,
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,

    -- 唯一索引：防止重复点赞（考虑软删除）
    CONSTRAINT unique_user_target UNIQUE (user_id, target_type, target_id, deleted_at)
);

-- 查询优化索引：根据目标查询点赞
CREATE INDEX idx_target ON likes(target_type, target_id, deleted_at);

-- 查询优化索引：根据用户查询点赞历史
CREATE INDEX idx_user ON likes(user_id, deleted_at);

-- 表注释
COMMENT ON TABLE likes IS '点赞表，支持对课程、文章、章节、评论的点赞';
COMMENT ON COLUMN likes.id IS '主键ID';
COMMENT ON COLUMN likes.user_id IS '点赞用户ID';
COMMENT ON COLUMN likes.target_type IS '目标类型：COURSE/POST/CHAPTER/COMMENT';
COMMENT ON COLUMN likes.target_id IS '目标对象ID';
COMMENT ON COLUMN likes.create_time IS '创建时间';
COMMENT ON COLUMN likes.update_time IS '更新时间';
COMMENT ON COLUMN likes.deleted_at IS '删除时间（软删除）';