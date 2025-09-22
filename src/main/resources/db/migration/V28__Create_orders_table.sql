-- 创建订单表
CREATE TABLE orders (
    id VARCHAR(36) PRIMARY KEY,
    order_no VARCHAR(32) UNIQUE NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    cdk_code VARCHAR(32) NOT NULL,
    product_type VARCHAR(20) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    order_type VARCHAR(20) NOT NULL,
    amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    activated_time TIMESTAMP NOT NULL,
    remark VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

-- 添加表注释
COMMENT ON TABLE orders IS '订单记录表，CDK激活时自动创建';

-- 添加列注释
COMMENT ON COLUMN orders.order_no IS '订单号，格式：ORD+时间戳+序号';
COMMENT ON COLUMN orders.user_id IS '用户ID，关联users表';
COMMENT ON COLUMN orders.cdk_code IS '关联的CDK激活码';
COMMENT ON COLUMN orders.product_type IS '商品类型：SUBSCRIPTION_PLAN订阅计划/COURSE课程';
COMMENT ON COLUMN orders.product_id IS '商品ID，对应subscription_plans.id或courses.id';
COMMENT ON COLUMN orders.product_name IS '商品名称，冗余存储便于查询';
COMMENT ON COLUMN orders.order_type IS '订单类型：PURCHASE购买订单/GIFT赠送订单';
COMMENT ON COLUMN orders.amount IS '订单金额，赠送订单为0';
COMMENT ON COLUMN orders.activated_time IS 'CDK激活时间';
COMMENT ON COLUMN orders.remark IS '订单备注';
COMMENT ON COLUMN orders.create_time IS '记录创建时间';
COMMENT ON COLUMN orders.update_time IS '记录更新时间';
COMMENT ON COLUMN orders.deleted IS '逻辑删除标记';

-- 创建基础索引
CREATE INDEX idx_orders_order_no ON orders(order_no);
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_cdk_code ON orders(cdk_code);
CREATE INDEX idx_orders_product_type ON orders(product_type);
CREATE INDEX idx_orders_product_id ON orders(product_id);
CREATE INDEX idx_orders_order_type ON orders(order_type);
CREATE INDEX idx_orders_activated_time ON orders(activated_time);
CREATE INDEX idx_orders_amount ON orders(amount);
CREATE INDEX idx_orders_create_time ON orders(create_time);

-- 创建复合索引（基于查询场景优化）
CREATE INDEX idx_orders_user_order_type ON orders(user_id, order_type);
CREATE INDEX idx_orders_time_range ON orders(activated_time, order_type);
CREATE INDEX idx_orders_product_info ON orders(product_type, product_id);
CREATE INDEX idx_orders_user_time_type ON orders(user_id, activated_time, order_type);

-- 外键约束
ALTER TABLE orders
ADD CONSTRAINT fk_orders_user_id
FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE orders
ADD CONSTRAINT fk_orders_cdk_code
FOREIGN KEY (cdk_code) REFERENCES cdk_codes(code);

-- 唯一约束：一个CDK只能对应一个订单
ALTER TABLE orders
ADD CONSTRAINT uk_orders_cdk_code
UNIQUE (cdk_code);