-- 修改subscription_plans表，将description字段改为benefits字段
-- 使用JSON类型存储权益列表

-- 添加新的benefits字段（JSON类型）
ALTER TABLE subscription_plans ADD COLUMN benefits JSON;

-- 将现有的description数据迁移到benefits字段（转换为JSON数组格式）
UPDATE subscription_plans
SET benefits = CASE
    WHEN description IS NOT NULL AND description != ''
    THEN json_build_array(description)
    ELSE json_build_array()
END;

-- 删除原有的description字段
ALTER TABLE subscription_plans DROP COLUMN description;

-- 添加列注释
COMMENT ON COLUMN subscription_plans.benefits IS '套餐权益列表，存储为JSON数组格式';