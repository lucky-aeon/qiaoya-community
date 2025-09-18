-- 为课程表添加价格和封面字段
ALTER TABLE courses ADD COLUMN price DECIMAL(10,2) DEFAULT 0.00;
ALTER TABLE courses ADD COLUMN original_price DECIMAL(10,2) DEFAULT 0.00;
ALTER TABLE courses ADD COLUMN cover_image VARCHAR(500);

-- 添加字段注释
COMMENT ON COLUMN courses.price IS '课程售价';
COMMENT ON COLUMN courses.original_price IS '课程原价';
COMMENT ON COLUMN courses.cover_image IS '课程封面图片URL';