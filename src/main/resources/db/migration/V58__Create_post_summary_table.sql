-- 文章评论AI摘要表（仅保存摘要内容）
-- 数据库：PostgreSQL

CREATE TABLE IF NOT EXISTS post_summary (
    id VARCHAR(36) PRIMARY KEY,
    post_id VARCHAR(36) NOT NULL UNIQUE,
    summary TEXT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL
);

COMMENT ON TABLE post_summary IS '文章评论AI摘要（仅保存摘要内容）';
COMMENT ON COLUMN post_summary.post_id IS '文章ID（唯一）';

