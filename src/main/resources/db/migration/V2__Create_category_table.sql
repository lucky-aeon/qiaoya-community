-- 创建分类表
CREATE TABLE categories (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    parent_id VARCHAR(36),
    type VARCHAR(20) NOT NULL DEFAULT 'ARTICLE',
    level INTEGER NOT NULL DEFAULT 1,
    sort_order INTEGER NOT NULL DEFAULT 0,
    description TEXT,
    icon VARCHAR(200),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 创建索引
CREATE INDEX idx_categories_parent_id ON categories(parent_id);
CREATE INDEX idx_categories_type ON categories(type);
CREATE INDEX idx_categories_level ON categories(level);
CREATE INDEX idx_categories_sort_order ON categories(sort_order);
CREATE INDEX idx_categories_create_time ON categories(create_time);

-- 添加表注释
COMMENT ON TABLE categories IS '分类表';
COMMENT ON COLUMN categories.id IS '分类ID (UUID格式)';
COMMENT ON COLUMN categories.name IS '分类名称';
COMMENT ON COLUMN categories.parent_id IS '父分类ID';
COMMENT ON COLUMN categories.type IS '分类类型：ARTICLE-文章，QA-问答';
COMMENT ON COLUMN categories.level IS '分类层级，从1开始';
COMMENT ON COLUMN categories.sort_order IS '排序权重';
COMMENT ON COLUMN categories.description IS '分类描述';
COMMENT ON COLUMN categories.icon IS '分类图标URL';
COMMENT ON COLUMN categories.is_active IS '是否启用';
COMMENT ON COLUMN categories.create_time IS '创建时间';
COMMENT ON COLUMN categories.update_time IS '更新时间';
COMMENT ON COLUMN categories.deleted IS '是否删除';