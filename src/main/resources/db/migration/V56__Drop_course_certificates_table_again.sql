-- 再次安全删除 course_certificates 表，避免后续迁移重新创建
-- 注意：此迁移用于覆盖早期 V53 可能重新创建的表结构

DROP TABLE IF EXISTS course_certificates CASCADE;

