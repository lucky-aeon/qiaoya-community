-- 通用表情回复表（PostgreSQL）
-- 说明：记录用户在不同业务对象上的表情操作；用于统计与"在用"校验

CREATE TABLE IF NOT EXISTS reactions (
    id            VARCHAR(36) PRIMARY KEY,
    business_type VARCHAR(50)  NOT NULL, -- 与业务枚举对齐：POST/COURSE/CHAPTER 等
    business_id   VARCHAR(36)  NOT NULL,
    user_id       VARCHAR(36)  NOT NULL,
    reaction_type VARCHAR(50)  NOT NULL, -- 引用 expression_types.code（不加外键，避免 code 变更牵连）
    create_time   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at    TIMESTAMP    NULL
);

-- 同一用户对同一业务同一种表情唯一（未删除）
CREATE UNIQUE INDEX IF NOT EXISTS uq_user_business_reaction_not_deleted
  ON reactions(user_id, business_type, business_id, reaction_type)
  WHERE deleted_at IS NULL;

-- 常用查询索引
CREATE INDEX IF NOT EXISTS idx_reaction_business ON reactions(business_type, business_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_reaction_type     ON reactions(reaction_type) WHERE deleted_at IS NULL;

-- 注释
COMMENT ON TABLE reactions IS '通用表情回复表（统计、在用校验）';
COMMENT ON COLUMN reactions.business_type IS '业务类型：POST/COURSE/CHAPTER 等';
COMMENT ON COLUMN reactions.reaction_type IS '表情代码（对应 expression_types.code）';

