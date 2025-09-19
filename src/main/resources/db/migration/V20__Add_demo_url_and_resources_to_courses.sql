-- 为课程表添加演示地址和资源字段
ALTER TABLE courses
ADD COLUMN demo_url VARCHAR(500),
ADD COLUMN resources JSON;