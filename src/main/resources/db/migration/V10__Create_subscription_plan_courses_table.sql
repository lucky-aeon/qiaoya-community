-- 创建套餐-课程关联表
CREATE TABLE subscription_plan_courses (
    id VARCHAR(36) PRIMARY KEY,
    subscription_plan_id VARCHAR(36) NOT NULL COMMENT '套餐ID',
    course_id VARCHAR(36) NOT NULL COMMENT '课程ID',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '逻辑删除标记',
    
    UNIQUE KEY uk_plan_course_deleted (subscription_plan_id, course_id, deleted),
    KEY idx_subscription_plan_id (subscription_plan_id),
    KEY idx_course_id (course_id),
    KEY idx_create_time (create_time),
    
    -- 外键约束
    CONSTRAINT fk_subscription_plan_courses_plan_id 
        FOREIGN KEY (subscription_plan_id) 
        REFERENCES subscription_plans(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_subscription_plan_courses_course_id 
        FOREIGN KEY (course_id) 
        REFERENCES courses(id) 
        ON DELETE CASCADE
);

-- 添加表注释
ALTER TABLE subscription_plan_courses COMMENT = '套餐-课程关联表，定义套餐包含哪些课程';