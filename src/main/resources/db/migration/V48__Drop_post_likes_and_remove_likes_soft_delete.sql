-- 移除历史表 post_likes（已由通用 likes 表替代）
DROP TABLE IF EXISTS post_likes;

-- 点赞表不再使用软删除，迁移数据并移除相关结构

-- 1) 清理已软删除的数据（历史取消点赞记录），仅保留有效点赞
DELETE FROM likes WHERE deleted_at IS NOT NULL;

-- 2) 去重：对于同一 (user_id, target_type, target_id) 仅保留最新一条记录
DELETE FROM likes l
USING (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY user_id, target_type, target_id
               ORDER BY create_time DESC, id DESC
           ) AS rn
    FROM likes
    WHERE deleted_at IS NULL
) d
WHERE l.id = d.id AND d.rn > 1;

-- 3) 移除依赖 deleted_at 的唯一约束与索引
ALTER TABLE IF EXISTS likes DROP CONSTRAINT IF EXISTS unique_user_target;
DROP INDEX IF EXISTS idx_target;
DROP INDEX IF EXISTS idx_user;

-- 4) 移除列 deleted_at
ALTER TABLE likes DROP COLUMN IF EXISTS deleted_at;

-- 5) 重建唯一约束与查询索引（不含软删除列）
ALTER TABLE likes ADD CONSTRAINT unique_user_target UNIQUE (user_id, target_type, target_id);
CREATE INDEX idx_target ON likes(target_type, target_id);
CREATE INDEX idx_user ON likes(user_id);

-- 备注：本迁移会影响应用层依赖软删除的逻辑，需同步更新代码逻辑为“物理删除/插入”式点赞切换。

