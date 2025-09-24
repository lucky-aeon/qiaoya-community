-- 问答采纳与帖子状态字段

-- 为文章添加问答解决状态与解决时间
ALTER TABLE posts
    ADD COLUMN resolve_status VARCHAR(32),
    ADD COLUMN solved_at TIMESTAMP;

COMMENT ON COLUMN posts.resolve_status IS '问答解决状态：UNSOLVED/SOLVED，仅问答类型使用';
COMMENT ON COLUMN posts.solved_at IS '首次被采纳的时间';

-- 可按需增加索引以便状态筛选
CREATE INDEX IF NOT EXISTS idx_posts_resolve_status ON posts(resolve_status);

-- 采纳关系表：支持同一帖子多条评论被采纳
CREATE TABLE post_accepted_comments (
    id VARCHAR(36) PRIMARY KEY,
    post_id VARCHAR(36) NOT NULL,
    comment_id VARCHAR(36) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE post_accepted_comments IS '帖子被采纳评论关系表（多采纳）';
COMMENT ON COLUMN post_accepted_comments.post_id IS '帖子ID';
COMMENT ON COLUMN post_accepted_comments.comment_id IS '被采纳的评论ID';

-- 幂等与快速查询
CREATE UNIQUE INDEX uniq_post_comment ON post_accepted_comments(post_id, comment_id) WHERE deleted = FALSE;
CREATE INDEX idx_pac_post_id ON post_accepted_comments(post_id) WHERE deleted = FALSE;

