-- 表情类型配置表（PostgreSQL）
-- 说明：用于管理员维护 Markdown 可用的表情字典

CREATE TABLE IF NOT EXISTS expression_types (
    id          VARCHAR(36) PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL,
    name        VARCHAR(100) NOT NULL,
    image_url   VARCHAR(255) NOT NULL,
    sort_order  INTEGER      NOT NULL DEFAULT 0,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at  TIMESTAMP    NULL
);

-- 软删下保持 code 唯一
CREATE UNIQUE INDEX IF NOT EXISTS uq_expression_code_not_deleted
  ON expression_types (code)
  WHERE deleted_at IS NULL;

-- 高频筛选与排序
CREATE INDEX IF NOT EXISTS idx_expression_types_active ON expression_types(is_active) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_expression_types_sort   ON expression_types(sort_order) WHERE deleted_at IS NULL;

-- 注释
COMMENT ON TABLE expression_types IS '表情类型配置表（Markdown 映射：:code: -> image_url）';
COMMENT ON COLUMN expression_types.code IS '表情代码（Markdown 使用，不含冒号）';
COMMENT ON COLUMN expression_types.image_url IS '图片URL（或相对路径）';
COMMENT ON COLUMN expression_types.sort_order IS '显示排序，越小越靠前';
COMMENT ON COLUMN expression_types.is_active IS '是否启用';

