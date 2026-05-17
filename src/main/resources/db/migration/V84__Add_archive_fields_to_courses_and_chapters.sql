ALTER TABLE courses
    ADD COLUMN archived BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN archive_reason TEXT,
    ADD COLUMN archived_at TIMESTAMP;

ALTER TABLE chapters
    ADD COLUMN archived BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN archive_reason TEXT,
    ADD COLUMN archived_at TIMESTAMP;

CREATE INDEX idx_courses_archived ON courses(archived);
CREATE INDEX idx_chapters_archived ON chapters(archived);

COMMENT ON COLUMN courses.archived IS '是否归档';
COMMENT ON COLUMN courses.archive_reason IS '归档原因';
COMMENT ON COLUMN courses.archived_at IS '归档时间';
COMMENT ON COLUMN chapters.archived IS '是否归档';
COMMENT ON COLUMN chapters.archive_reason IS '归档原因';
COMMENT ON COLUMN chapters.archived_at IS '归档时间';
