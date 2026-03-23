-- Skills 市场表
CREATE TABLE skills (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    summary VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    github_url VARCHAR(500) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL
);

CREATE INDEX idx_skills_user_id ON skills(user_id);
CREATE INDEX idx_skills_create_time ON skills(create_time);

COMMENT ON TABLE skills IS 'Skills 市场表';
COMMENT ON COLUMN skills.id IS '主键ID (UUID)';
COMMENT ON COLUMN skills.user_id IS '创建用户ID';
COMMENT ON COLUMN skills.name IS 'Skill 名称';
COMMENT ON COLUMN skills.summary IS 'Skill 简介';
COMMENT ON COLUMN skills.description IS 'Skill 详细描述';
COMMENT ON COLUMN skills.github_url IS 'GitHub 链接';
COMMENT ON COLUMN skills.create_time IS '创建时间';
COMMENT ON COLUMN skills.update_time IS '更新时间';
COMMENT ON COLUMN skills.deleted_at IS '软删除标记';
