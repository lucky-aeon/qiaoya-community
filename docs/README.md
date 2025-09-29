# 项目文档总览与索引（qiaoya-community）

说明：本索引帮助快速定位各类技术文档，并统一术语与文档状态口径。若遇到术语歧义或跨文档重复，请先查阅术语表与状态说明。

## 文档状态说明
- 草案：仍在讨论中，方向未最终确定
- 设计稿：方案清晰，可落地，可能有边角需联调
- 已落地：代码已实现并在演进维护
- 归档：历史方案/背景沉淀，不再作为当前权威

## 术语与名词
- 建议先阅读：docs/glossary.md（关键术语与口径对齐）

## 目录索引

### 架构与规范（横切）
- 后端架构总览（推广/总览）：docs/architecture.md
- DDD 分层规范：当前在项目说明与团队约定中维护（后续可沉淀到 docs/architecture/ddd-guidelines.md）

### 课程（Course）
- 课程权限体系技术文档（已落地）
  - 文件：docs/course/课程权限体系技术文档.md
  - 摘要：统一“课程解锁”判定（直购/套餐）、列表 unlocked 标记、详情/章节访问校验、事件链路（CDK/订单/订阅）。

- 课程资源绑定设计方案（设计稿）
  - 文件：docs/course/课程资源绑定设计方案.md
  - 摘要：资源与课程/章节建立绑定；访问口校验“课程解锁”；章节更新时解析内容同步绑定。

- 课程访问控制（套餐绑定）设计方案（草案/历史）
  - 文件：docs/course/课程访问控制-套餐绑定设计方案.md
  - 摘要：早期按“课程被绑定则受控、未绑定则免费”的访问控制思路，现由“课程权限体系技术文档”收敛覆盖，保留供参考。

### 套餐/订阅（Subscription）
- 付费社区权限系统技术文档（背景/架构）
  - 文件：docs/subscription/付费社区权限系统技术文档.md
- CDK 激活权限管理系统实现方案（历史/演进背景）
  - 文件：docs/subscription/CDK激活权限管理系统实现方案.md
- 套餐绑定接口权限与菜单技术方案（前台）（草案，可落地）
  - 文件：docs/subscription/套餐绑定接口权限与菜单技术方案-前台.md

### 订单（Order）
- 订单模块技术设计方案（方案）
  - 文件：docs/order/订单模块技术设计方案.md

### 通知与消息（Notification & Message）
- 消息中心技术方案（方案）
  - 文件：docs/notification/消息中心技术方案.md
- 基于事件驱动的通知系统设计（方案）
  - 文件：docs/notification/基于事件驱动的通知系统设计.md

### 关注（Follow）
- 关注功能（Follow）设计文档（方案）
  - 文件：docs/follow/关注功能设计文档.md

### 活动日志（Activity Log）
- 用户活动日志系统扩展设计文档（方案）
  - 文件：docs/activity/用户活动日志系统扩展设计文档.md

### 集成（Integration）
- GitHub 登录接入技术方案（方案）
  - 文件：docs/integration/GitHub登录接入技术方案.md
- 邮件服务配置指南（指南）
  - 文件：docs/integration/邮件服务配置指南.md

### 安全（Security）
- 基于 IP 的登录设备限制技术方案（方案）
  - 文件：docs/security/基于IP的登录设备限制技术方案.md

### 运维（Ops）
 - CI/CD（GitHub Actions + SSH + Docker）
  - 文件：docs/deployment/后端CI-CD技术方案-GitHub-Actions-SSH-Docker.md
 - 发布可靠性与回滚策略（蓝绿/金丝雀/候选容器预检）
  - 文件：docs/deployment/发布可靠性与回滚策略-蓝绿-金丝雀-候选容器预检.md
 - Nginx/Traefik 蓝绿切流运维操作手册
  - 文件：docs/deployment/Nginx-Traefik-蓝绿切流-运维操作手册.md
 - CI 容器级冒烟测试：复用步骤与示例
  - 文件：docs/deployment/CI-容器级冒烟测试-复用步骤与示例.md
 - CI 构建提速方案（国内仓库 + 缓存 + 自托管 Runner）
  - 文件：docs/deployment/CI构建提速方案-国内仓库与自托管Runner.md
- 在现有工作流中插入冒烟并分离部署（操作清单）
  - 文件：docs/deployment/CI-在现有工作流中插入冒烟与分离部署-操作清单.md

### 可观测性（Observability）
- Prometheus 指标接入与打点方案
  - 文件：docs/observability/Prometheus指标接入与打点方案.md

## 命名与贡献（约定）
- 文件命名：暂保留现状；后续建议统一中文文件名（短横线连接）或统一英文 kebab-case（二选一）。
- 元数据：标题下建议补齐“状态/最后更新/适用范围/关键代码路径”。
- 链接规范：统一使用相对路径，避免断链；历史方案在文首标明“归档/历史”。

## 状态矩阵（速览）
- 已落地：课程权限体系技术文档
- 设计稿（可落地/分阶段）：课程资源绑定方案、套餐绑定权限与菜单、订单模块、消息中心、活动日志扩展、对象化通知、GitHub 登录、邮件指南、IP 设备限制、Prometheus 指标、CI/CD
- 归档（历史）：课程访问控制（套餐绑定）草案、CDK激活权限方案（演进背景）
