-- 为面试题表添加标签字段，支持标题搜索和标签筛选功能
ALTER TABLE interview_questions
ADD COLUMN tags TEXT COMMENT '标签，逗号分隔，如: Vue3,响应式原理,源码';

-- 为标题字段添加索引，优化搜索性能
CREATE INDEX idx_interview_questions_title ON interview_questions(title);

-- 为难度评分字段添加索引，优化难度筛选性能
CREATE INDEX idx_interview_questions_rating ON interview_questions(rating);
