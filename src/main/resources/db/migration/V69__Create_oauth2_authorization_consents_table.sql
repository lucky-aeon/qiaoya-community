-- OAuth2 用户授权同意表
-- 同一 (client_id, principal_name) 仅一条有效记录（未删除）

CREATE TABLE IF NOT EXISTS oauth2_authorization_consents (
    id                 VARCHAR(36) PRIMARY KEY,

    client_id          VARCHAR(100) NOT NULL,
    principal_name     VARCHAR(255) NOT NULL,
    authorities        TEXT         NOT NULL,
    consent_time       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- BaseEntity 公共字段
    create_time        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at         TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_oauth2_consent_unique
    ON oauth2_authorization_consents (client_id, principal_name)
    WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_oauth2_consent_client ON oauth2_authorization_consents (client_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_oauth2_consent_principal ON oauth2_authorization_consents (principal_name) WHERE deleted_at IS NULL;

-- 表注释
COMMENT ON TABLE oauth2_authorization_consents IS 'OAuth2用户授权同意表 - 记录用户对客户端的授权同意';

-- 列注释
COMMENT ON COLUMN oauth2_authorization_consents.id IS '主键UUID';
COMMENT ON COLUMN oauth2_authorization_consents.client_id IS '客户端ID';
COMMENT ON COLUMN oauth2_authorization_consents.principal_name IS '用户标识（用户ID）';
COMMENT ON COLUMN oauth2_authorization_consents.authorities IS '授权的权限列表（逗号分隔的scope）';
COMMENT ON COLUMN oauth2_authorization_consents.consent_time IS '授权同意时间';
COMMENT ON COLUMN oauth2_authorization_consents.create_time IS '创建时间';
COMMENT ON COLUMN oauth2_authorization_consents.update_time IS '更新时间';
COMMENT ON COLUMN oauth2_authorization_consents.deleted_at IS '软删除时间戳（NULL表示未删除）';
