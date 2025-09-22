-- 为CDK表添加订单相关字段
ALTER TABLE cdk_codes
ADD COLUMN acquisition_type VARCHAR(20) NOT NULL DEFAULT 'PURCHASE',
ADD COLUMN price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
ADD COLUMN remark VARCHAR(500);

-- 添加列注释
COMMENT ON COLUMN cdk_codes.acquisition_type IS 'CDK获得方式：PURCHASE购买/GIFT赠送';
COMMENT ON COLUMN cdk_codes.price IS 'CDK对应价格，赠送时为0';
COMMENT ON COLUMN cdk_codes.remark IS 'CDK备注信息';

-- 为新字段创建索引
CREATE INDEX idx_cdk_codes_acquisition_type ON cdk_codes(acquisition_type);
CREATE INDEX idx_cdk_codes_price ON cdk_codes(price);

-- 为查询优化创建复合索引
CREATE INDEX idx_cdk_codes_type_acquisition ON cdk_codes(cdk_type, acquisition_type);
CREATE INDEX idx_cdk_codes_status_acquisition ON cdk_codes(status, acquisition_type);