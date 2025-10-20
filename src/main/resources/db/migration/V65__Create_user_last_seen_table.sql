-- 用户频道 Last Seen 表（用于导航小红点-列表级未读）
CREATE TABLE user_last_seen (
    id           VARCHAR(36) PRIMARY KEY,
    user_id      VARCHAR(36)   NOT NULL,
    channel      VARCHAR(20)   NOT NULL, -- POSTS | QUESTIONS
    last_seen_at TIMESTAMP     NULL,
    create_time  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at   TIMESTAMP     NULL
);

-- 唯一约束：同一用户每个频道仅保留一条活跃记录（软删后可重建）
CREATE UNIQUE INDEX IF NOT EXISTS uk_user_last_seen_user_channel
  ON user_last_seen (user_id, channel)
  WHERE deleted_at IS NULL;

-- 常用检索索引
CREATE INDEX IF NOT EXISTS idx_user_last_seen_user
  ON user_last_seen (user_id)
  WHERE deleted_at IS NULL;

-- 注释
COMMENT ON TABLE user_last_seen IS '用户频道 Last Seen 表（导航小红点）';
COMMENT ON COLUMN user_last_seen.id IS '主键ID (UUID)';
COMMENT ON COLUMN user_last_seen.user_id IS '用户ID';
COMMENT ON COLUMN user_last_seen.channel IS '频道：POSTS/QUESTIONS';
COMMENT ON COLUMN user_last_seen.last_seen_at IS '上次访问列表时间';
COMMENT ON COLUMN user_last_seen.create_time IS '创建时间';
COMMENT ON COLUMN user_last_seen.update_time IS '更新时间';
COMMENT ON COLUMN user_last_seen.deleted_at IS '软删除时间，NULL 表示未删除';

