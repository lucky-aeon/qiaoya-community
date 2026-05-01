ALTER TABLE users ADD COLUMN plus_guide_completed_at TIMESTAMP NULL;

-- 给已订阅过 Plus 套餐（level >= 2）的用户补数据，避免他们重新看到指引
UPDATE users SET plus_guide_completed_at = NOW()
WHERE id IN (
    SELECT DISTINCT us.user_id
    FROM user_subscriptions us
    JOIN subscription_plans sp ON us.subscription_plan_id = sp.id
    WHERE sp.level >= 2
);
