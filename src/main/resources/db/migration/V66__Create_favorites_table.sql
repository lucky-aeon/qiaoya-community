-- 创建收藏表
CREATE TABLE favorites (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    target_type VARCHAR(40) NOT NULL,
    target_id VARCHAR(36) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 唯一约束：防止重复收藏
CREATE UNIQUE INDEX uk_favorites_unique ON favorites (user_id, target_type, target_id);

-- 查询优化：按目标统计/查列表
CREATE INDEX idx_favorites_target ON favorites (target_type, target_id);
CREATE INDEX idx_favorites_user ON favorites (user_id, create_time DESC);

-- 表注释
COMMENT ON TABLE favorites IS '收藏表，支持文章/章节/评论/题目';
COMMENT ON COLUMN favorites.id IS '主键ID';
COMMENT ON COLUMN favorites.user_id IS '收藏用户ID';
COMMENT ON COLUMN favorites.target_type IS '目标类型：POST/CHAPTER/COMMENT/INTERVIEW_QUESTION';
COMMENT ON COLUMN favorites.target_id IS '目标对象ID';
COMMENT ON COLUMN favorites.create_time IS '创建时间';
COMMENT ON COLUMN favorites.update_time IS '更新时间';
