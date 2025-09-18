-- 关注关系表
CREATE TABLE follows (
    id VARCHAR(36) PRIMARY KEY,
    follower_id VARCHAR(36) NOT NULL COMMENT '关注者用户ID',
    target_id VARCHAR(36) NOT NULL COMMENT '被关注目标ID',
    target_type VARCHAR(20) NOT NULL COMMENT '关注目标类型：USER/COURSE/CHAPTER/POST',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '关注状态：ACTIVE/CANCELLED',
    follow_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    unfollow_time TIMESTAMP NULL COMMENT '取消关注时间',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
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