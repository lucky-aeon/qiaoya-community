# oper_logs → user_activity_logs 迁移与映射方案（基于 request_info）

本文档说明旧项目 MySQL 表 `oper_logs` 如何按 `request_info` 规则映射为新项目 PostgreSQL 表 `user_activity_logs`，并给出分期落地策略与实现要点。

## 背景与目标

- 旧系统通过中间件记录操作日志：`request_info = RequestURI(已解码)`，示例：
  - `/community/file/singUrl?fileKey=13/1741427453119`
  - `/community/articles/225`
  - `/community/courses/section/123`
  - `/community/knowledge/query?question=...`
- 新系统以统一的活动日志表记录：`user_activity_logs`，需要明确 `activity_type`、`target_type`/`target_id`、`request_path` 等字段
- 约束：尽量重用新项目已有的 `ActivityType`（src/main/java/org/xhy/community/domain/common/valueobject/ActivityType.java）

## 字段对照（oper_logs → user_activity_logs）

- `id` → `id`：使用稳定可幂等的派生ID，建议 `md5("oper|{oldId}|{createdAt}|{request_info}")`
- `user_id`(int) → `user_id`(varchar)：通过“邮箱”在新库 `users(email)` 反查新ID（落空允许为空）
- `request_method` → `request_method`：原值
- `request_info` → `request_path`：按规则映射；无法识别时回落为原始 `request_info`
- `ip` → `ip`：原值
- `user_agent` → `user_agent`：原值
- `platform` → `equipment`：去引号后保存（browser 暂置空）
- `exec_at`(string) → `execution_time_ms`(int)：支持 `µs/ms/s/数值` 解析；失败置空，并在 `context_data.exec_at_raw` 留痕
- `created_at` → `created_at`、`updated_at=created_at`
- `request_body/response_data`：不直接入库，择要信息放入 `context_data`

## 分期策略

第一期（已实现，安全、不丢关键价值）
- 资源下载：`/community/file/singUrl?fileKey=...`
  - `activity_type=RESOURCE_DOWNLOAD`
  - `target_type=RESOURCE`
  - `target_id`：通过 `resources(file_key→id)` 映射
  - `request_path`：规范化为 `/api/public/resource/{id}/access`
  - `context_data`：保留 `request_info`、`file_key_raw`、`file_key`、`resource_id`、`exec_at_raw`（如解析失败）

第二期（建议补充，覆盖高频浏览行为）
- 文章详情：`/community/articles/{id}`
  - `activity_type=VIEW_POST`
  - `target_type=POST`
  - `target_id`：与迁移后的 `posts.id` 一致（旧文章ID→新表字符串ID）
  - `request_path`：未知时回退 `request_info`，或使用 `/api/public/posts/{id}`（如后续公开接口落地）
- 章节访问：`/community/courses/section/{id}`
  - `activity_type=VIEW_COURSE`（或后续新增 `VIEW_CHAPTER` 更精确；当前先用 VIEW_COURSE）
  - `target_type=CHAPTER`
  - `target_id`：与迁移后的 `chapters.id` 一致（旧章节ID→新表字符串ID）
  - `request_path`：回退 `request_info`，或 `/api/public/courses/chapters/{id}`（若开放）
- AI 问答/搜索：`/community/knowledge/query?question=...`
  - `activity_type=SEARCH_CONTENT`
  - `target_type`/`target_id`：空
  - `request_path`：保留原 `request_info` 或归一 `/api/public/knowledge/query`
  - `context_data.question`：从查询参数提取并保存

第三期（按需扩展，覆盖管理台操作）
- 管理端前缀：`/community/admin/**`
  - 结合 HTTP 方法与资源路径映射至 `ActivityType` 的 ADMIN_* 类别，例如：
    - `DELETE /community/admin/user/{userId}/devices/{sessionId}` → `ADMIN_FORCE_LOGOUT`（target_type=`DEVICE_SESSION`，target_id=`{sessionId}`）
    - `PUT /community/admin/user/{id}/max-devices` → `ADMIN_UPDATE_USER`
    - `/community/admin/expressions` → `ADMIN_EXPRESSION_CREATE/UPDATE/DELETE/TOGGLE`（按方法和子路径）
    - `/community/admin/update-logs` → `ADMIN_UPDATE_LOG_*`
  - 未能一一确认的路径，先以 `ActivityType.ADMIN_UPDATE_CONFIG` 占位并保留足量 `context_data`

## 识别与解析规则（示例正则与步骤）

1) 资源下载
- 匹配：`^(/api)?/community/file/singUrl\?`，从 `fileKey` 取值（URLDecode），映射资源ID

2) 文章详情
- 匹配：`^/community/articles/(\d+)$`
- 提取 `{id}` 为目标ID

3) 章节访问
- 匹配：`^/community/courses/section/(\d+)$`
- 提取 `{id}` 为目标ID

4) AI 搜索
- 匹配：`^/community/knowledge/query(\?.*)?$`
- 从 QueryString 中解析 `question`

5) 管理端（建议用“前缀+方法”）
- 前缀：`^/community/admin/`
- 依据方法与尾部资源路由归类（可外置一张“pattern → ActivityType/target_type/extractor”配置表以便维护）

## 落地实现要点

- 迁移器入口：`src/main/java/org/xhy/community/tools/migration/OperLogsMigrator.java`
  - 已实现第一期：资源下载（`RESOURCE_DOWNLOAD`）
  - 建议新增：文章详情、章节访问、AI 搜索、部分 admin 操作
  - 插入：`INSERT ... ON CONFLICT(id) DO NOTHING`，支持 `BATCH_SIZE`、`DRY_RUN`
  - 用户反查：缓存 `email → user_id`，降低反复查库
  - exec_at：统一解析为毫秒

## 依赖前提

- 文章/章节已迁移完成，且沿用“旧ID作为新表字符串ID”的策略：
  - `articles.id` → `posts.id`（字符串）
  - `courses_sections.id` → `chapters.id`（字符串）
- 资源表已构建 `file_key → id` 的映射（`resources` 已导入）

## 回滚/幂等

- 幂等：`id` 为派生 hash，重复执行不产生重复记录
- 可选回滚：按时间范围或派生ID前缀（如 `md5("oper|...)`）删除

## 验证步骤（建议）

1. DRY_RUN 验证
- 环境：`DRY_RUN=true`、`BATCH_SIZE=1000`
- 重点观察统计：`skippedUnsupported`（未识别路径）、`skippedResMiss`（资源映射缺失）

2. 小批量实插
- 选择时间窗口（如近 7 天）或 ID 范围进行入库
- 对比：
  - 下载量：`RESOURCE_DOWNLOAD` 与旧系统“文件日志”分页一致性
  - 文章/章节浏览：抽样核验数量级与趋势

3. 全量入库
- 监控写入效率与 PG 连接数量
- 如需加速：调大 `BATCH_SIZE`（建议 1000～2000）

## 后续优化建议

- 丰富 admin 路由映射清单，沉淀为可维护的正则/模板配置
- 若业务需要更精细的章节浏览类型，考虑新增 `ActivityType.VIEW_CHAPTER` 并适配前端展示
- 给 user_activity_logs 增加必要索引（如 `activity_type`、`target_type,target_id` 组合）以支持分析

