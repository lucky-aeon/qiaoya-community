-- 技能市场表
CREATE TABLE skills (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    name VARCHAR(200) NOT NULL,
    summary VARCHAR(500) NOT NULL,
    description TEXT NOT NULL,
    github_url VARCHAR(500) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_skills_user_id ON skills (user_id);
CREATE INDEX idx_skills_create_time ON skills (create_time DESC);

COMMENT ON TABLE skills IS '技能市场表';
COMMENT ON COLUMN skills.id IS '主键ID';
COMMENT ON COLUMN skills.user_id IS '作者用户ID';
COMMENT ON COLUMN skills.name IS '技能名称';
COMMENT ON COLUMN skills.summary IS '技能摘要';
COMMENT ON COLUMN skills.description IS '技能详细描述';
COMMENT ON COLUMN skills.github_url IS 'GitHub 链接';
COMMENT ON COLUMN skills.create_time IS '创建时间';
COMMENT ON COLUMN skills.update_time IS '更新时间';
COMMENT ON COLUMN skills.deleted IS '逻辑删除标记';
