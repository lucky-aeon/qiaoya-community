-- 调整 ai_daily_items 的唯一索引：
-- 使其仅对未软删除（deleted_at IS NULL）的记录生效，
-- 避免逻辑删除后的历史记录阻断新数据插入。

-- 1) url_hash 唯一索引（仅未删除生效）
DROP INDEX IF EXISTS ux_ai_daily_items_url_hash;
CREATE UNIQUE INDEX ux_ai_daily_items_url_hash
    ON ai_daily_items (url_hash)
    WHERE deleted_at IS NULL;

-- 2) (source, source_item_id) 唯一索引（仅未删除且 source_item_id 不为空生效）
DROP INDEX IF EXISTS ux_ai_daily_items_source_item;
CREATE UNIQUE INDEX ux_ai_daily_items_source_item
    ON ai_daily_items (source, source_item_id)
    WHERE source_item_id IS NOT NULL AND deleted_at IS NULL;

