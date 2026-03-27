# Skills 市场后端设计

## 相关文档

- 前端：`qiaoya-community-frontend/docs/superpowers/specs/skills/2026-03-27-feat-skills-market-design.md`

## 背景

社区需要新增一个面向游客和登录用户的 Skills 市场。技能由用户自行上传，无审核、无草稿状态，创建后立即进入公开市场。用户中心需要管理自己上传的 Skills，支持新增、编辑、删除、查看自己的列表。

## 业务边界

Skill 只有四个业务字段：

- `name`
- `summary`
- `description`
- `githubUrl`

系统字段沿用基础实体：

- `id`
- `createTime`
- `updateTime`
- `deleted`

归属字段：

- `userId`

本次不做：

- 审核
- 草稿/发布状态
- 分类、标签、搜索、点赞、收藏
- 审核后台

## 分层设计

新增独立 `skill` 子域，遵守现有 DDD 分层：

- `application/skill`
- `domain/skill`
- `interfaces/skill`

建议的最小结构：

- `domain/skill/entity/SkillEntity.java`
- `domain/skill/repository/SkillRepository.java`
- `domain/skill/service/SkillDomainService.java`
- `application/skill/dto/SkillDTO.java`
- `application/skill/assembler/SkillAssembler.java`
- `application/skill/service/SkillAppService.java`
- `interfaces/skill/controller/PublicSkillController.java`
- `interfaces/skill/controller/UserSkillController.java`
- `interfaces/skill/request/CreateSkillRequest.java`
- `interfaces/skill/request/UpdateSkillRequest.java`
- `interfaces/skill/request/SkillQueryRequest.java`

## 数据库设计

新增 `skills` 表，字段建议如下：

- `id`
- `user_id`
- `name`
- `summary`
- `description`
- `github_url`
- `create_time`
- `update_time`
- `deleted`

索引建议：

- `idx_skills_user_id`
- `idx_skills_create_time`

排序规则：

- 公开市场默认按 `create_time desc`
- 我的列表默认按 `update_time desc`

## 接口设计

### 公开接口

- `GET /api/public/skills`
  - 返回分页列表
  - 供游客和登录用户市场页使用
  - 列表项应包含：`id`、`name`、`summary`、`githubUrl`、时间信息
  - 分页返回中的 `total` 直接作为“skills 收录数”

- `GET /api/public/skills/{id}`
  - 返回详情
  - 包含完整 Markdown 描述
  - 供详情弹窗按需拉取

公开接口不走 `/api/user/**` 鉴权拦截器，也不加 `@RequiresPlanPermissions`。

### 用户接口

- `GET /api/user/skills`
  - 查询我的 Skills 分页列表
- `GET /api/user/skills/{id}`
  - 查询我的单个 Skill 详情
- `POST /api/user/skills`
  - 创建 Skill
- `PUT /api/user/skills/{id}`
  - 编辑我的 Skill
- `DELETE /api/user/skills/{id}`
  - 删除我的 Skill

用户接口放在 `/api/user/**` 下，复用现有用户身份拦截器。首版不额外挂 `@RequiresPlanPermissions`，原因是：

- 技能市场本身对游客公开
- 该能力不应再被套餐菜单体系隐藏
- `/api/user/**` 已有登录、设备、有效订阅等基础校验

## 领域规则

- 创建成功后立即公开可见
- 只有作者本人可编辑、删除、查看自己的私有管理详情
- 删除采用软删除
- `githubUrl` 必须为合法 `http/https` URL
- `name`、`summary`、`description`、`githubUrl` 都不能为空

作者校验放在 `SkillDomainService`，不要在 Application 层直接依赖 Repository。

## 应用层职责

`SkillAppService` 负责：

- 编排公开列表与详情查询
- 编排我的列表与详情查询
- 调用 `SkillDomainService` 完成创建、更新、删除
- 将实体转换为 `SkillDTO`

`SkillAssembler` 负责实体与 DTO 转换，保持静态方法风格。

## 控制器校验

在 request DTO 中完成参数格式校验：

- `@NotBlank`：名称、简介、描述、GitHub URL
- `@Size`：名称与简介长度限制
- `@Pattern` 或自定义 URL 校验：限制为 `http/https`

控制器不承载业务规则，例如“只能修改自己的 Skill”，该逻辑应下沉到 Domain。

## 查询与返回策略

为减少公开列表负载，列表接口不返回完整 Markdown 描述，只返回卡片所需字段。详情弹窗使用详情接口单独拉取完整描述。

返回 DTO 建议至少包含：

- `id`
- `userId`（仅用户侧管理可用，公开侧可按需不回）
- `name`
- `summary`
- `description`
- `githubUrl`
- `createTime`
- `updateTime`

## 测试方案

后端至少覆盖以下验证：

- 创建 Skill 后可在公开列表中查询到
- 非作者更新或删除时被拒绝
- 公开详情可查询到 Markdown 描述
- 删除后不会出现在公开列表与我的列表中

优先补充应用层或领域层测试；若现有模块更依赖集成测试，也可补充控制器层集成测试。

## 风险与取舍

- `/api/public/**` 不经过 `UserContextInterceptor`，因此公开接口不能依赖 `UserContext`
- 若未来要增加分类、搜索或收藏，本设计仍可平滑扩展，不影响当前四字段模型
- 首版不做审核意味着公开内容质量完全依赖用户自约束，后续若需要平台治理，应新增状态字段和审核流，而不是在当前接口上打补丁
