-- Migrate logical delete from boolean to timestamp (deleted_at)
-- Database: PostgreSQL

-- 1) Add deleted_at columns to all entities that previously used boolean deleted
--    and backfill timestamp for rows marked as deleted=true

-- Helper: add column + backfill for a table
-- NOTE: Flyway doesn't support variables; we inline per table for clarity and safety.

-- users
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;
UPDATE users SET deleted_at = NOW() WHERE deleted = TRUE AND deleted_at IS NULL;

-- categories
ALTER TABLE IF EXISTS categories ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;
UPDATE categories SET deleted_at = NOW() WHERE deleted = TRUE AND deleted_at IS NULL;

-- posts
ALTER TABLE IF EXISTS posts ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;
UPDATE posts SET deleted_at = NOW() WHERE deleted = TRUE AND deleted_at IS NULL;

-- post_likes
ALTER TABLE IF EXISTS post_likes ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;
UPDATE post_likes SET deleted_at = NOW() WHERE deleted = TRUE AND deleted_at IS NULL;

-- comments
ALTER TABLE IF EXISTS comments ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;
UPDATE comments SET deleted_at = NOW() WHERE deleted = TRUE AND deleted_at IS NULL;

-- chapters
ALTER TABLE IF EXISTS chapters ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;
UPDATE chapters SET deleted_at = NOW() WHERE deleted = TRUE AND deleted_at IS NULL;

-- resources
ALTER TABLE IF EXISTS resources ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;
UPDATE resources SET deleted_at = NOW() WHERE deleted = TRUE AND deleted_at IS NULL;

-- courses
ALTER TABLE IF EXISTS courses ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;
UPDATE courses SET deleted_at = NOW() WHERE deleted = TRUE AND deleted_at IS NULL;

-- subscription_plans
ALTER TABLE IF EXISTS subscription_plans ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;
UPDATE subscription_plans SET deleted_at = NOW() WHERE deleted = TRUE AND deleted_at IS NULL;

-- subscription_plan_courses
ALTER TABLE IF EXISTS subscription_plan_courses ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;
UPDATE subscription_plan_courses SET deleted_at = NOW() WHERE deleted = TRUE AND deleted_at IS NULL;

-- user_subscriptions
ALTER TABLE IF EXISTS user_subscriptions ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;
UPDATE user_subscriptions SET deleted_at = NOW() WHERE deleted = TRUE AND deleted_at IS NULL;

-- user_courses
ALTER TABLE IF EXISTS user_courses ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;
UPDATE user_courses SET deleted_at = NOW() WHERE deleted = TRUE AND deleted_at IS NULL;

-- student_testimonials
ALTER TABLE IF EXISTS student_testimonials ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;
UPDATE student_testimonials SET deleted_at = NOW() WHERE deleted = TRUE AND deleted_at IS NULL;

-- cdk_codes
ALTER TABLE IF EXISTS cdk_codes ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;
UPDATE cdk_codes SET deleted_at = NOW() WHERE deleted = TRUE AND deleted_at IS NULL;

-- user_activity_logs
ALTER TABLE IF EXISTS user_activity_logs ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;
UPDATE user_activity_logs SET deleted_at = NOW() WHERE deleted = TRUE AND deleted_at IS NULL;

-- system_configs
ALTER TABLE IF EXISTS system_configs ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;
UPDATE system_configs SET deleted_at = NOW() WHERE deleted = TRUE AND deleted_at IS NULL;

-- update_logs
ALTER TABLE IF EXISTS update_logs ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;
UPDATE update_logs SET deleted_at = NOW() WHERE deleted = TRUE AND deleted_at IS NULL;

-- update_log_changes
ALTER TABLE IF EXISTS update_log_changes ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;
UPDATE update_log_changes SET deleted_at = NOW() WHERE deleted = TRUE AND deleted_at IS NULL;

-- orders
ALTER TABLE IF EXISTS orders ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;
UPDATE orders SET deleted_at = NOW() WHERE deleted = TRUE AND deleted_at IS NULL;

-- follows
ALTER TABLE IF EXISTS follows ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;
UPDATE follows SET deleted_at = NOW() WHERE deleted = TRUE AND deleted_at IS NULL;

-- post_accepted_comments
ALTER TABLE IF EXISTS post_accepted_comments ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;
UPDATE post_accepted_comments SET deleted_at = NOW() WHERE deleted = TRUE AND deleted_at IS NULL;

-- notifications
ALTER TABLE IF EXISTS notifications ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;
UPDATE notifications SET deleted_at = NOW() WHERE deleted = TRUE AND deleted_at IS NULL;

-- user_social_accounts
ALTER TABLE IF EXISTS user_social_accounts ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;
UPDATE user_social_accounts SET deleted_at = NOW() WHERE deleted = TRUE AND deleted_at IS NULL;


-- 2) Drop old indexes referencing boolean deleted and recreate with deleted_at IS NULL

-- follows indexes
DROP INDEX IF EXISTS uk_follows_unique;
DROP INDEX IF EXISTS idx_follows_follower;
DROP INDEX IF EXISTS idx_follows_target;
DROP INDEX IF EXISTS idx_follows_target_type;
CREATE UNIQUE INDEX IF NOT EXISTS uk_follows_unique_v2
  ON follows (follower_id, target_id, target_type)
  WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_follows_follower_v2
  ON follows (follower_id, status)
  WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_follows_target_v2
  ON follows (target_id, target_type, status)
  WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_follows_target_type_v2
  ON follows (target_type, status)
  WHERE deleted_at IS NULL;

-- notifications
DROP INDEX IF EXISTS idx_notifications_deleted;
CREATE INDEX IF NOT EXISTS idx_notifications_deleted_at
  ON notifications (deleted_at);

-- post_accepted_comments
DROP INDEX IF EXISTS uniq_post_comment;
DROP INDEX IF EXISTS idx_pac_post_id;
CREATE UNIQUE INDEX IF NOT EXISTS uniq_post_comment_v2
  ON post_accepted_comments (post_id, comment_id)
  WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_pac_post_id_v2
  ON post_accepted_comments (post_id)
  WHERE deleted_at IS NULL;

-- user_social_accounts
DROP INDEX IF EXISTS uniq_user_social_provider_open;
CREATE UNIQUE INDEX IF NOT EXISTS uniq_user_social_provider_open_v2
  ON user_social_accounts (provider, open_id)
  WHERE deleted_at IS NULL;

-- student_testimonials
DROP INDEX IF EXISTS idx_student_testimonials_user_id;
CREATE UNIQUE INDEX IF NOT EXISTS idx_student_testimonials_user_id_v2
  ON student_testimonials (user_id)
  WHERE deleted_at IS NULL;

-- user_subscriptions
DROP INDEX IF EXISTS uk_user_active_subscription;
CREATE UNIQUE INDEX IF NOT EXISTS uk_user_active_subscription_v2
  ON user_subscriptions (user_id)
  WHERE deleted_at IS NULL;


-- 3) Drop old boolean deleted columns
ALTER TABLE IF EXISTS users DROP COLUMN IF EXISTS deleted;
ALTER TABLE IF EXISTS categories DROP COLUMN IF EXISTS deleted;
ALTER TABLE IF EXISTS posts DROP COLUMN IF EXISTS deleted;
ALTER TABLE IF EXISTS post_likes DROP COLUMN IF EXISTS deleted;
ALTER TABLE IF EXISTS comments DROP COLUMN IF EXISTS deleted;
ALTER TABLE IF EXISTS chapters DROP COLUMN IF EXISTS deleted;
ALTER TABLE IF EXISTS resources DROP COLUMN IF EXISTS deleted;
ALTER TABLE IF EXISTS courses DROP COLUMN IF EXISTS deleted;
ALTER TABLE IF EXISTS subscription_plans DROP COLUMN IF EXISTS deleted;
ALTER TABLE IF EXISTS subscription_plan_courses DROP COLUMN IF EXISTS deleted;
ALTER TABLE IF EXISTS user_subscriptions DROP COLUMN IF EXISTS deleted;
ALTER TABLE IF EXISTS user_courses DROP COLUMN IF EXISTS deleted;
ALTER TABLE IF EXISTS student_testimonials DROP COLUMN IF EXISTS deleted;
ALTER TABLE IF EXISTS cdk_codes DROP COLUMN IF EXISTS deleted;
ALTER TABLE IF EXISTS user_activity_logs DROP COLUMN IF EXISTS deleted;
ALTER TABLE IF EXISTS system_configs DROP COLUMN IF EXISTS deleted;
ALTER TABLE IF EXISTS update_logs DROP COLUMN IF EXISTS deleted;
ALTER TABLE IF EXISTS update_log_changes DROP COLUMN IF EXISTS deleted;
ALTER TABLE IF EXISTS orders DROP COLUMN IF EXISTS deleted;
ALTER TABLE IF EXISTS follows DROP COLUMN IF EXISTS deleted;
ALTER TABLE IF EXISTS post_accepted_comments DROP COLUMN IF EXISTS deleted;
ALTER TABLE IF EXISTS notifications DROP COLUMN IF EXISTS deleted;
ALTER TABLE IF EXISTS user_social_accounts DROP COLUMN IF EXISTS deleted;

-- 4) Update column comments (optional)
COMMENT ON COLUMN posts.deleted_at IS '删除时间，NULL 表示未删除';
COMMENT ON COLUMN comments.deleted_at IS '删除时间，NULL 表示未删除';
COMMENT ON COLUMN categories.deleted_at IS '删除时间，NULL 表示未删除';
COMMENT ON COLUMN follows.deleted_at IS '删除时间，NULL 表示未删除';
COMMENT ON COLUMN notifications.deleted_at IS '删除时间，NULL 表示未删除';
COMMENT ON COLUMN user_subscriptions.deleted_at IS '删除时间，NULL 表示未删除';
COMMENT ON COLUMN user_social_accounts.deleted_at IS '删除时间，NULL 表示未删除';
COMMENT ON COLUMN post_accepted_comments.deleted_at IS '删除时间，NULL 表示未删除';
COMMENT ON COLUMN student_testimonials.deleted_at IS '删除时间，NULL 表示未删除';

