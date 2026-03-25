-- 为已有套餐回填 skills 市场菜单与权限

INSERT INTO subscription_plan_menus (id, subscription_plan_id, menu_id, create_time, update_time)
SELECT md5(spm.subscription_plan_id || ':MENU_DASHBOARD_SKILLS'),
       spm.subscription_plan_id,
       'MENU_DASHBOARD_SKILLS',
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
FROM subscription_plan_menus spm
WHERE spm.menu_id = 'MENU_DASHBOARD_HOME'
ON CONFLICT (subscription_plan_id, menu_id) DO NOTHING;

INSERT INTO subscription_plan_menus (id, subscription_plan_id, menu_id, create_time, update_time)
SELECT md5(spm.subscription_plan_id || ':MENU_USER_SKILLS'),
       spm.subscription_plan_id,
       'MENU_USER_SKILLS',
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
FROM subscription_plan_menus spm
WHERE spm.menu_id = 'MENU_USER_BACKEND'
ON CONFLICT (subscription_plan_id, menu_id) DO NOTHING;

INSERT INTO subscription_plan_permissions (id, subscription_plan_id, permission_code, create_time, update_time)
SELECT md5(spp.subscription_plan_id || ':SKILL_MY_LIST'),
       spp.subscription_plan_id,
       'SKILL_MY_LIST',
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
FROM subscription_plan_permissions spp
WHERE spp.permission_code = 'POST_LIST_SELF'
ON CONFLICT (subscription_plan_id, permission_code) DO NOTHING;

INSERT INTO subscription_plan_permissions (id, subscription_plan_id, permission_code, create_time, update_time)
SELECT md5(spp.subscription_plan_id || ':SKILL_MY_DETAIL'),
       spp.subscription_plan_id,
       'SKILL_MY_DETAIL',
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
FROM subscription_plan_permissions spp
WHERE spp.permission_code = 'POST_DETAIL_SELF'
ON CONFLICT (subscription_plan_id, permission_code) DO NOTHING;

INSERT INTO subscription_plan_permissions (id, subscription_plan_id, permission_code, create_time, update_time)
SELECT md5(spp.subscription_plan_id || ':SKILL_CREATE'),
       spp.subscription_plan_id,
       'SKILL_CREATE',
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
FROM subscription_plan_permissions spp
WHERE spp.permission_code = 'POST_CREATE'
ON CONFLICT (subscription_plan_id, permission_code) DO NOTHING;

INSERT INTO subscription_plan_permissions (id, subscription_plan_id, permission_code, create_time, update_time)
SELECT md5(spp.subscription_plan_id || ':SKILL_UPDATE'),
       spp.subscription_plan_id,
       'SKILL_UPDATE',
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
FROM subscription_plan_permissions spp
WHERE spp.permission_code = 'POST_EDIT_SELF'
ON CONFLICT (subscription_plan_id, permission_code) DO NOTHING;

INSERT INTO subscription_plan_permissions (id, subscription_plan_id, permission_code, create_time, update_time)
SELECT md5(spp.subscription_plan_id || ':SKILL_DELETE'),
       spp.subscription_plan_id,
       'SKILL_DELETE',
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
FROM subscription_plan_permissions spp
WHERE spp.permission_code = 'POST_DELETE_SELF'
ON CONFLICT (subscription_plan_id, permission_code) DO NOTHING;
