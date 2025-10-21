-- OAuth2 客户端表
-- 存储第三方应用的客户端信息，用于 OAuth2 授权服务器

CREATE TABLE IF NOT EXISTS oauth2_clients (
    -- 主键
    id VARCHAR(36) PRIMARY KEY,

    -- 客户端基本信息
    client_id VARCHAR(100) NOT NULL UNIQUE,
    client_secret_enc VARCHAR(500) NOT NULL,
    client_name VARCHAR(200) NOT NULL,

    -- OAuth2 配置
    redirect_uris TEXT NOT NULL,
    grant_types TEXT NOT NULL,
    scopes TEXT NOT NULL,
    client_authentication_methods TEXT NOT NULL,

    -- Token 配置
    access_token_validity_seconds INTEGER NOT NULL DEFAULT 3600,
    refresh_token_validity_seconds INTEGER NOT NULL DEFAULT 2592000,

    -- PKCE 和授权配置
    require_proof_key BOOLEAN NOT NULL DEFAULT FALSE,
    require_authorization_consent BOOLEAN NOT NULL DEFAULT TRUE,

    -- 状态管理
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    -- 审计字段
    created_by VARCHAR(36),

    -- BaseEntity 公共字段
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- 索引
CREATE INDEX idx_oauth2_clients_client_id ON oauth2_clients (client_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_oauth2_clients_status ON oauth2_clients (status) WHERE deleted_at IS NULL;
CREATE INDEX idx_oauth2_clients_created_by ON oauth2_clients (created_by) WHERE deleted_at IS NULL;
CREATE INDEX idx_oauth2_clients_create_time ON oauth2_clients (create_time) WHERE deleted_at IS NULL;

-- 表注释
COMMENT ON TABLE oauth2_clients IS 'OAuth2客户端应用表 - 存储第三方应用的OAuth2客户端配置信息';

-- 列注释
COMMENT ON COLUMN oauth2_clients.id IS '主键UUID';
COMMENT ON COLUMN oauth2_clients.client_id IS '客户端ID（对外暴露，用于OAuth2授权流程）';
COMMENT ON COLUMN oauth2_clients.client_secret_enc IS '客户端密钥（BCrypt加密存储，仅在创建/重置时返回明文）';
COMMENT ON COLUMN oauth2_clients.client_name IS '客户端名称（应用名称，显示在授权同意页面）';
COMMENT ON COLUMN oauth2_clients.redirect_uris IS '重定向URI列表（JSON数组，OAuth2回调地址白名单）';
COMMENT ON COLUMN oauth2_clients.grant_types IS '授权类型列表（JSON数组，支持的OAuth2授权模式）';
COMMENT ON COLUMN oauth2_clients.scopes IS '允许的Scope列表（JSON数组，客户端可请求的权限范围）';
COMMENT ON COLUMN oauth2_clients.client_authentication_methods IS '客户端认证方式（JSON数组，Token请求时的认证方式）';
COMMENT ON COLUMN oauth2_clients.access_token_validity_seconds IS 'Access Token有效期（秒）';
COMMENT ON COLUMN oauth2_clients.refresh_token_validity_seconds IS 'Refresh Token有效期（秒）';
COMMENT ON COLUMN oauth2_clients.require_proof_key IS '是否强制要求PKCE（Proof Key for Code Exchange，增强安全性）';
COMMENT ON COLUMN oauth2_clients.require_authorization_consent IS '是否需要用户授权同意（首次授权时显示同意页面）';
COMMENT ON COLUMN oauth2_clients.status IS '客户端状态（ACTIVE-正常/SUSPENDED-暂停/REVOKED-撤销）';
COMMENT ON COLUMN oauth2_clients.created_by IS '创建人用户ID';
COMMENT ON COLUMN oauth2_clients.create_time IS '创建时间';
COMMENT ON COLUMN oauth2_clients.update_time IS '更新时间';
COMMENT ON COLUMN oauth2_clients.deleted_at IS '软删除时间戳（NULL表示未删除）';
