CREATE TABLE user_courses (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    course_id VARCHAR(36) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

-- 创建唯一索引，避免重复的用户-课程关系
CREATE UNIQUE INDEX idx_user_courses_user_course ON user_courses(user_id, course_id, deleted);

-- 创建索引优化查询性能
CREATE INDEX idx_user_courses_user_id ON user_courses(user_id);
CREATE INDEX idx_user_courses_course_id ON user_courses(course_id);

-- 表注释
COMMENT ON TABLE user_courses IS '用户课程权限表，记录用户直接拥有的课程权限（永久有效）';

-- 列注释
COMMENT ON COLUMN user_courses.id IS '主键ID';
COMMENT ON COLUMN user_courses.user_id IS '用户ID';
COMMENT ON COLUMN user_courses.course_id IS '课程ID';
COMMENT ON COLUMN user_courses.create_time IS '创建时间';
COMMENT ON COLUMN user_courses.update_time IS '更新时间';
COMMENT ON COLUMN user_courses.deleted IS '逻辑删除标记';