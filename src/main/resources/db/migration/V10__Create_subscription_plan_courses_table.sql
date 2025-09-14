CREATE TABLE subscription_plan_courses (
                                           id VARCHAR(36) PRIMARY KEY,
                                           subscription_plan_id VARCHAR(36) NOT NULL,
                                           course_id VARCHAR(36) NOT NULL,
                                           create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                           update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                           deleted BOOLEAN DEFAULT FALSE,
);

-- 表注释
COMMENT ON TABLE subscription_plan_courses IS '套餐-课程关联表，定义套餐包含哪些课程';

-- 列注释
COMMENT ON COLUMN subscription_plan_courses.subscription_plan_id IS '套餐ID';
COMMENT ON COLUMN subscription_plan_courses.course_id IS '课程ID';
COMMENT ON COLUMN subscription_plan_courses.create_time IS '创建时间';
COMMENT ON COLUMN subscription_plan_courses.update_time IS '更新时间';
COMMENT ON COLUMN subscription_plan_courses.deleted IS '逻辑删除标记';
