-- 为 chat_rooms 新增受众字段（枚举：PAID_ONLY/FREE_ONLY/ALL_USERS）
-- 仅新增列，默认 NULL；业务读取时若为 NULL 将回退到旧的套餐判定逻辑

ALTER TABLE chat_rooms
ADD COLUMN IF NOT EXISTS audience VARCHAR(32);

COMMENT ON COLUMN chat_rooms.audience IS '房间受众：PAID_ONLY/FREE_ONLY/ALL_USERS；NULL 表示历史数据，按旧套餐绑定回退判定';

