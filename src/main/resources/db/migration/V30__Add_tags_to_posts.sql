-- 为文章添加标签字段
ALTER TABLE posts
    ADD COLUMN tags JSON;

COMMENT ON COLUMN posts.tags IS '标签（JSON数组）';

