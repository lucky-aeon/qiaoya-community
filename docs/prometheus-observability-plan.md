# Prometheus 指标接入与打点方案（qiaoya-community）

## 1. 背景与目标
- 目标：建立“可用性、性能、业务健康度”三层可观测体系，覆盖 HTTP、错误、线程池、外部依赖与核心业务域（帖子、评论、通知、订阅/订单/CDK、资源/OSS）。
- 原则：分阶段落地、标签低基数、以 SLI/SLO 为导向，优先排查问题与容量规划相关指标。

## 2. 范围与优先级
- 阶段 1（基础开箱）
  - 接入 Actuator + Micrometer + Prometheus Registry，暴露 `/actuator/prometheus`。
  - 默认指标：`http.server.requests`、JVM、线程、GC、Hikari/JDBC、Redis/Lettuce、Spring Scheduler、Executor。
- 阶段 2（横切关键路径）
  - 全局异常统计、三大拦截器（用户上下文/权限/管理员）拒绝原因与路径模板。
  - 登录/注册链路成功率与时延。
- 阶段 3（资源/OSS）
  - STS 发放与下载跳转、OSS 回调处理、上传凭证 API。
- 阶段 4（领域业务）
  - 帖子/评论/通知、CDK/订单/订阅等关键业务动作的计数与耗时。
- 阶段 5（AOP 与调度/事件）
  - 用户/业务活动切面执行时延；定时任务与事件监听处理情况。

## 3. 基础设施接入（不涉及业务代码）
- 依赖：Spring Boot Actuator、Micrometer、micrometer-registry-prometheus。
- 端点：开启 `/actuator/prometheus`（建议仅内网或经网关鉴权访问）。
- 自动指标：
  - HTTP 请求：`http.server.requests`（方法、状态、路径模板维度）。
  - JVM/系统：内存、GC、线程、CPU、文件描述符等。
  - JDBC/Hikari：连接池大小、活跃连接、等待时间。
  - Redis/Lettuce：连接、命令速率、错误。
  - 线程池：建议对自定义执行器暴露 `executor` 指标（见 6.2）。

## 4. 打点清单（按模块/文件聚合）
以下均为“指标意图+标签建议”，不包含实现代码。路径用于定位落点。

### 4.1 HTTP 与全局错误
- 全局异常（计数）
  - 位置：`src/main/java/org/xhy/community/infrastructure/config/GlobalExceptionHandler.java`
  - 指标：`community_exception_total`
  - 标签：`exception`（类型名）、`http_status`、`error_code`（业务枚举码）
  - 价值：量化业务/系统/校验异常分布与变化趋势。

### 4.2 拦截器（鉴权/权限）
- 用户上下文拦截（401/403）
  - 位置：`interfaces/webconfig/UserContextInterceptor.java`
  - 指标：`community_access_denied_total`
  - 标签：`interceptor=user_context`、`reason`（missing_token/blacklisted_token/inactive_user/no_subscription）、`path_template`
- 功能权限拦截（403）
  - 位置：`interfaces/webconfig/PlanPermissionInterceptor.java`
  - 指标：`community_access_denied_total`
  - 标签：`interceptor=plan_permission`、`reason=no_permission`、`path_template`
- 管理员拦截（401/403）
  - 位置：`interfaces/webconfig/AdminAuthInterceptor.java`
  - 指标：`community_access_denied_total`
  - 标签：`interceptor=admin_auth`、`reason=not_admin|missing_token|invalid_token`、`path_template`

### 4.3 认证与会话
- 登录/注册接口
  - 位置：`interfaces/user/controller/AuthController.java`
  - 指标：`community_auth_attempts_total`（计数）、`community_auth_latency_seconds`（直方图）
  - 标签：`endpoint=login|register`、`outcome=success|failure`
- 设备/IP 并发控制
  - 位置：`domain/session/service/DeviceSessionDomainService.java`
  - 指标：
    - `community_device_session_denied_total`（banned/deny_new）
    - `community_device_session_evictions_total`（LRU 淘汰次数）
    - `community_device_lock_acquire_seconds`（锁获取时延直方图）
  - 标签：`reason=banned|deny_new`
- Token 黑名单
  - 位置：`domain/session/service/TokenBlacklistDomainService.java`
  - 指标：`community_token_blacklist_ops_total`
  - 标签：`operation=add|remove|check|add_user|remove_user`

### 4.4 线程池（Async 执行器）
- 位置：`infrastructure/config/AsyncConfig.java`
- 指标：使用 Micrometer `executor` 绑定，或自定义：
  - `community_executor_queue_size`（gauge）
  - `community_executor_active_threads`（gauge）
  - `community_executor_completed_tasks_total`（counter）
- 标签：`executor=userActivityLogExecutor|businessActivityExecutor|taskExecutor`

### 4.5 外部依赖
- GitHub OAuth 客户端
  - 位置：`infrastructure/oauth/GithubOAuthClient.java`
  - 指标：`community_external_call_seconds`（直方图）
  - 标签：`provider=github`、`operation=token|userinfo|emails`、`outcome=success|failure`
- 邮件发送（SMTP）
  - 位置：`infrastructure/email/impl/AliCloudEmailService.java`
  - 指标：`community_email_send_total`、`community_email_send_seconds`
  - 标签：`provider=smtp`、`outcome=success|failure`

### 4.6 资源/OSS 链路
- STS 发放/预签名/删除/存在性检查
  - 位置：`infrastructure/service/AliyunOssService.java`
  - 指标：`community_oss_ops_seconds`（直方图）
  - 标签：`action=sts_credentials|presign|delete|exists`、`outcome=success|failure`
- 上传凭证 API
  - 位置：`interfaces/resource/controller/ResourceController.java`
  - 指标：`community_upload_credentials_requests_total`
  - 标签：`content_type`
- OSS 回调处理
  - 位置：`interfaces/public_api/controller/PublicResourceController.java`
  - 指标：`community_oss_callback_total`
  - 标签：`outcome=ok|unauthorized|blacklisted|invalid_jwt`、`mime_type`
- 资源下载访问跳转
  - 位置：同上 `accessResource`
  - 指标：`community_resource_access_total`
  - 标签：`outcome=redirected|unauthorized|forbidden`

### 4.7 领域业务
- 帖子 Post
  - 位置：`domain/post/service/PostDomainService.java`
  - 指标：`community_post_ops_total`
  - 标签：`action=create|publish|unpublish|delete|accept|revoke`、`category_type`（若可得）
- 评论 Comment
  - 位置：`domain/comment/service/CommentDomainService.java`
  - 指标：`community_comment_ops_total`
  - 标签：`action=create|reply|delete`、`business_type=POST|COURSE|...`
- 通知 Notification
  - 位置：`domain/notification/service/NotificationDomainService.java`
  - 指标：
    - 单发：`community_notification_send_total`、`community_notification_send_seconds`
      - 标签：`channel=IN_APP|EMAIL`、`type`、`outcome`
    - 批量：`community_notification_batch_size`（gauge） 、`community_notification_batch_seconds`、`community_notification_batch_failed_total`
- CDK
  - 位置：`domain/cdk/service/CDKDomainService.java`
  - 指标：`community_cdk_ops_total`
  - 标签：`action=create_batch|activate`、`cdk_type`、`acquisition_type`、`outcome`
- 订单 Order
  - 位置：`domain/order/service/OrderDomainService.java`
  - 指标：`community_order_ops_total`
  - 标签：`action=create|query`、`order_type|product_type`（可选）
- 订阅 Subscription
  - 位置：`domain/subscription/service/SubscriptionDomainService.java`
  - 指标：`community_subscription_ops_total`
  - 标签：`action=create_from_cdk|create_system_gift|active_check`、`outcome`

### 4.8 AOP 活动日志
- 用户活动切面
  - 位置：`application/aspect/UserActivityLogAspect.java`
  - 指标：`community_user_activity_total`、`community_user_activity_seconds`
  - 标签：`activity_type`、`outcome=success|failure`
- 业务活动切面
  - 位置：`application/aspect/BusinessActivityLogAspect.java`
  - 指标：`community_business_activity_seconds`
  - 标签：`activity_type`、`method`、`path_template`

### 4.9 定时任务与事件监听
- 定时任务（订阅过期检查）
  - 位置：`application/subscription/service/SubscriptionExpiryCheckService.java`
  - 指标：`community_scheduled_runs_total`、`community_scheduled_run_seconds`
  - 标签：`task=subscription_expiry_check`、`outcome`
- 事件监听（通知相关）
  - 位置：`application/notification/listener/*`
  - 指标：`community_event_handle_seconds`、`community_event_handle_total`
  - 标签：`event_type=comment_created|user_followed|content_updated|subscription_expiring|cdk_activated`、`outcome`

### 4.10 权限码扫描
- 启动时发现的权限码数量
  - 位置：`infrastructure/permission/PermissionCodeScanner.java`
  - 指标：`community_permission_codes_gauge`
  - 标签：无或 `module=all`

## 5. 标签设计与基数控制
- 一律避免高基数：不要使用用户 ID、资源 ID、原始 URL、邮箱等。
- URL 统一使用 `path_template`（如 `/api/user/resource/upload-credentials`）。
- 通用标签集合（按需选用）：
  - `method`、`path_template`、`status|outcome`、`reason`、`error_code`、`activity_type`、`channel`、`provider`、`action`、`executor`。

## 6. 指标命名规范与桶建议
- 命名前缀：`community_`（Prometheus 下划线风格）。
- 计数器：`*_total`；直方图：`*_seconds`；仪表：`*_gauge`。
- 关键时延桶（示例）：
  - 认证/拦截：`{0.025, 0.05, 0.1, 0.2, 0.5, 1, 2}` 秒
  - 外部调用/STS：`{0.05, 0.1, 0.2, 0.5, 1, 2, 5}` 秒
  - 业务活动：`{0.02, 0.05, 0.1, 0.2, 0.5, 1}` 秒

## 7. Grafana 大盘建议
- 概览：请求量/错误率/P95&P99、线程池活跃度、DB/Redis 健康。
- 安全与访问控制：401/403 按原因堆叠、登录成功率、黑名单命中率。
- 资源链路：STS 成功率与时延、回调授权失败率、下载跳转成功率。
- 领域业务：帖子/评论/通知/订阅等关键动作趋势与失败占比。
- 外部依赖：GitHub/SMTP 成功率与时延分布。

## 8. 落地步骤（逐步合入）
1) 引入 Actuator + Prometheus Registry，开放 `/actuator/prometheus`，在测试环境验证基础指标。
2) 接入线程池 `executor` 指标，并添加全局异常计数（按 error_code）。
3) 为三大拦截器与登录/注册增加计数/时延（仅低基数标签）。
4) 资源/OSS 链路打点：STS、回调、访问跳转、上传凭证。
5) 领域动作计数：帖子/评论/通知（单发+批量）/CDK/订单/订阅。
6) AOP 活动直方图与事件/定时任务打点。
7) 建立 Grafana 大盘与报警规则（如登录成功率低于阈值、STS 失败率突增等）。

## 9. 风险与注意事项
- 标签爆炸：务必用 `path_template` 替代原始 URL，杜绝 ID/邮箱等高基数字段。
- 性能影响：时延直方图只放在关键路径（例如认证、STS、外部依赖、AOP 活动）。
- 指标稳定性：命名一旦发布尽量保持兼容，必要时通过新指标过渡。
- 安全：`/actuator/prometheus` 建议内网/网关保护；避免在指标中泄露敏感信息。

## 10. 未决问题（待确认）
- 本期优先 SLI/SLO：更关注“可靠性（错误率/时延）”还是“业务增长（帖子/评论/订阅）”？
- 是否需要按环境（dev/staging/prod）添加统一 label？（建议由 Prometheus job 注入）
- 是否需要统一的 `service`/`module` label 用于多服务拆分后的对齐？

---
如需，我可以补充每个落点的“微改动位置与示例命名”对照表，先评审命名与标签后再实施。

