-- 用户-房间维度的 Last Seen 记录（用于未读消息统计与清零）
CREATE TABLE IF NOT EXISTS user_chat_room_last_seen (
  id            VARCHAR(36) PRIMARY KEY,
  user_id       VARCHAR(36) NOT NULL,
  room_id       VARCHAR(36) NOT NULL,
  last_seen_at  TIMESTAMP   NOT NULL,
  create_time   TIMESTAMP   NOT NULL DEFAULT NOW(),
  update_time   TIMESTAMP   NOT NULL DEFAULT NOW(),
  deleted_at    TIMESTAMP   NULL
);

-- 唯一索引（未删除语义）
CREATE UNIQUE INDEX IF NOT EXISTS uk_user_room_last_seen
  ON user_chat_room_last_seen (user_id, room_id)
  WHERE deleted_at IS NULL;

