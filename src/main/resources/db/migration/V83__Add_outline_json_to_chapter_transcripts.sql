ALTER TABLE IF EXISTS chapter_transcripts
    ADD COLUMN IF NOT EXISTS outline_json TEXT;

COMMENT ON COLUMN chapter_transcripts.outline_json IS 'AI 课代表按主题整理的大模块时间轴 JSON';
