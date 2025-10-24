-- Chat module tables (PostgreSQL)
-- 说明：遵循“非高频字段不建索引”；软删字段为 deleted_at（NULL 表示未删除）

-- chat_rooms
CREATE TABLE IF NOT EXISTS chat_rooms (
  id                  VARCHAR(36) PRIMARY KEY,
  name                VARCHAR(128) NOT NULL,
  description         TEXT         NULL,
  subscription_plan_id VARCHAR(36) NOT NULL,
  creator_id          VARCHAR(36) NOT NULL,
  create_time         TIMESTAMP    NOT NULL DEFAULT NOW(),
  update_time         TIMESTAMP    NOT NULL DEFAULT NOW(),
  deleted_at          TIMESTAMP    NULL
);
CREATE INDEX IF NOT EXISTS idx_chat_rooms_creator ON chat_rooms (creator_id) WHERE deleted_at IS NULL;

-- chat_room_members
CREATE TABLE IF NOT EXISTS chat_room_members (
  id          VARCHAR(36) PRIMARY KEY,
  room_id     VARCHAR(36) NOT NULL,
  user_id     VARCHAR(36) NOT NULL,
  role        VARCHAR(32) NOT NULL,
  create_time TIMESTAMP   NOT NULL DEFAULT NOW(),
  update_time TIMESTAMP   NOT NULL DEFAULT NOW(),
  deleted_at  TIMESTAMP   NULL,
  CONSTRAINT uk_room_user UNIQUE (room_id, user_id)
);
CREATE INDEX IF NOT EXISTS idx_chat_room_members_room ON chat_room_members (room_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_chat_room_members_user ON chat_room_members (user_id) WHERE deleted_at IS NULL;

-- chat_messages
CREATE TABLE IF NOT EXISTS chat_messages (
  id                 VARCHAR(36) PRIMARY KEY,
  room_id            VARCHAR(36) NOT NULL,
  sender_id          VARCHAR(36) NOT NULL,
  content            TEXT        NOT NULL,
  quoted_message_id  VARCHAR(36) NULL,
  mentioned_user_ids JSONB       NULL,
  create_time        TIMESTAMP   NOT NULL DEFAULT NOW(),
  update_time        TIMESTAMP   NOT NULL DEFAULT NOW(),
  deleted_at         TIMESTAMP   NULL
);
CREATE INDEX IF NOT EXISTS idx_chat_messages_room_time ON chat_messages (room_id, create_time DESC) WHERE deleted_at IS NULL;
-- 如引用查询不高频，可按需启用：
-- CREATE INDEX IF NOT EXISTS idx_chat_messages_quoted ON chat_messages (quoted_message_id) WHERE deleted_at IS NULL;
