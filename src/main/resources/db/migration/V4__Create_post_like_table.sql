-- 创建文章点赞表
CREATE TABLE post_likes (
    id VARCHAR(36) PRIMARY KEY,
    post_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 创建唯一约束：防止重复点赞
CREATE UNIQUE INDEX idx_post_likes_unique ON post_likes(post_id, user_id) WHERE deleted = FALSE;

-- 创建索引
CREATE INDEX idx_post_likes_post_id ON post_likes(post_id);
CREATE INDEX idx_post_likes_user_id ON post_likes(user_id);
CREATE INDEX idx_post_likes_create_time ON post_likes(create_time);

-- 添加表注释
COMMENT ON TABLE post_likes IS '文章点赞表';
COMMENT ON COLUMN post_likes.id IS '点赞记录ID (UUID格式)';
COMMENT ON COLUMN post_likes.post_id IS '文章ID';
COMMENT ON COLUMN post_likes.user_id IS '点赞用户ID';
COMMENT ON COLUMN post_likes.create_time IS '创建时间';
COMMENT ON COLUMN post_likes.update_time IS '更新时间';
COMMENT ON COLUMN post_likes.deleted IS '是否删除';