-- 说明：开发阶段证书表无正式数据，按设计改造为“用户标签系统”后，该表不再使用（重命名至V54以避免版本冲突）
-- 安全删除 course_certificates 表（若存在）及相关索引

DROP TABLE IF EXISTS course_certificates CASCADE;
