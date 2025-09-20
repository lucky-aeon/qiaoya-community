-- 通知记录表
CREATE TABLE notifications (
    id VARCHAR(36) PRIMARY KEY,
    recipient_id VARCHAR(36) NOT NULL,
    type VARCHAR(50) NOT NULL,
    channel_type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 添加注释
COMMENT ON TABLE notifications IS '通知记录表';
COMMENT ON COLUMN notifications.id IS '通知ID';
COMMENT ON COLUMN notifications.recipient_id IS '接收者ID';
COMMENT ON COLUMN notifications.type IS '通知类型';
COMMENT ON COLUMN notifications.channel_type IS '渠道类型(IN_APP/EMAIL/SMS)';
COMMENT ON COLUMN notifications.title IS '通知标题';
COMMENT ON COLUMN notifications.content IS '通知内容';
COMMENT ON COLUMN notifications.status IS '通知状态(PENDING/SENT/READ/FAILED)';
COMMENT ON COLUMN notifications.create_time IS '创建时间';
COMMENT ON COLUMN notifications.update_time IS '更新时间';
COMMENT ON COLUMN notifications.deleted IS '是否删除';

-- 创建索引
CREATE INDEX idx_notifications_recipient_channel ON notifications(recipient_id, channel_type);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_create_time ON notifications(create_time);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_deleted ON notifications(deleted);