-- 用户标签系统三张表：标签定义、标签绑定、用户授予（重命名至V55以避免版本冲突）

-- 1) 标签定义
CREATE TABLE IF NOT EXISTS tag_definitions (
    id VARCHAR(36) PRIMARY KEY,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(30) NOT NULL,
    icon_url VARCHAR(500),
    description TEXT,
    public_visible BOOLEAN NOT NULL DEFAULT TRUE,
    unique_per_user BOOLEAN NOT NULL DEFAULT TRUE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_tag_code ON tag_definitions(code);
CREATE INDEX IF NOT EXISTS idx_tag_enabled ON tag_definitions(enabled);

COMMENT ON TABLE tag_definitions IS '标签定义：描述标签是什么（展示、分类、可见性）';
COMMENT ON COLUMN tag_definitions.code IS '标签编码（业务主键，唯一、稳定）';
COMMENT ON COLUMN tag_definitions.category IS '标签类别（如 ACHIEVEMENT/IDENTITY/EVENT 等）';
COMMENT ON COLUMN tag_definitions.public_visible IS '是否对用户可见';
COMMENT ON COLUMN tag_definitions.unique_per_user IS '同一用户是否仅允许一张';

-- 2) 标签绑定（标签与业务对象的关联）
CREATE TABLE IF NOT EXISTS tag_scopes (
    id VARCHAR(36) PRIMARY KEY,
    tag_id VARCHAR(36) NOT NULL,
    target_type VARCHAR(30) NOT NULL,
    target_id VARCHAR(36) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_tag_scope
  ON tag_scopes(tag_id, target_type, target_id)
  WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_scope_target
  ON tag_scopes(target_type, target_id)
  WHERE deleted_at IS NULL;

COMMENT ON TABLE tag_scopes IS '标签绑定：一个标签与哪些业务对象（课程/活动等）关联';
COMMENT ON COLUMN tag_scopes.target_type IS '绑定对象类型：如 COURSE/CHAPTER/POST/ACTIVITY 等';

-- 3) 用户标签授予
CREATE TABLE IF NOT EXISTS user_tag_assignments (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    tag_id VARCHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL,
    issued_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    expire_at TIMESTAMP,
    source_type VARCHAR(30),
    source_id VARCHAR(36),
    meta JSONB,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_tag
  ON user_tag_assignments(user_id, tag_id)
  WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_user_tags ON user_tag_assignments(user_id);
CREATE INDEX IF NOT EXISTS idx_tag_users ON user_tag_assignments(tag_id);

COMMENT ON TABLE user_tag_assignments IS '用户标签授予：记录谁在何时以何种来源获得/撤销标签';
COMMENT ON COLUMN user_tag_assignments.status IS 'ISSUED/REVOKED/EXPIRED';
