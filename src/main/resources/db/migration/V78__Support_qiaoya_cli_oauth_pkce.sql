-- 支持敲鸭 CLI 作为 OAuth2 public client：
-- 1) client_id 唯一性只约束未软删除记录
-- 2) public client 不保存 client_secret
-- 3) 授权码记录保存 redirect_uri 与 PKCE 信息

ALTER TABLE oauth2_clients
    ALTER COLUMN client_secret_enc DROP NOT NULL;

ALTER TABLE oauth2_clients
    DROP CONSTRAINT IF EXISTS oauth2_clients_client_id_key;

DROP INDEX IF EXISTS ux_oauth2_clients_client_id_active;
CREATE UNIQUE INDEX ux_oauth2_clients_client_id_active
    ON oauth2_clients (client_id)
    WHERE deleted_at IS NULL;

ALTER TABLE oauth2_authorizations
    ADD COLUMN IF NOT EXISTS redirect_uri TEXT,
    ADD COLUMN IF NOT EXISTS authorization_code_challenge VARCHAR(256),
    ADD COLUMN IF NOT EXISTS authorization_code_challenge_method VARCHAR(20);

COMMENT ON COLUMN oauth2_authorizations.redirect_uri IS '授权请求使用的重定向URI，Token交换时必须一致';
COMMENT ON COLUMN oauth2_authorizations.authorization_code_challenge IS 'PKCE code_challenge';
COMMENT ON COLUMN oauth2_authorizations.authorization_code_challenge_method IS 'PKCE code_challenge_method';

INSERT INTO oauth2_clients (
    id,
    client_id,
    client_secret_enc,
    client_name,
    redirect_uris,
    grant_types,
    scopes,
    client_authentication_methods,
    access_token_validity_seconds,
    refresh_token_validity_seconds,
    require_proof_key,
    require_authorization_consent,
    status,
    created_by
) VALUES (
    'qiaoya-cli-oauth-client',
    'qiaoya-cli',
    NULL,
    '敲鸭 CLI',
    '["http://127.0.0.1/callback","http://localhost/callback"]',
    '["authorization_code","refresh_token"]',
    '["openid","profile","email","qiaoya.read"]',
    '["none"]',
    3600,
    2592000,
    TRUE,
    TRUE,
    'ACTIVE',
    NULL
) ON CONFLICT (client_id) WHERE deleted_at IS NULL DO UPDATE SET
    client_secret_enc = NULL,
    client_name = EXCLUDED.client_name,
    redirect_uris = EXCLUDED.redirect_uris,
    grant_types = EXCLUDED.grant_types,
    scopes = EXCLUDED.scopes,
    client_authentication_methods = EXCLUDED.client_authentication_methods,
    access_token_validity_seconds = EXCLUDED.access_token_validity_seconds,
    refresh_token_validity_seconds = EXCLUDED.refresh_token_validity_seconds,
    require_proof_key = EXCLUDED.require_proof_key,
    require_authorization_consent = EXCLUDED.require_authorization_consent,
    status = EXCLUDED.status,
    update_time = CURRENT_TIMESTAMP;
