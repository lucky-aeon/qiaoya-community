ALTER TABLE update_logs
    ADD COLUMN IF NOT EXISTS is_important BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN update_logs.is_important IS '是否重要更新';
