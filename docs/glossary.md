# 术语表（Glossary）

统一关键术语口径，避免跨文档理解偏差。

## 权限与访问控制
- 直购（Direct Purchase）：用户直接获得某课程权限，通常永久有效；数据落在 `user_courses`。
- 套餐（Subscription Plan）：管理员配置的付费套餐；用户拥有套餐订阅期内可访问套餐所含课程；由 `user_subscriptions` + `subscription_plan_courses` 推导。
- 课程解锁（Unlocked）：用户对某课程具备访问权限（直购或套餐包含），在列表/详情中以 `unlocked` 标记体现。
- 功能权限码（PlanPermission）：接口级功能访问控制的字符串码（如 `RESOURCE_DOWNLOAD`），与课程解锁正交；由套餐绑定与拦截器校验。
- 菜单码（MenuCode）：前端菜单可见性控制的标识，与功能权限码解耦；同样由套餐绑定合并后下发。
- AccessLevel：访问级别枚举，常见 `USER`/`ADMIN`，用于 Domain 查询范围隔离与复用。

## 事件与订单
- CDK（兑换码）：支持套餐或课程两类兑换码；激活后发布事件驱动下游（订单记录、权限同步/订阅创建、标记已用）。
- 订单（Order）：以事件驱动生成的交易/激活记录，用于财务统计与审计。

## 关注（Follow）与“订阅”歧义
- 本项目“付费订阅”指 Subscription Plan；
- 文档《订阅功能设计文档》中的“订阅”实为“关注/Follow”功能，为避免混淆，统一称“关注（Follow）”。

## 分层（与职责）
- Application（应用层）：编排流程、事务、DTO/Assembler；仅调用 Domain Service。
- Domain（领域层）：核心业务规则与持久化；Service 调用 Repository；不做参数格式校验。
- Infrastructure（基础设施层）：技术实现（配置、缓存、OAuth 客户端、加密、邮件、OSS、监控等），任何层可使用。

## 状态口径
- 草案：仍在讨论中，方向未定。
- 设计稿：方案明确可落地，可能有细节待联调。
- 已落地：对应代码已实现并在演进维护。
- 归档：历史/对比方案，供参考，不再作为权威依据。

