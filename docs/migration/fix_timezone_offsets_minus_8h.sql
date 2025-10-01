-- 用途：修复因时区误读导致的时间+8小时问题（PostgreSQL）
-- 说明：以下更新会将相关时间字段统一减去 8 小时。
--      如需限定时间窗口，请自行在 WHERE 中追加时间范围条件。
-- 注意：请在执行前做好备份，并先在测试库验证。

BEGIN;

-- users（基础：create_time/update_time/deleted_at）
UPDATE users SET create_time = create_time - INTERVAL '8 hours' WHERE create_time IS NOT NULL;
UPDATE users SET update_time = update_time - INTERVAL '8 hours' WHERE update_time IS NOT NULL;
UPDATE users SET deleted_at = deleted_at - INTERVAL '8 hours' WHERE deleted_at IS NOT NULL;

-- posts（额外：publish_time/solved_at）
UPDATE posts SET create_time = create_time - INTERVAL '8 hours' WHERE create_time IS NOT NULL;
UPDATE posts SET update_time = update_time - INTERVAL '8 hours' WHERE update_time IS NOT NULL;
UPDATE posts SET deleted_at = deleted_at - INTERVAL '8 hours' WHERE deleted_at IS NOT NULL;
UPDATE posts SET publish_time = publish_time - INTERVAL '8 hours' WHERE publish_time IS NOT NULL;
UPDATE posts SET solved_at  = solved_at  - INTERVAL '8 hours' WHERE solved_at  IS NOT NULL;

-- comments
UPDATE comments SET create_time = create_time - INTERVAL '8 hours' WHERE create_time IS NOT NULL;
UPDATE comments SET update_time = update_time - INTERVAL '8 hours' WHERE update_time IS NOT NULL;
UPDATE comments SET deleted_at = deleted_at - INTERVAL '8 hours' WHERE deleted_at IS NOT NULL;

-- courses
UPDATE courses SET create_time = create_time - INTERVAL '8 hours' WHERE create_time IS NOT NULL;
UPDATE courses SET update_time = update_time - INTERVAL '8 hours' WHERE update_time IS NOT NULL;
UPDATE courses SET deleted_at = deleted_at - INTERVAL '8 hours' WHERE deleted_at IS NOT NULL;

-- chapters
UPDATE chapters SET create_time = create_time - INTERVAL '8 hours' WHERE create_time IS NOT NULL;
UPDATE chapters SET update_time = update_time - INTERVAL '8 hours' WHERE update_time IS NOT NULL;
UPDATE chapters SET deleted_at = deleted_at - INTERVAL '8 hours' WHERE deleted_at IS NOT NULL;

-- resources
UPDATE resources SET create_time = create_time - INTERVAL '8 hours' WHERE create_time IS NOT NULL;
UPDATE resources SET update_time = update_time - INTERVAL '8 hours' WHERE update_time IS NOT NULL;
UPDATE resources SET deleted_at = deleted_at - INTERVAL '8 hours' WHERE deleted_at IS NOT NULL;

-- likes
UPDATE likes SET create_time = create_time - INTERVAL '8 hours' WHERE create_time IS NOT NULL;
UPDATE likes SET update_time = update_time - INTERVAL '8 hours' WHERE update_time IS NOT NULL;
UPDATE likes SET deleted_at = deleted_at - INTERVAL '8 hours' WHERE deleted_at IS NOT NULL;

-- reactions
UPDATE reactions SET create_time = create_time - INTERVAL '8 hours' WHERE create_time IS NOT NULL;
UPDATE reactions SET update_time = update_time - INTERVAL '8 hours' WHERE update_time IS NOT NULL;
UPDATE reactions SET deleted_at = deleted_at - INTERVAL '8 hours' WHERE deleted_at IS NOT NULL;

-- post_accepted_comments
UPDATE post_accepted_comments SET create_time = create_time - INTERVAL '8 hours' WHERE create_time IS NOT NULL;
UPDATE post_accepted_comments SET update_time = update_time - INTERVAL '8 hours' WHERE update_time IS NOT NULL;
UPDATE post_accepted_comments SET deleted_at = deleted_at - INTERVAL '8 hours' WHERE deleted_at IS NOT NULL;

-- categories
UPDATE categories SET create_time = create_time - INTERVAL '8 hours' WHERE create_time IS NOT NULL;
UPDATE categories SET update_time = update_time - INTERVAL '8 hours' WHERE update_time IS NOT NULL;
UPDATE categories SET deleted_at = deleted_at - INTERVAL '8 hours' WHERE deleted_at IS NOT NULL;

-- update_logs
UPDATE update_logs SET create_time = create_time - INTERVAL '8 hours' WHERE create_time IS NOT NULL;
UPDATE update_logs SET update_time = update_time - INTERVAL '8 hours' WHERE update_time IS NOT NULL;
UPDATE update_logs SET deleted_at = deleted_at - INTERVAL '8 hours' WHERE deleted_at IS NOT NULL;

-- update_log_changes
UPDATE update_log_changes SET create_time = create_time - INTERVAL '8 hours' WHERE create_time IS NOT NULL;
UPDATE update_log_changes SET update_time = update_time - INTERVAL '8 hours' WHERE update_time IS NOT NULL;
UPDATE update_log_changes SET deleted_at = deleted_at - INTERVAL '8 hours' WHERE deleted_at IS NOT NULL;

-- cdk_codes（如涉及时间字段则一并修正）
UPDATE cdk_codes SET create_time = create_time - INTERVAL '8 hours' WHERE create_time IS NOT NULL;
UPDATE cdk_codes SET update_time = update_time - INTERVAL '8 hours' WHERE update_time IS NOT NULL;
UPDATE cdk_codes SET deleted_at = deleted_at - INTERVAL '8 hours' WHERE deleted_at IS NOT NULL;

-- orders（额外：activated_time）
UPDATE orders SET create_time = create_time - INTERVAL '8 hours' WHERE create_time IS NOT NULL;
UPDATE orders SET update_time = update_time - INTERVAL '8 hours' WHERE update_time IS NOT NULL;
UPDATE orders SET deleted_at = deleted_at - INTERVAL '8 hours' WHERE deleted_at IS NOT NULL;
UPDATE orders SET activated_time = activated_time - INTERVAL '8 hours' WHERE activated_time IS NOT NULL;

-- user_subscriptions（额外：start_time/end_time）
UPDATE user_subscriptions SET create_time = create_time - INTERVAL '8 hours' WHERE create_time IS NOT NULL;
UPDATE user_subscriptions SET update_time = update_time - INTERVAL '8 hours' WHERE update_time IS NOT NULL;
UPDATE user_subscriptions SET deleted_at = deleted_at - INTERVAL '8 hours' WHERE deleted_at IS NOT NULL;
UPDATE user_subscriptions SET start_time = start_time - INTERVAL '8 hours' WHERE start_time IS NOT NULL;
UPDATE user_subscriptions SET end_time   = end_time   - INTERVAL '8 hours' WHERE end_time   IS NOT NULL;

-- expression_types（如存在创建/更新时间字段）
UPDATE expression_types SET create_time = create_time - INTERVAL '8 hours' WHERE create_time IS NOT NULL;
UPDATE expression_types SET update_time = update_time - INTERVAL '8 hours' WHERE update_time IS NOT NULL;
UPDATE expression_types SET deleted_at = deleted_at - INTERVAL '8 hours' WHERE deleted_at IS NOT NULL;

-- ai_daily_items（额外：published_at/fetched_at）
UPDATE ai_daily_items SET create_time = create_time - INTERVAL '8 hours' WHERE create_time IS NOT NULL;
UPDATE ai_daily_items SET update_time = update_time - INTERVAL '8 hours' WHERE update_time IS NOT NULL;
UPDATE ai_daily_items SET deleted_at = deleted_at - INTERVAL '8 hours' WHERE deleted_at IS NOT NULL;
UPDATE ai_daily_items SET published_at = published_at - INTERVAL '8 hours' WHERE published_at IS NOT NULL;
UPDATE ai_daily_items SET fetched_at   = fetched_at   - INTERVAL '8 hours' WHERE fetched_at   IS NOT NULL;

COMMIT;

-- 可选回滚（若误执行，可将以上减去改为加上再执行，或从备份恢复）
-- BEGIN; ... UPDATE ... + INTERVAL '8 hours' ... COMMIT;

