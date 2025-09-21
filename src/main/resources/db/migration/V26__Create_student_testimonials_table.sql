-- 创建学员评价表
CREATE TABLE student_testimonials (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    content TEXT NOT NULL,
    rating INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    sort_order INTEGER NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 创建用户ID唯一索引（每个用户只能有一条评价）
CREATE UNIQUE INDEX idx_student_testimonials_user_id ON student_testimonials(user_id) WHERE deleted = FALSE;

-- 添加表注释
COMMENT ON TABLE student_testimonials IS '学员评价表';
COMMENT ON COLUMN student_testimonials.id IS '评价ID (UUID格式)';
COMMENT ON COLUMN student_testimonials.user_id IS '发布用户ID';
COMMENT ON COLUMN student_testimonials.content IS '评价内容';
COMMENT ON COLUMN student_testimonials.rating IS '评分（1-5分）';
COMMENT ON COLUMN student_testimonials.status IS '状态：PENDING-待审核，APPROVED-已通过，REJECTED-已拒绝，PUBLISHED-已发布';
COMMENT ON COLUMN student_testimonials.sort_order IS '排序权重（数值越大排序越靠前）';
COMMENT ON COLUMN student_testimonials.create_time IS '创建时间';
COMMENT ON COLUMN student_testimonials.update_time IS '更新时间';
COMMENT ON COLUMN student_testimonials.deleted IS '是否删除';