-- 通用讨论摘要表（文章/章节等）
-- 数据库：PostgreSQL

CREATE TABLE IF NOT EXISTS discussion_summaries (
    id           VARCHAR(36) PRIMARY KEY,
    target_type  VARCHAR(32) NOT NULL,
    target_id    VARCHAR(36) NOT NULL,
    summary      TEXT        NOT NULL,
    create_time  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at   TIMESTAMP   NULL
);

-- 仅对未软删除的记录建立唯一约束，便于软删后重建
CREATE UNIQUE INDEX IF NOT EXISTS uq_discussion_summary_target 
  ON discussion_summaries(target_type, target_id) 
  WHERE deleted_at IS NULL;

COMMENT ON TABLE discussion_summaries IS '通用讨论摘要（文章/章节等）';
COMMENT ON COLUMN discussion_summaries.target_type IS '摘要目标类型：POST/CHAPTER';
COMMENT ON COLUMN discussion_summaries.target_id   IS '摘要目标ID';

