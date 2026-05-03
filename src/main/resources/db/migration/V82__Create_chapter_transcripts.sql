-- 课程章节视频转写任务与时间轴分段

CREATE TABLE IF NOT EXISTS chapter_transcripts (
    id               VARCHAR(36) PRIMARY KEY,
    course_id        VARCHAR(36) NOT NULL,
    chapter_id       VARCHAR(36) NOT NULL,
    resource_id      VARCHAR(36) NOT NULL,
    provider         VARCHAR(32) NOT NULL,
    model            VARCHAR(100) NOT NULL,
    provider_task_id VARCHAR(128),
    status           VARCHAR(32) NOT NULL,
    language         VARCHAR(32),
    duration_ms      BIGINT,
    text             TEXT,
    summary          TEXT,
    key_points_json  TEXT,
    raw_result_json  TEXT,
    error_code       VARCHAR(128),
    error_message    TEXT,
    submitted_at     TIMESTAMP NULL,
    completed_at     TIMESTAMP NULL,
    create_time      TIMESTAMP NOT NULL DEFAULT NOW(),
    update_time      TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at       TIMESTAMP NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_chapter_transcript_active
    ON chapter_transcripts(chapter_id, resource_id)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_chapter_transcript_status
    ON chapter_transcripts(status)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_chapter_transcript_chapter
    ON chapter_transcripts(chapter_id)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_chapter_transcript_provider_task
    ON chapter_transcripts(provider_task_id)
    WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS chapter_transcript_segments (
    id            VARCHAR(36) PRIMARY KEY,
    transcript_id VARCHAR(36) NOT NULL,
    course_id     VARCHAR(36) NOT NULL,
    chapter_id    VARCHAR(36) NOT NULL,
    start_ms      BIGINT,
    end_ms        BIGINT,
    speaker       VARCHAR(64),
    text          TEXT NOT NULL,
    sort_order    INTEGER NOT NULL DEFAULT 0,
    create_time   TIMESTAMP NOT NULL DEFAULT NOW(),
    update_time   TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at    TIMESTAMP NULL
);

CREATE INDEX IF NOT EXISTS idx_chapter_transcript_segment_transcript_order
    ON chapter_transcript_segments(transcript_id, sort_order)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_chapter_transcript_segment_chapter_time
    ON chapter_transcript_segments(chapter_id, start_ms)
    WHERE deleted_at IS NULL;

COMMENT ON TABLE chapter_transcripts IS '课程章节视频转写任务与最终结果';
COMMENT ON TABLE chapter_transcript_segments IS '课程章节视频转写时间轴分段';
