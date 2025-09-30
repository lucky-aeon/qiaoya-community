-- Align column types with Java LocalDateTime by converting TIMESTAMPTZ -> TIMESTAMP
-- Keep the same local time in Asia/Shanghai

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'ai_daily_items' AND column_name = 'published_at'
    ) THEN
        BEGIN
            ALTER TABLE ai_daily_items 
                ALTER COLUMN published_at TYPE TIMESTAMP WITHOUT TIME ZONE 
                USING published_at AT TIME ZONE 'Asia/Shanghai';
        EXCEPTION WHEN others THEN
            -- ignore if already correct type
            NULL;
        END;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'ai_daily_items' AND column_name = 'fetched_at'
    ) THEN
        BEGIN
            ALTER TABLE ai_daily_items 
                ALTER COLUMN fetched_at TYPE TIMESTAMP WITHOUT TIME ZONE 
                USING fetched_at AT TIME ZONE 'Asia/Shanghai';
        EXCEPTION WHEN others THEN
            NULL;
        END;
    END IF;
END $$;

