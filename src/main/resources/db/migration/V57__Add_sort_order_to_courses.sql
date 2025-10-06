-- 为课程表新增排序字段，默认值为 0，用于列表排序（倒序）
ALTER TABLE courses
    ADD COLUMN IF NOT EXISTS sort_order INTEGER NOT NULL DEFAULT 0;

-- 备注：无需创建索引，作为排序字段即可
