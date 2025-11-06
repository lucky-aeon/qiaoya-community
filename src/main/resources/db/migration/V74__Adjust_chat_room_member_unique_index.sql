-- 调整 chat_room_members 的唯一约束，仅对未删除成员生效
ALTER TABLE chat_room_members DROP CONSTRAINT IF EXISTS uk_room_user;

CREATE UNIQUE INDEX IF NOT EXISTS uk_chat_room_members_room_user_active
    ON chat_room_members (room_id, user_id)
    WHERE deleted_at IS NULL;
-- 第一句移除原有的表级唯一约束 uk_room_user。
-- 第二句新建部分唯一索引，条件是 deleted_at IS NULL，只会约束仍在房间里的成员。