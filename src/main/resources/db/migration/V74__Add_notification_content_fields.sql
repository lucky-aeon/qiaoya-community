-- 通知业务内容字段
ALTER TABLE notifications
    ADD COLUMN IF NOT EXISTS content_type VARCHAR(50),
    ADD COLUMN IF NOT EXISTS content_id VARCHAR(36);

COMMENT ON COLUMN notifications.content_type IS '业务内容类型';
COMMENT ON COLUMN notifications.content_id IS '业务内容ID';
