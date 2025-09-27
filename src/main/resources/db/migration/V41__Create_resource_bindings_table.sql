-- 创建资源绑定关系表（资源 ↔ 业务对象）
-- 逻辑删除采用 deleted_at 时间戳，NULL 表示未删除

CREATE TABLE IF NOT EXISTS resource_bindings (
    id           VARCHAR(36) PRIMARY KEY,
    resource_id  VARCHAR(36) NOT NULL,
    target_type  VARCHAR(32) NOT NULL,
    target_id    VARCHAR(36) NOT NULL,
    create_time  TIMESTAMP NOT NULL DEFAULT NOW(),
    update_time  TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at   TIMESTAMP NULL
);

-- 未删除记录唯一约束，避免重复绑定
CREATE UNIQUE INDEX IF NOT EXISTS uq_resource_bindings_unique
    ON resource_bindings(resource_id, target_type, target_id)
    WHERE deleted_at IS NULL;

-- 常用查询索引（未删除）
CREATE INDEX IF NOT EXISTS idx_resource_bindings_resource
    ON resource_bindings(resource_id)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_resource_bindings_target
    ON resource_bindings(target_type, target_id)
    WHERE deleted_at IS NULL;

