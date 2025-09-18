-- 关注关系表
CREATE TABLE follows (
    id VARCHAR(36) PRIMARY KEY,
    follower_id VARCHAR(36) NOT NULL,
    target_id VARCHAR(36) NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    follow_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    unfollow_time TIMESTAMP NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 索引设计
-- 防止重复关注的唯一索引
CREATE UNIQUE INDEX uk_follows_unique ON follows (follower_id, target_id, target_type, deleted);

-- 查询用户关注列表
CREATE INDEX idx_follows_follower ON follows (follower_id, status, deleted);

-- 查询目标被关注列表
CREATE INDEX idx_follows_target ON follows (target_id, target_type, status, deleted);

-- 按关注类型统计
CREATE INDEX idx_follows_target_type ON follows (target_type, status, deleted);

-- 按关注时间排序
CREATE INDEX idx_follows_time ON follows (follow_time);

-- 为follower_id添加外键约束（引用users表）
ALTER TABLE follows ADD CONSTRAINT fk_follows_follower 
    FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE;

-- 添加约束
ALTER TABLE follows ADD CONSTRAINT chk_follows_target_type 
    CHECK (target_type IN ('USER', 'COURSE', 'CHAPTER', 'POST'));

ALTER TABLE follows ADD CONSTRAINT chk_follows_status 
    CHECK (status IN ('ACTIVE', 'CANCELLED'));

-- 添加表注释
COMMENT ON TABLE follows IS '关注关系表';
COMMENT ON COLUMN follows.id IS '关注记录ID (UUID格式)';
COMMENT ON COLUMN follows.follower_id IS '关注者用户ID';
COMMENT ON COLUMN follows.target_id IS '被关注目标ID';
COMMENT ON COLUMN follows.target_type IS '关注目标类型：USER/COURSE/CHAPTER/POST';
COMMENT ON COLUMN follows.status IS '关注状态：ACTIVE/CANCELLED';
COMMENT ON COLUMN follows.follow_time IS '关注时间';
COMMENT ON COLUMN follows.unfollow_time IS '取消关注时间';
COMMENT ON COLUMN follows.create_time IS '创建时间';
COMMENT ON COLUMN follows.update_time IS '更新时间';
COMMENT ON COLUMN follows.deleted IS '是否删除';