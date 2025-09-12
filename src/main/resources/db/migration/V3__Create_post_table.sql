-- 创建文章表
CREATE TABLE posts (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    summary VARCHAR(500),
    cover_image VARCHAR(500),
    author_id VARCHAR(36) NOT NULL,
    category_id VARCHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    like_count INTEGER NOT NULL DEFAULT 0,
    view_count INTEGER NOT NULL DEFAULT 0,
    comment_count INTEGER NOT NULL DEFAULT 0,
    is_top BOOLEAN NOT NULL DEFAULT FALSE,
    publish_time TIMESTAMP,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 创建索引
CREATE INDEX idx_posts_author_id ON posts(author_id);
CREATE INDEX idx_posts_category_id ON posts(category_id);
CREATE INDEX idx_posts_status ON posts(status);
CREATE INDEX idx_posts_publish_time ON posts(publish_time);
CREATE INDEX idx_posts_is_top ON posts(is_top);
CREATE INDEX idx_posts_create_time ON posts(create_time);

-- 添加表注释
COMMENT ON TABLE posts IS '文章表';
COMMENT ON COLUMN posts.id IS '文章ID (UUID格式)';
COMMENT ON COLUMN posts.title IS '文章标题';
COMMENT ON COLUMN posts.content IS '文章内容';
COMMENT ON COLUMN posts.summary IS '文章概要';
COMMENT ON COLUMN posts.cover_image IS '封面图片URL';
COMMENT ON COLUMN posts.author_id IS '作者ID';
COMMENT ON COLUMN posts.category_id IS '分类ID';
COMMENT ON COLUMN posts.status IS '状态：DRAFT-草稿，PUBLISHED-已发布';
COMMENT ON COLUMN posts.like_count IS '点赞数';
COMMENT ON COLUMN posts.view_count IS '浏览数';
COMMENT ON COLUMN posts.comment_count IS '评论数';
COMMENT ON COLUMN posts.is_top IS '是否置顶';
COMMENT ON COLUMN posts.publish_time IS '发布时间';
COMMENT ON COLUMN posts.create_time IS '创建时间';
COMMENT ON COLUMN posts.update_time IS '更新时间';
COMMENT ON COLUMN posts.deleted IS '是否删除';