-- OAuth2 授权记录表（授权码、Access Token、Refresh Token、ID Token、状态与扩展）
-- PostgreSQL 语法，软删除字段为 deleted_at TIMESTAMP

CREATE TABLE IF NOT EXISTS oauth2_authorizations (
    id                               VARCHAR(36) PRIMARY KEY,

    -- 关联信息
    client_id                        VARCHAR(100) NOT NULL,
    principal_name                   VARCHAR(255) NOT NULL,

    -- 授权类型
    authorization_grant_type         VARCHAR(50)  NOT NULL,

    -- 授权码
    authorization_code_value         TEXT,
    authorization_code_issued_at     TIMESTAMP,
    authorization_code_expires_at    TIMESTAMP,
    authorization_code_metadata      TEXT,

    -- Access Token
    access_token_value               TEXT,
    access_token_issued_at           TIMESTAMP,
    access_token_expires_at          TIMESTAMP,
    access_token_metadata            TEXT,
    access_token_type                VARCHAR(50),
    access_token_scopes              TEXT,

    -- Refresh Token
    refresh_token_value              TEXT,
    refresh_token_issued_at          TIMESTAMP,
    refresh_token_expires_at         TIMESTAMP,
    refresh_token_metadata           TEXT,

    -- OIDC ID Token
    oidc_id_token_value              TEXT,
    oidc_id_token_issued_at          TIMESTAMP,
    oidc_id_token_expires_at         TIMESTAMP,
    oidc_id_token_metadata           TEXT,

    -- 其他
    state                            VARCHAR(500),
    attributes                       TEXT,

    -- BaseEntity 公共字段
    create_time                      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time                      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at                       TIMESTAMP
);

-- 索引（仅对未删除行）
CREATE INDEX IF NOT EXISTS idx_oauth2_auth_client_id ON oauth2_authorizations (client_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_oauth2_auth_principal ON oauth2_authorizations (principal_name) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_oauth2_auth_code ON oauth2_authorizations (authorization_code_value) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_oauth2_auth_access_token ON oauth2_authorizations (access_token_value) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_oauth2_auth_refresh_token ON oauth2_authorizations (refresh_token_value) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_oauth2_auth_expires ON oauth2_authorizations (access_token_expires_at) WHERE deleted_at IS NULL;

-- 表注释
COMMENT ON TABLE oauth2_authorizations IS 'OAuth2授权记录表 - 存储授权码、Access Token、Refresh Token等信息';

-- 列注释
COMMENT ON COLUMN oauth2_authorizations.id IS '主键UUID';
COMMENT ON COLUMN oauth2_authorizations.client_id IS '客户端ID（对应 oauth2_clients.client_id）';
COMMENT ON COLUMN oauth2_authorizations.principal_name IS '用户标识（用户ID或用户名）';
COMMENT ON COLUMN oauth2_authorizations.authorization_grant_type IS '授权类型（authorization_code、refresh_token、client_credentials）';
COMMENT ON COLUMN oauth2_authorizations.authorization_code_value IS '授权码值';
COMMENT ON COLUMN oauth2_authorizations.authorization_code_issued_at IS '授权码签发时间';
COMMENT ON COLUMN oauth2_authorizations.authorization_code_expires_at IS '授权码过期时间';
COMMENT ON COLUMN oauth2_authorizations.access_token_value IS 'Access Token值（JWT格式）';
COMMENT ON COLUMN oauth2_authorizations.access_token_issued_at IS 'Access Token签发时间';
COMMENT ON COLUMN oauth2_authorizations.access_token_expires_at IS 'Access Token过期时间';
COMMENT ON COLUMN oauth2_authorizations.access_token_type IS 'Access Token类型（Bearer）';
COMMENT ON COLUMN oauth2_authorizations.access_token_scopes IS 'Access Token权限范围（逗号分隔）';
COMMENT ON COLUMN oauth2_authorizations.refresh_token_value IS 'Refresh Token值';
COMMENT ON COLUMN oauth2_authorizations.refresh_token_issued_at IS 'Refresh Token签发时间';
COMMENT ON COLUMN oauth2_authorizations.refresh_token_expires_at IS 'Refresh Token过期时间';
COMMENT ON COLUMN oauth2_authorizations.state IS 'State参数（CSRF防护）';
COMMENT ON COLUMN oauth2_authorizations.create_time IS '创建时间';
COMMENT ON COLUMN oauth2_authorizations.update_time IS '更新时间';
COMMENT ON COLUMN oauth2_authorizations.deleted_at IS '软删除时间戳（NULL表示未删除）';
