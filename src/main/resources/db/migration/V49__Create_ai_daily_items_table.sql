-- AI Daily Items table (PostgreSQL)
-- Stores aggregated AIBase daily news with full HTML content

CREATE TABLE IF NOT EXISTS ai_daily_items (
    id              VARCHAR(36) PRIMARY KEY,
    source          VARCHAR(32) NOT NULL, -- enum: AIBASE
    title           VARCHAR(512) NOT NULL,
    summary         TEXT,
    content         TEXT NOT NULL, -- full HTML content
    url             VARCHAR(1024) NOT NULL,
    source_item_id  BIGINT, -- original source incremental id
    published_at    TIMESTAMPTZ NOT NULL,
    fetched_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    url_hash        VARCHAR(64) NOT NULL,
    status          VARCHAR(32) NOT NULL DEFAULT 'PUBLISHED', -- enum: PUBLISHED/HIDDEN
    metadata        JSONB,
    create_time     TIMESTAMP,
    update_time     TIMESTAMP,
    deleted_at      TIMESTAMP NULL
);

-- Uniqueness and performance indexes
CREATE UNIQUE INDEX IF NOT EXISTS ux_ai_daily_items_url_hash
    ON ai_daily_items (url_hash);

-- Ensure uniqueness by (source, source_item_id) when source_item_id is present
CREATE UNIQUE INDEX IF NOT EXISTS ux_ai_daily_items_source_item
    ON ai_daily_items (source, source_item_id)
    WHERE source_item_id IS NOT NULL;

-- Common query indexes
CREATE INDEX IF NOT EXISTS idx_ai_daily_items_published_at
    ON ai_daily_items (published_at);

CREATE INDEX IF NOT EXISTS idx_ai_daily_items_source
    ON ai_daily_items (source);

COMMENT ON TABLE ai_daily_items IS 'AI Daily aggregated articles (AIBase)';
COMMENT ON COLUMN ai_daily_items.source IS 'Source enum: AIBASE';
COMMENT ON COLUMN ai_daily_items.status IS 'Status enum: PUBLISHED/HIDDEN';

