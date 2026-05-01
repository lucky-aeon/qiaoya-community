-- 将 qiaoya-cli OAuth scope 调整为现有前后端统一的 read。
-- V78 可能已经在本地环境执行过，不能再修改已执行迁移。

UPDATE oauth2_clients
SET scopes = '["openid","profile","email","read"]',
    update_time = CURRENT_TIMESTAMP
WHERE client_id = 'qiaoya-cli'
  AND deleted_at IS NULL;

UPDATE oauth2_authorization_consents
SET authorities = REPLACE(authorities, 'qiaoya.read', 'read'),
    update_time = CURRENT_TIMESTAMP
WHERE client_id = 'qiaoya-cli'
  AND deleted_at IS NULL
  AND authorities LIKE '%qiaoya.read%';

UPDATE oauth2_authorizations
SET access_token_scopes = REPLACE(access_token_scopes, 'qiaoya.read', 'read'),
    update_time = CURRENT_TIMESTAMP
WHERE client_id = 'qiaoya-cli'
  AND deleted_at IS NULL
  AND access_token_scopes LIKE '%qiaoya.read%';
