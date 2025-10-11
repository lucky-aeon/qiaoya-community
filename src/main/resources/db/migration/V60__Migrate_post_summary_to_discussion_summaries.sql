-- 将已有的文章摘要迁移到通用讨论摘要表
-- 若目标表已存在同一 target（未软删除），则跳过

INSERT INTO discussion_summaries (id, target_type, target_id, summary, create_time, update_time, deleted_at)
SELECT ps.id, 'POST', ps.post_id, ps.summary, ps.create_time, ps.update_time, ps.deleted_at
FROM post_summary ps
WHERE NOT EXISTS (
  SELECT 1 FROM discussion_summaries ds
  WHERE ds.target_type = 'POST' AND ds.target_id = ps.post_id AND ds.deleted_at IS NULL
);

