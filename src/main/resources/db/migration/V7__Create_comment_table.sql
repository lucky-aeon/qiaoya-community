-- 创建评论表
CREATE TABLE comments (
    id VARCHAR(36) PRIMARY KEY,
    parent_comment_id VARCHAR(36),
    root_comment_id VARCHAR(36),
    content TEXT NOT NULL,
    comment_user_id VARCHAR(36) NOT NULL,
    reply_user_id VARCHAR(36),
    business_id VARCHAR(36) NOT NULL,
    business_type VARCHAR(20) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 创建索引
CREATE INDEX idx_comments_parent_comment_id ON comments(parent_comment_id);
CREATE INDEX idx_comments_root_comment_id ON comments(root_comment_id);
CREATE INDEX idx_comments_comment_user_id ON comments(comment_user_id);
CREATE INDEX idx_comments_reply_user_id ON comments(reply_user_id);
CREATE INDEX idx_comments_business_id ON comments(business_id);
CREATE INDEX idx_comments_business_type ON comments(business_type);
CREATE INDEX idx_comments_business_id_type ON comments(business_id, business_type);
CREATE INDEX idx_comments_create_time ON comments(create_time);

-- 添加表注释
COMMENT ON TABLE comments IS '评论表';
COMMENT ON COLUMN comments.id IS '评论ID (UUID格式)';
COMMENT ON COLUMN comments.parent_comment_id IS '父评论ID（直接回复的评论）';
COMMENT ON COLUMN comments.root_comment_id IS '根评论ID（楼主评论）';
COMMENT ON COLUMN comments.content IS '评论内容';
COMMENT ON COLUMN comments.comment_user_id IS '评论用户ID';
COMMENT ON COLUMN comments.reply_user_id IS '被回复用户ID';
COMMENT ON COLUMN comments.business_id IS '业务ID（文章ID、课程ID等）';
COMMENT ON COLUMN comments.business_type IS '业务类型：POST-文章，COURSE-课程';
COMMENT ON COLUMN comments.create_time IS '创建时间';
COMMENT ON COLUMN comments.update_time IS '更新时间';
COMMENT ON COLUMN comments.deleted IS '是否删除';