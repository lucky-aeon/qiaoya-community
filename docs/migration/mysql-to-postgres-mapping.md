# 旧敲鸭 MySQL → 新敲鸭 PostgreSQL 数据迁移方案（首版）

> 目标：一次性将旧项目（只读）数据迁移至新项目数据库，并对齐新领域模型与字段规范；不考虑迁移窗口内的增量写入（旧库目前无写操作）。

## 概览
- 源库（旧）：MySQL `community@124.220.234.136`
- 目标库（新）：PostgreSQL `qiaoya_community@124.220.234.136`
- 策略：分域分批 + staging 中转 + 字段/枚举/主键映射 + 校验对账
- 主键：旧库多为自增 int，新库统一 `UUID (VARCHAR(36))` → 迁移时生成并维护“旧→新”映射
- 软删除：保留 `deleted_at`，新库已做全局忽略软删（查询时不应再额外加条件）

## 迁移范围与对照
- 用户与账号：`users`、`user_github_bindings` → `users`、`user_social_accounts`
- 内容与互动：`articles`、`comments`、`article_likes`、`qa_adoptions`（以及 `types`/`article_tags`/`article_tag_relations`）→ `posts`、`comments`、`post_likes`、`post_accepted_comments`、`categories`
- 课程与章节：`courses`、`courses_sections` → `courses`、`chapters`
- 订单与兑换：`orders`、`invite_codes`（以及可能的 `invite_relations`）→ `orders`、`cdk_codes`、（必要时）`user_subscriptions`
- 资源与绑定：`files` → `resources`、`resource_bindings`
- 更新日志：`update_logs` → `update_logs`（`update_log_changes` 如无来源可留空）
- 非核心域且新库无等价表：归档至 `staging.legacy_unmapped`（JSON），如 `chat_*`、`meetings*`、`sso_*`、`point_*`、`ai_*`、`message_*`、`share_*` 等

## 主键/外键与映射表
- 建议在 PG 新建辅助表：`migration.id_mappings`
  - 字段：`source_table`、`source_id`、`target_table`、`target_id`、`created_at`
  - 用途：保存旧表自增 ID 到新表 UUID 的映射，供后续外键回填
- 迁移顺序：先主表（如 `users`、`posts`、`courses`、`subscription_plans`），再从表（如 `comments`、`post_likes`、`chapters`、`user_social_accounts`、`post_accepted_comments`、`orders`）

## 表级字段映射（按域）

### 1) 用户与社交
- MySQL `users` → PG `users`
  - `id (int)` → `id (uuid)`：生成 UUID，入 `id_mappings`
  - `name (varchar)` → `name (varchar)`
  - `account (varchar)` → `email (varchar)`：按邮箱正则校验；不符合则落空并记录于 `extra/remark`（可人工处理）
  - `password (varchar)` → `password (varchar)`：原样迁移；如算法不同，可另增 `password_algo`（若新表未提供，可放弃记录，首登引导改密）
  - `desc (longtext)` → `description (text)`
  - `avatar (varchar)` → `avatar (varchar)`
  - `state (tinyint)` → `status (varchar)`：建议映射 `1→ACTIVE`，其它→`DISABLED`
  - `subscribe (tinyint)` → 不直接使用（以新域 `user_subscriptions` 管理），可在 `remark` 记录原值
  - `max_concurrent_devices (int)` → `max_concurrent_devices (int)`：保留旧值
  - `created_at`/`updated_at`/`deleted_at` → `create_time`/`update_time`/`deleted_at`（UTC）
  - 新字段默认：`email_notification_enabled=false`、`role='USER'`

- MySQL `user_github_bindings` → PG `user_social_accounts`
  - `id (int)` → `id (uuid)`
  - `user_id (int)` → `user_id (uuid)`：通过 `id_mappings(users)` 回填
  - 常量：`provider='GITHUB'`
  - `github_id (bigint)` → `open_id (varchar)`（转字符串）
  - `github_username (varchar)` → `login (varchar)`
  - `github_avatar (varchar)` → `avatar_url (varchar)`
  - `access_token (varchar)` → `access_token_enc (varchar)`（明文迁入或置空，后续再安全化）
  - `created_at/updated_at/deleted_at` → `create_time/update_time/deleted_at`

### 2) 内容与互动
- MySQL `articles` → PG `posts`
  - `id (int)` → `id (uuid)`
  - `title (varchar)` → `title (varchar)`
  - `content (longtext)` → `content (text)`
  - `user_id (int)` → `author_id (uuid)`：映射 `users`
  - `type (int)` → `category_id (uuid)`：先迁 `types → categories` 后映射
  - `state (int)` → `status (varchar)`：建议 `0→DRAFT`、`1→PUBLISHED`、其它→`ARCHIVED`
  - `abstract (varchar)` → `summary (varchar)`
  - `cover (varchar)` → `cover_image (varchar)`
  - `top_number (int)` → `is_top (boolean)`：`>0` 视为 `true`
  - `like (int)` → `like_count (int)`：优先用 `article_likes` 聚合重建；若缺失则回填原值
  - `created_at/updated_at/deleted_at` → `create_time/update_time/deleted_at`；`publish_time` 若 `status=PUBLISHED` 则取 `created_at`
  - `tags (json)`：由 `article_tag_relations` + `article_tags.tag_name` 聚合成字符串数组 JSON 落 `posts.tags`

- MySQL `comments` → PG `comments`
  - `id (int)` → `id (uuid)`
  - `parent_id (int)` → `parent_comment_id (uuid)`：通过 `id_mappings(comments)` 回填
  - `root_id (int)` → `root_comment_id (uuid)`：同上
  - `content (longtext)` → `content (text)`
  - `from_user_id (int)` → `comment_user_id (uuid)`；`to_user_id (int)` → `reply_user_id (uuid)`（如空保留空）
  - `business_id (int)` → `business_id (uuid)`：映射 `articles → posts`
  - 常量：`business_type='POST'`
  - `created_at/updated_at/deleted_at` → `create_time/update_time/deleted_at`

- MySQL `article_likes`/`reactions` → PG `post_likes`
  - 优先使用 `article_likes`：`article_id (int)`→`post_id (uuid)`，`user_id (int)`→`user_id (uuid)`
  - 若使用 `reactions`：仅迁 `reaction_type='LIKE'` 且 `business_type=文章` 的数据

- MySQL `qa_adoptions` → PG `post_accepted_comments`
  - `article_id (int)`→`post_id (uuid)`；`comment_id (int)`→`comment_id (uuid)`；时间字段对齐
  - 同时为对应 `posts` 设置 `resolve_status='RESOLVED'`，`solved_at=created_at`

- MySQL `types` → PG `categories`
  - `id (int)` → `id (uuid)`；`title (varchar)` → `name (varchar)`
  - `parent_id (int)` → `parent_id (uuid)`（0 视为 null）
  - `sort (int)` → `sort_order (int)`；`desc (varchar)` → `description (text)`
  - `state (tinyint)` → `is_active (boolean)`：`1→true` 其余 `false`
  - 常量：`type='ARTICLE'`、`level` 可按层级计算/默认 `1`

- MySQL `article_tags`/`article_tag_relations` → PG `posts.tags (json)`
  - 以 `article_tag_relations` 关联 `article_tags.tag_name` 聚合为字符串数组，写入 `posts.tags`

### 3) 课程与章节
- MySQL `courses` → PG `courses`
  - `id (int)` → `id (uuid)`
  - `title`、`desc→description (text)`、`user_id→author_id (uuid)`、`cover→cover_image`、`url→project_url`
  - `money (int)` → `price (numeric)`；`score (int)` → `rating (numeric)`（或保留为空）
  - `technology (varchar)` → `tech_stack (json)`（可按分隔符切分为数组）
  - `resources (json)` → `resources (json)` 原样迁移
  - `demo_url/custom_page_url` → `demo_url`（`custom_page_url` 无对应，放 `description` 备注或忽略）
  - `state (tinyint)` → `status (varchar)`：`1→PENDING`/`PUBLISHED`（按业务口径确认），默认 `PENDING`
  - `created_at/updated_at/deleted_at` → `create_time/update_time/deleted_at`

- MySQL `courses_sections` → PG `chapters`
  - `id (int)` → `id (uuid)`；`title`、`content` 对齐
  - `course_id (int)` → `course_id (uuid)`；`user_id (int)` → `author_id (uuid)`
  - `sort (int)` → `sort_order (int)`；`reading_time (int)` → `reading_time (int)`
  - `created_at/deleted_at` → `create_time/deleted_at`

### 4) 订单与兑换/会员
- MySQL `invite_codes` → PG `cdk_codes`
  - `id (int)` → `id (uuid)`；`code (varchar)` → `code`
  - `state (tinyint)` → `status (varchar)`：约定 `0→INACTIVE`、`1→ACTIVE`、`2→USED`（以实际值域为准）
  - `acquisition_type (tinyint)` → `acquisition_type (varchar)`：`1→PURCHASE`、`2→GIFT`…（需对照）
  - `member_id/creator` → 记录至 `remark` 或映射到 `used_by_user_id/created_by`（若确定含义）
  - `created_at/updated_at` → `create_time/update_time`
  - 若存在使用关系（见 `invite_relations`）：将 `invitee_id`（映射后）写入 `used_by_user_id`，`created_at` → `used_time`

- MySQL `orders` → PG `orders`
  - `id (int)` → `id (uuid)`；`invite_code (varchar)` → `cdk_code (varchar)`
  - `price (int)` → `amount (numeric)`（按分/元统一，建议转元保留两位小数）
  - `purchaser (int)` → `user_id (uuid)`；`created_at` → `create_time`
  - 其余新库字段：
    - `order_no` 生成规则：`LEGACY-{id}`
    - `order_type`：`CDK_REDEEM`（如订单仅为兑换码）
    - `product_type/product_id/product_name`：若无来源，置默认或留空（必要时放入 `extra`）
    - `activated_time`：可取 `create_time` 或留空
    - `extra (jsonb)`：保留旧行关键信息便于审计

- MySQL `subscriptions`（旧库语义不等同“会员”订阅，多为通知/事件订阅）
  - 暂不迁至 `user_subscriptions`，避免语义冲突；会员数据以 `cdk_codes` + `orders` + 新逻辑驱动

### 5) 资源与绑定
- MySQL `files` → PG `resources`
  - `id (int)` → `id (uuid)`；`file_key`、`size`、`format`、`user_id (int→uuid)` 对齐
  - `business_id (int)`：由于旧库无业务类型，无法判断绑定目标；建议仅落 `resources`
  - 如需绑定：按业务规则推断 `target_type/target_id` 生成为 `resource_bindings`；无法确认的入 `staging.legacy_unmapped`

### 6) 更新日志
- MySQL `update_logs` → PG `update_logs`
  - `title/description/content/version/type/status/publish_date/created_at/updated_at` 字段一一映射
  - 新库 `update_log_changes` 若无来源数据，暂留空

### 7) 不迁移/归档（示例）
- `chat_groups`、`chat_messages`、`meetings*`、`sso_*`、`point_*`、`ai_*`、`message_*`、`share_*`、`login_logs`、`oper_logs`、`user_online_session`、`user_points`、`user_tag_*` 等：
  - 直接导出为 JSON 行写入 `staging.legacy_unmapped(table_name, row_json, reason)`，便于后续运营或离线分析

## 执行步骤（一次性全量，无增量）
1. 目标库准备
   - 创建 `staging`、`migration` schema；建表 `migration.id_mappings`、`migration.migration_logs`、`staging.legacy_unmapped`
   - 开启相关表的外键与唯一索引“延迟启用”策略（或迁移后再创建索引）
2. 抽取（MySQL → CSV/Parquet）
   - 按域分批导出：`users`、`user_github_bindings`、`articles`、`comments`、`article_likes`、`qa_adoptions`、`types`、`article_tags`、`article_tag_relations`、`courses`、`courses_sections`、`invite_codes`、`invite_relations`、`orders`、`files`、`update_logs`
3. 装载至 staging（PG `COPY`）
   - 将原始数据导入 `staging.*` 临时表，保留原类型与命名
4. 转换与落库（staging → public）
   - 生成 UUID 与 `id_mappings`
   - 依顺序写入目标表（先主后从），过程中根据映射规则转换字段、枚举、时间与布尔
   - `posts.tags` 由标签关系聚合写入；`post_likes` 由 likes 源表生成；`post_accepted_comments` 按采纳表生成并同步设置帖子解决状态
5. 校验
   - 行数/唯一约束/外键完整性校验
   - 统计对账：分状态/分类/时间分布对比
   - 业务断言：如“每贴最多一个被采纳评论”
6. 切换
   - 应用连到新库；保留迁移日志与备份

## 校验清单（示例）
- users：源/目标行数一致；email 非空比例；状态分布
- posts：行数一致；每类状态数量；`like_count` 与 `post_likes` 聚合一致
- comments：根/子评论比例；孤儿评论（无父/无根）应为 0
- orders：行数一致；`amount` 总和；`cdk_code` 命中率
- categories：层级与 parent 映射正确；禁用数量
- courses/chapters：每课程章节数分布

## 回滚与审计
- 所有写入携带 `run_id`（时间戳或 UUID），失败可按 `run_id` 清理目标数据回滚
- `migration.migration_logs` 记录各表源/目标计数、校验摘要与耗时
- 关键生成值（UUID 映射）保存在 `migration.id_mappings`，支持幂等重放

## 工具与建议
- 推荐实现：自研轻量 ETL（Spring Boot CLI 或 Spring Batch）
  - MySQL 只读、PG 读写；Reader→Processor（映射/校验/UUID）→Writer（COPY/批量 INSERT/ON CONFLICT）
- 备选：`mysqldump/SELECT INTO OUTFILE` + PG `COPY FROM` + 一批转换 SQL 脚本
- 本仓库保留该方案文档与后续脚本（不在本次提交中实现代码）

## 附：核心表字段快照（摘录）
- 旧库 MySQL（部分）：
  - users(id,int), name, account, password, invite_code, desc, avatar, subscribe,tinyint, state,tinyint, max_concurrent_devices,int, created_at,updated_at,deleted_at
  - user_github_bindings(id,int), user_id,int, github_id,bigint, github_username, github_avatar, access_token, created_at,updated_at,deleted_at
  - articles(id,int), title, content,longtext, user_id,int, state,int, like,int, type,int, top_number,int, cover, abstract, created_at,updated_at,deleted_at
  - comments(id,int), parent_id,int, root_id,int, content, from_user_id,int, to_user_id,int, business_id,int, created_at,updated_at,deleted_at
  - article_likes(id,int), article_id,int, user_id,int
  - qa_adoptions(id,int), article_id,int, comment_id,int, created_at
  - courses(id,int), title, desc,longtext, technology, url, cover, money,int, state,tinyint, score,int, user_id,int, resources,json, demo_url, custom_page_url, created_at,updated_at,deleted_at
  - courses_sections(id,int), title, content,longtext, course_id,int, user_id,int, sort,int, reading_time,int, created_at, deleted_at
  - invite_codes(id,int), member_id,int, code, state,tinyint, acquisition_type,tinyint, creator,tinyint, created_at,updated_at
  - invite_relations(id,bigint), inviter_id,int, invitee_id,int, invite_code, created_at
  - orders(id,int), invite_code, price,int, purchaser,int, acquisition_type,tinyint, creator,int, created_at
  - files(id,int), file_key, size,bigint, format, user_id,int, business_id,int, created_at,updated_at
  - update_logs(id,bigint), title, description,text, content,longtext, version, type, status, publish_date, created_at, updated_at
- 新库 PG（部分）：
  - users(id,uuid), name, description,text, avatar, email, password, status, email_notification_enabled, max_concurrent_devices,int, role, create_time, update_time, deleted_at
  - user_social_accounts(id,uuid), user_id, provider, open_id, login, avatar_url, access_token_enc, refresh_token_enc, expires_at, create_time, update_time, deleted_at
  - posts(id,uuid), title, content,text, summary, cover_image, author_id, category_id, status, like_count, view_count, comment_count, is_top, publish_time, create_time, update_time, tags,json, resolve_status, solved_at, deleted_at
  - comments(id,uuid), parent_comment_id, root_comment_id, content,text, comment_user_id, reply_user_id, business_id, business_type, create_time, update_time, deleted_at
  - post_likes(id,uuid), post_id, user_id, create_time, update_time, deleted_at
  - post_accepted_comments(id,uuid), post_id, comment_id, create_time, update_time, deleted_at
  - categories(id,uuid), name, parent_id, type, level,int, sort_order,int, description,text, icon, is_active,bool, create_time, update_time, deleted_at
  - courses(id,uuid), title, description,text, tech_stack,json, project_url, tags,json, rating,numeric, status, author_id, total_reading_time,int, price, original_price, cover_image, demo_url, resources,json, deleted_at
  - chapters(id,uuid), title, content,text, course_id, author_id, sort_order,int, reading_time,int, create_time, update_time, deleted_at
  - cdk_codes(id,uuid), code, cdk_type, target_id, batch_id, status, used_by_user_id, used_time, create_time, update_time, acquisition_type, remark, deleted_at, price, subscription_strategy
  - orders(id,uuid), order_no, user_id, cdk_code, product_type, product_id, product_name, order_type, amount, activated_time, remark, create_time, update_time, deleted_at, extra,jsonb
  - resources(id,uuid), file_key, size,bigint, format, user_id, resource_type, original_name, create_time, update_time, deleted_at；resource_bindings(id,uuid), resource_id, target_type, target_id

---

如需我同步产出脚本模板（staging DDL、id 映射 DDL、样例转换 SQL、校验 SQL），请告知优先级，我可以在 `scripts/migration/` 下新增草稿。
