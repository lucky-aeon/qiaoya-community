-- 用户学习进度表（章节级）
CREATE TABLE IF NOT EXISTS user_chapter_progress (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    course_id VARCHAR(36) NOT NULL,
    chapter_id VARCHAR(36) NOT NULL,
    progress_percent SMALLINT NOT NULL DEFAULT 0,
    last_position_sec INTEGER DEFAULT 0,
    time_spent_sec INTEGER NOT NULL DEFAULT 0,
    last_access_time TIMESTAMP,
    completed_at TIMESTAMP,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL
);

-- 唯一约束：同一用户同一章节仅一条有效记录
CREATE UNIQUE INDEX IF NOT EXISTS uk_user_chapter_progress
ON user_chapter_progress (user_id, chapter_id)
WHERE deleted_at IS NULL;

-- 常用查询索引
CREATE INDEX IF NOT EXISTS idx_ucp_user ON user_chapter_progress (user_id);
CREATE INDEX IF NOT EXISTS idx_ucp_course ON user_chapter_progress (course_id);
CREATE INDEX IF NOT EXISTS idx_ucp_chapter ON user_chapter_progress (chapter_id);
CREATE INDEX IF NOT EXISTS idx_ucp_last_access ON user_chapter_progress (last_access_time);

COMMENT ON TABLE user_chapter_progress IS '用户章节学习进度（状态表）';
COMMENT ON COLUMN user_chapter_progress.id IS '主键ID（UUID）';
COMMENT ON COLUMN user_chapter_progress.user_id IS '用户ID';
COMMENT ON COLUMN user_chapter_progress.course_id IS '课程ID';
COMMENT ON COLUMN user_chapter_progress.chapter_id IS '章节ID';
COMMENT ON COLUMN user_chapter_progress.progress_percent IS '学习进度百分比（0-100，仅递增）';
COMMENT ON COLUMN user_chapter_progress.last_position_sec IS '最近播放/阅读位置（单位：秒，仅递增）';
COMMENT ON COLUMN user_chapter_progress.time_spent_sec IS '累计有效学习时长（单位：秒，增量裁剪防作弊）';
COMMENT ON COLUMN user_chapter_progress.last_access_time IS '最近访问时间';
COMMENT ON COLUMN user_chapter_progress.completed_at IS '章节完成时间（达到阈值记为完成）';
COMMENT ON COLUMN user_chapter_progress.create_time IS '创建时间';
COMMENT ON COLUMN user_chapter_progress.update_time IS '更新时间';
COMMENT ON COLUMN user_chapter_progress.deleted_at IS '逻辑删除时间（NULL 表示未删除）';

-- 用户学习进度表（课程级汇总）
CREATE TABLE IF NOT EXISTS user_course_progress (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    course_id VARCHAR(36) NOT NULL,
    total_chapters INTEGER NOT NULL DEFAULT 0,
    completed_chapters INTEGER NOT NULL DEFAULT 0,
    progress_percent SMALLINT NOT NULL DEFAULT 0,
    last_access_chapter_id VARCHAR(36),
    last_access_time TIMESTAMP,
    completed_at TIMESTAMP,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_course_progress
ON user_course_progress (user_id, course_id)
WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_ucr_user ON user_course_progress (user_id);
CREATE INDEX IF NOT EXISTS idx_ucr_course ON user_course_progress (course_id);
CREATE INDEX IF NOT EXISTS idx_ucr_completed ON user_course_progress (completed_at);

COMMENT ON TABLE user_course_progress IS '用户课程学习进度（汇总表）';
COMMENT ON COLUMN user_course_progress.id IS '主键ID（UUID）';
COMMENT ON COLUMN user_course_progress.user_id IS '用户ID';
COMMENT ON COLUMN user_course_progress.course_id IS '课程ID';
COMMENT ON COLUMN user_course_progress.total_chapters IS '课程章节总数快照';
COMMENT ON COLUMN user_course_progress.completed_chapters IS '已完成章节数';
COMMENT ON COLUMN user_course_progress.progress_percent IS '课程进度百分比（已完成/总数）';
COMMENT ON COLUMN user_course_progress.last_access_chapter_id IS '最近访问的章节ID';
COMMENT ON COLUMN user_course_progress.last_access_time IS '最近访问时间';
COMMENT ON COLUMN user_course_progress.completed_at IS '课程完成时间（全部章节完成）';
COMMENT ON COLUMN user_course_progress.create_time IS '创建时间';
COMMENT ON COLUMN user_course_progress.update_time IS '更新时间';
COMMENT ON COLUMN user_course_progress.deleted_at IS '逻辑删除时间（NULL 表示未删除）';
