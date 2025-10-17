-- 优化管理后台仪表盘统计的查询性能
-- 数据库：PostgreSQL

-- 1) user_course_progress 按 last_access_time 的时间范围查询较频繁
--    为 last_access_time 添加索引（仅针对未删除记录）
CREATE INDEX IF NOT EXISTS idx_ucr_last_access_time
  ON user_course_progress (last_access_time)
  WHERE deleted_at IS NULL;

-- 2) user_subscriptions 在按 user_id + 时间范围（start_time/end_time）过滤时可能出现全表扫描
--    添加复合索引以覆盖查询条件
CREATE INDEX IF NOT EXISTS idx_user_subscriptions_user_time
  ON user_subscriptions (user_id, start_time, end_time);

