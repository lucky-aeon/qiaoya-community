# GitHub 登录接入技术方案（DDD + MyBatis Plus）

## 1. 背景与目标
- 目标：接入 GitHub OAuth2 登录，支持首次登录创建账户、后续快捷登录，以及用户侧绑定/解绑；发放站内访问令牌。
- 约束：严格遵循本项目 DDD 分层、命名与 MyBatis Plus 使用规范；不编写自定义 SQL；使用 UUID 主键；遵守异常与枚举规范。

## 2. 范围与不做
- 本方案聚焦后端落地：模型设计、流程编排、组件划分、接口设计与安全策略。
- 若无持续调用 GitHub API 的业务，不长期保存第三方 access_token。

## 3. 架构对齐（DDD 分层）
- Application 层：编排流程、事务、Assembler；不做参数格式校验；只调用 Domain Service。
- Domain 层：核心业务规则（唯一性、绑定约束、状态校验）、数据持久化；不做格式校验；只调用 Repository。
- Infrastructure 层：GitHub OAuth 客户端、配置、令牌签发、state 生成与校验、（可选）敏感信息加密；任何层可使用。
- 禁止关系：Application 不能直接调 Repository；Infrastructure 不能调用 Domain；Domain 不能调用 Application。

## 4. 数据模型与迁移（PostgreSQL + Flyway）
### 4.1 新增三方账号表 user_social_accounts
- 目的：不污染 `users` 表，支持多 provider 扩展。
- 字段（均使用 `BaseEntity` 公共字段：`id`、`create_time`、`update_time`、`deleted`）：
  - `user_id`：站内用户 ID（FK → users.id，`VARCHAR(36)`）
  - `provider`：认证提供方（枚举 AuthProvider，持久化为字符串）
  - `open_id`：第三方用户唯一标识（GitHub `id`）
  - `login`：GitHub `login`
  - `avatar_url`：头像 URL（可选）
  - `access_token_enc`、`refresh_token_enc`、`expires_at`（可选，如需长期调用 GitHub API 则启用并加密存储）
- 约束与索引：
  - 唯一索引：`(provider, open_id)` 在 `deleted = false` 条件下唯一（部分唯一索引）
  - 普通索引：`user_id`

### 4.2 Flyway 迁移
- 路径：`src/main/resources/db/migration`
- 文件名：`VXX__create_user_social_accounts.sql`（提交时替换 `XX` 为版本号）
- 建表示例（PostgreSQL）：

```sql
create table if not exists user_social_accounts (
  id              varchar(36) primary key,
  user_id         varchar(36) not null,
  provider        varchar(32) not null,
  open_id         varchar(128) not null,
  login           varchar(128),
  avatar_url      varchar(512),
  access_token_enc   varchar(512),
  refresh_token_enc  varchar(512),
  expires_at      timestamp,
  create_time     timestamp not null default now(),
  update_time     timestamp not null default now(),
  deleted         boolean not null default false
);

create index if not exists idx_user_social_user_id on user_social_accounts (user_id);
create unique index if not exists uniq_user_social_provider_open
  on user_social_accounts (provider, open_id)
  where deleted = false;
```

## 5. 枚举与类型处理
- 新增枚举：`org.xhy.community.domain.common.valueobject.AuthProvider { GITHUB }`
- 将 `AuthProvider` 注册到 `MyBatisTypeHandlerConfig`（项目要求：枚举类型必须使用枚举传递与持久化）。

## 6. 系统配置设计（通过系统配置管理 OAuth）
- 新增类型：`org.xhy.community.domain.config.valueobject.SystemConfigType.OAUTH_GITHUB`（"GitHub OAuth 配置"）
- 新增配置模型：`org.xhy.community.domain.config.valueobject.GithubOAuthConfig`
  - 字段：
    - `clientId: String`（必填）
    - `clientSecret: String`（必填，保存于 system_configs.data，加密由基础设施层负责可选）
    - `redirectUri: String`（必填）
    - `scopes: List<String>`（默认 `["read:user","user:email"]`）
    - `authorizeBaseUri: String`（默认 `https://github.com/login/oauth/authorize`）
    - `tokenUri: String`（默认 `https://github.com/login/oauth/access_token`）
    - `userApi: String`（默认 `https://api.github.com/user`）
    - `emailApi: String`（默认 `https://api.github.com/user/emails`，如需）
    - `requireVerifiedEmailForMerge: boolean`（默认 `true`，仅在 GitHub 邮箱 verified=true 时触发合并）
    - `fetchEmailFromApi: boolean`（默认 `true`，当 `/user` 不返回邮箱时，调用 `/user/emails` 获取）
    - `updateUserProfileIfEmpty: boolean`（默认 `true`，合并时仅在站内字段为空时用 GitHub 的头像/昵称补全）
- Admin 配置管理：复用现有 `AdminSystemConfigController`
  - `GET /api/admin/system-configs/OAUTH_GITHUB`
  - `PUT /api/admin/system-configs/OAUTH_GITHUB`（body 为 `GithubOAuthConfig` JSON）
- AdminSystemConfigAppService 校验建议：
  - 校验 `clientId/clientSecret/redirectUri` 非空
  - 校验 `redirectUri` 为站点允许的域名范围
  - 如配置 `scopes`，仅允许白名单 scope
- App 层读取：`GithubAuthAppService` 从 `SystemConfigDomainService.getConfigData(SystemConfigType.OAUTH_GITHUB, GithubOAuthConfig.class)` 读取配置，严禁直接读 yml。

## 7. 关键组件设计
### 7.1 Domain 层
- Entity：`org.xhy.community.domain.auth.entity.UserSocialAccountEntity extends BaseEntity`
  - 字段：`userId`、`provider`（AuthProvider）、`openId`、`login`、`avatarUrl`、可选 token 字段
- Repository：`org.xhy.community.domain.auth.repository.UserSocialAccountRepository extends BaseMapper<UserSocialAccountEntity>`
- Service：`org.xhy.community.domain.auth.service.AuthDomainService`
  - 方法建议：
    - `UserEntity getOrCreateUserByGithub(OpenIdProfile profile)`
    - `void bindGithub(String userId, OpenIdProfile profile)`
    - `void unbindGithub(String userId)`
  - 业务规则：
    - `(provider, openId)` 在未软删数据上唯一，禁止一号多绑
    - 邮箱合并策略（已确认：邮箱相同需要合并）：
      - 若 GitHub 提供的邮箱可用（根据配置 `requireVerifiedEmailForMerge` 判定）且与站内某用户邮箱相同，则合并：为该用户创建社交绑定记录；禁止与其他用户重复绑定
      - 若无法获取可用邮箱（未返回或未验证），则创建新用户（或引导补全邮箱）
    - 用户状态校验：禁用用户不可登录/绑定
  - 查询不显式 `deleted=false`，遵循全局软删策略（配置已处理）
  - 合并补全：当 `updateUserProfileIfEmpty=true` 时，仅在站内字段为空时使用 GitHub 的 `login/avatar` 进行补全

### 7.2 Application 层
- DTO：`AuthorizeUrlDTO`（`url`、`state`）、`AuthDTO`（`token`、`UserDTO user`）
- Assembler：`org.xhy.community.application.auth.assembler.AuthAssembler`（静态方法，BeanUtils.copy）
- AppService：`org.xhy.community.application.auth.service.GithubAuthAppService`
  - `AuthorizeUrlDTO getAuthorizeUrl()`：生成 state + 授权地址
  - `AuthDTO handleCallback(GithubCallbackRequest request)`：校验 state → 换 token → 拉取用户信息 → Domain 处理 → 签发令牌
  - `void bind(String userId, GithubCallbackRequest request)`：绑定当前登录用户
  - `void unbind(String userId)`：解绑当前登录用户
  - 事务：`handleCallback` 与 `bind/unbind` 建议开启事务，保证绑定映射与用户写一致性

### 7.3 Infrastructure 层
- 配置：`infrastructure/config/oauth/GithubOAuthProperties`（`clientId`、`clientSecret`、`redirectUri`、`scopes`）
- OAuth 客户端：`infrastructure/oauth/GithubOAuthClient`
  - `Token exchangeCodeForToken(String code)`
  - `GithubUser fetchUserInfo(String accessToken)`（含 `id/login/name/avatar_url/email`）
- State 服务：`infrastructure/oauth/OAuthStateService`
  - 采用 HMAC+过期时间，无状态校验；或基于缓存存储
- 令牌服务：`infrastructure/security/TokenService`（签发站内 JWT/同构令牌）
- 加密：`infrastructure/crypto/TokenEncryptor`（如需保存第三方 token，使用 Spring Security Crypto 对称加密）

## 8. 业务流程
### 8.1 获取授权地址（前端跳转）
1) 生成短期 `state`（HMAC+过期） 2) 拼装 `https://github.com/login/oauth/authorize` 3) 返回 `AuthorizeUrlDTO`

### 8.2 回调登录（含邮箱合并）
1) 校验 `state`
2) 使用 `code` 兑换 `access_token`
3) 拉取用户信息：`/user`（必要时按配置调用 `/user/emails` 获取 verified 邮箱）
4) App 层转换 `OpenIdProfile { openId, login, name, avatarUrl, email(verified?) }`
5) 合并/创建：
   - 先查 `(provider, openId)` 绑定，存在则直接返回对应用户
   - 不存在时，若获取到可用邮箱：按邮箱查用户 → 命中则合并绑定；未命中则创建用户
6) `TokenService.issue(userId, roles)`
7) 组装 `AuthDTO` 返回（标记 `newUser` 或 `merged=true`）

### 8.3 绑定 / 解绑
- 绑定：登录态下复用授权流程，最终 `AuthDomainService.bindGithub(userId, profile)`
- 解绑：`AuthDomainService.unbindGithub(userId)`；若用户唯一登录方式为 GitHub，需额外校验与二次确认

## 9. 安全与合规
- CSRF/重放：严格校验 `state`；授权码一次性使用；`state` 有效期建议 ≤5 分钟
- 最小权限：`scopes` 仅 `read:user` 与 `user:email`（如确需邮箱）
- 回调校验：`redirectUri` 与后端配置一致
- 敏感信息：第三方 token 可选保存且需加密；日志脱敏；禁止在异常中回显敏感数据
- 限流：对生成 URL 与回调接口限流，防止撞库与滥用
- 审计：记录登录/绑定/解绑事件（时间、IP、UA）
  - 建议在发生“邮箱合并”时添加审计事件，包含合并来源（GitHub）、目标用户、邮箱、openId

## 10. 管理员能力（可选）
- 路由：`/api/admin/auth/...`；App 层命名 `AdminAuthAppService`
- 复用权限：`AccessLevel` 控制数据范围（ADMIN 可见全量，USER 仅限本人）
- 能力：
  - 分页查询三方绑定列表（避免 N+1：批量查询关联用户信息并在 Assembler 汇总）
  - 审计与强制解绑

## 11. 开发规范对齐点（关键）
- 命名：`*Entity`/`*AppService`/`*DomainService`/`*Repository`，Assembler 以 `Assembler` 结尾
- 依赖注入：优先构造器注入
- Assembler：静态方法 + `BeanUtils.copyProperties`
- Repository：仅继承 `BaseMapper<T>`，不写自定义 SQL，使用 `LambdaQueryWrapper`
- UUID：`@TableId(type = IdType.ASSIGN_UUID)`，`MetaObjectHandler` 自动填充
- 分页：若用分页，请求继承 `interfaces/common/request/PageRequest.java`
- 枚举：项目中涉及枚举类型的传递必须使用枚举类型，并在 `MyBatisTypeHandlerConfig` 注册
- 软删：查询不要显式 `.eq(deleted, false)`（全局已处理）
- 修改：API 传 `update` 请求对象 → Assembler 转成 `Entity` → Domain 修改 → 返回 DTO
- 提交信息：`feat/fix(模块): 消息`

## 12. 实施计划与任务清单
1) 数据库迁移：新增 `user_social_accounts` 表与索引（Flyway）
2) 系统配置：新增 `SystemConfigType.OAUTH_GITHUB` 与 `GithubOAuthConfig`；在 AdminSystemConfigAppService 增加校验
3) 基础设施：`GithubOAuthClient`、`OAuthStateService`、`TokenService`、`TokenEncryptor`
4) Domain：`UserSocialAccountEntity`、`UserSocialAccountRepository`、`AuthDomainService`（创建/绑定/解绑/规则）
5) Application：`GithubAuthAppService`、DTO、Assembler（从系统配置读取 OAuth 参数）
6) 管理员能力（可选）：`AdminAuthAppService` 与 DTO/Assembler
7) 测试与联调：模拟 GitHub，覆盖登录、重复绑定、邮箱冲突、禁用用户等用例
8) 竞态与幂等：通过唯一索引 `(provider, open_id)` 与用户邮箱唯一约束保证；在发生唯一约束冲突时进行一次查询-重试（idempotent）

## 13. 配置项（通过系统配置管理）
- `SystemConfigType.OAUTH_GITHUB` 对应的数据结构 `GithubOAuthConfig`（JSON），由 Admin 接口统一维护，不再读取 yml。

## 14. 接口设计（前台 / 用户管理后台 / 管理员后台）
说明：API 层负责参数格式校验（@Valid）、鉴权与路由；App 层编排业务；Domain 层做业务规则校验。

- 前台（未登录也可访问）
  - `GET /api/oauth/github/url`
    - 出参：`AuthorizeUrlDTO { String url; String state; Long expireAt; }`
  - `GET /api/oauth/github/callback?code=...&state=...`
    - 出参：`AuthDTO { String token; Long expireAt; UserDTO user; Boolean newUser; }`
    - 说明：如采用浏览器跳转回前端，也可在服务端完成登录后 302 到前端携带 one-time code（可选方案）。

- 用户管理后台（需登录）
  - `GET /api/user/oauth/github/status`
    - 出参：`UserSocialBindStatusDTO { boolean bound; String login; String avatarUrl; String provider; }`
  - `POST /api/user/oauth/github/bind`
    - 入参：`GithubCallbackRequest { String code; String state; }`
    - 出参：`Void` 或 `UserSocialBindStatusDTO`（推荐返回最新绑定信息）
  - `POST /api/user/oauth/github/unbind`
    - 出参：`Void`

- 管理员后台（需管理员权限）
  - `GET /api/admin/auth/social-accounts`
    - 入参：`AdminSocialAccountQueryRequest extends PageRequest { String userId; String provider; String login; LocalDateTime startTime; LocalDateTime endTime; }`
    - 出参：分页 `Page<AdminSocialAccountDTO>`
      - `AdminSocialAccountDTO { String id; String userId; String userEmail; String provider; String login; String avatarUrl; LocalDateTime createTime; }`
  - `GET /api/admin/auth/social-accounts/{id}` → `AdminSocialAccountDTO`
  - `POST /api/admin/auth/social-accounts/{id}/unbind` → `Void`

控制器建议：
- `org.xhy.community.interfaces.oauth.controller/PublicOAuthController`（前台）
- `org.xhy.community.interfaces.oauth.controller/UserOAuthController`（用户管理后台）
- `org.xhy.community.interfaces.oauth.controller/AdminOAuthController`（管理员后台）

与 App 层映射：
- `GithubAuthAppService.getAuthorizeUrl()` ↔ 前台 `GET /api/oauth/github/url`
- `GithubAuthAppService.handleCallback(GithubCallbackRequest)` ↔ 前台 `GET /api/oauth/github/callback`、用户绑定 `POST /api/user/oauth/github/bind`
- `GithubAuthAppService.unbindGithub(String userId)` ↔ 用户 `POST /api/user/oauth/github/unbind`
- `AdminAuthAppService`（可选）↔ 管理员后台三方账号查询/解绑

请求/响应对象：
- Request 位于 `interfaces/.../request`，若为分页查询需继承 `PageRequest`
- DTO 位于 `application/.../dto`，Assembler 静态方法完成转换

## 15. 测试策略
- 单元：
  - `OAuthStateService`：state 生成/过期/验签
  - `AuthDomainService`：唯一性约束、邮箱冲突、邮箱合并（verified 与非 verified 情况）、禁用用户、重复绑定/解绑
- 集成：
  - 模拟 GitHub token 交换与用户信息接口（WireMock/MockWebServer）
  - 全流程：获取 URL → 回调（含邮箱合并路径）→ 发令牌
- 性能：
  - 授权回调 QPS 有限，关键在索引与唯一性约束正确性

## 16. 风险与边界
- 邮箱合并策略需明确：是否允许根据邮箱自动绑定到既有用户（需确认邮箱可信度）
- 唯一登录方式风险：若仅 GitHub 登录，解绑需二次确认或先配置其他登录方式
- GitHub 可能不返回邮箱：需前端触发资料补全流程
- 第三方 token：若无调用需求不落库；若落库必须加密并设置过期与轮换策略

## 17. 验收标准
- 首次 GitHub 登录自动完成用户创建或合并策略，并返回站内令牌
- 已绑定用户可快捷登录；重复绑定/解绑路径有明确错误码与提示
- 软删与唯一性约束正确，数据一致性无异常
- 日志无敏感信息，state 校验与限流生效
- 所有代码遵循 DDD 分层、命名、Assembler 静态方法、枚举与 MyBatis Plus 规范
 - 邮箱一致的情况下发生“合并绑定”（默认要求 verified=true），数据与权限保持在原用户下

## 18. 错误码扩展（Auth 模块建议新增）
- `OAUTH_STATE_INVALID(32xx, "state 无效或过期")`
- `OAUTH_CODE_EXCHANGE_FAILED(32xx, "授权码兑换失败")`
- `OAUTH_USERINFO_FAILED(32xx, "获取第三方用户信息失败")`
- `OAUTH_ALREADY_BOUND(32xx, "该第三方账号已绑定其他用户")`
- `OAUTH_BIND_CONFLICT(32xx, "绑定冲突，请检查账号关系")`
- `USER_DISABLED(32xx, "用户已被禁用")`

## 19. 示例
- 系统配置示例（`SystemConfigType=OAUTH_GITHUB` → `GithubOAuthConfig` JSON）：
```json
{
  "clientId": "gh_xxx",
  "clientSecret": "***",
  "redirectUri": "https://your.domain.com/api/oauth/github/callback",
  "scopes": ["read:user", "user:email"],
  "authorizeBaseUri": "https://github.com/login/oauth/authorize",
  "tokenUri": "https://github.com/login/oauth/access_token",
  "userApi": "https://api.github.com/user",
  "emailApi": "https://api.github.com/user/emails",
  "requireVerifiedEmailForMerge": true,
  "fetchEmailFromApi": true,
  "updateUserProfileIfEmpty": true
}
```

- 回调登录响应示例：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "token": "jwt.xxx.yyy",
    "expireAt": 1727000000,
    "newUser": false,
    "user": { "id": "u_123", "email": "a@b.com", "name": "Alice" }
  }
}
```

## 20. 开放问题（需产品/安全确认）
- 邮箱命中既有用户时是否自动合并绑定？触发条件与回滚策略？
- 是否需要长期调用 GitHub API？若需要，token 存储周期与轮换策略？
- 站内令牌标准（JWT 载荷、有效期、刷新策略、角色/权限承载）？
- 解绑策略：是否强制存在至少一种可用登录方式？
