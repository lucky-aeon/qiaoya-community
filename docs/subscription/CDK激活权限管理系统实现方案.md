# CDK激活权限管理系统实现方案

作者：后端
最后更新：2025-09-27
状态：归档（演进背景）
适用范围：历史方案与设计考量，现以《课程权限体系技术文档.md》为权威

维护人：后端
权威文档：docs/课程权限体系技术文档.md

## 1. 问题背景与讨论过程

### 1.1 初始架构设想
最初考虑创建独立的权限领域，实现复杂的权限管理系统，包括：
- 复杂的`user_courses`表设计（包含source_type、source_order_id等字段）
- 独立的权限领域调用多个其他领域
- 复制套餐课程权限到用户权限表

### 1.2 架构问题发现
通过讨论发现了以下关键问题：

**问题1：权限复制模式的缺陷**
- 如果将套餐课程权限复制到用户权限表，管理员调整套餐课程配置后，已激活用户无法自动获得权限变更
- 权限与套餐脱钩，无法实现动态权限管理

**问题2：权限领域边界问题**
- 独立的权限领域需要调用套餐、订阅、课程等多个领域，违反DDD边界原则
- 架构复杂度过高

**问题3：数据模型过度设计**
- user_courses表的source_type等字段增加了不必要的复杂性
- 权限来源应该通过关系链查询，而不是冗余存储

### 1.3 最终架构决策
经过充分讨论，确定了以下设计原则：
- **权限分离**：静态权限（直接购买）+ 动态权限（套餐订阅）
- **关系链查询**：用户→套餐→课程的动态权限验证
- **应用层编排**：权限验证逻辑放在应用层服务，不创建独立权限领域
- **简化数据模型**：user_courses表只存储用户-课程关系，不记录来源

## 2. 最终架构设计

### 2.1 核心设计思路

#### 权限分离设计
1. **静态权限（user_courses表）**：用户直接拥有的课程权限（永久有效）
   - 课程CDK激活获得的权限
   - 管理员直接授权的权限

2. **动态权限（订阅关系）**：基于套餐订阅的临时权限
   - 通过 `user_subscriptions` + `subscription_plan_courses` 实时查询
   - 管理员调整套餐课程配置后用户权限自动跟随

#### 权限验证逻辑
```
用户访问课程权限验证：
├── 1. 查询静态权限（user_courses表）
│   └── 如果存在 → 允许访问（永久有效）
└── 2. 查询动态权限（用户有效订阅 + 套餐课程配置）
    ├── 查询用户有效订阅列表
    ├── 检查订阅套餐是否包含该课程
    └── 如果包含 → 允许访问（订阅期内有效）
```

### 2.2 数据模型设计

#### user_courses表（简化设计）
```sql
CREATE TABLE user_courses (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    course_id VARCHAR(36) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);
```

**设计要点**：
- 只存储用户-课程关系，不记录权限来源
- 通过唯一索引避免重复权限
- 所有权限均为永久有效

#### 现有表关系保持不变
- `user_subscriptions`：用户-套餐订阅关系（带有效期）
- `subscription_plan_courses`：套餐-课程配置关系（管理员可调整）

### 2.3 系统组件架构

#### 应用层权限服务
```
UserPermissionAppService（应用层编排服务）
├── hasAccessToCourse(userId, courseId) - 统一权限验证
├── grantCourseAccess(userId, courseId) - 授予课程权限
└── getDirectCoursesIds(userId) - 查询用户直接拥有的课程
```

#### 事件监听器
```
CourseCDKEventListener
├── 监听课程CDK激活事件
├── 验证课程存在性
└── 调用权限服务授予权限
```

## 3. CDK激活流程设计

### 3.1 套餐CDK激活流程
```
用户激活套餐CDK →
├── CDKDomainService.activateCDK() 发布CDKActivatedEvent
└── SubscriptionCDKEventListener 监听事件
    └── 创建user_subscriptions记录

权限验证时 →
├── 查询用户有效订阅
└── 实时查询套餐包含的课程（动态权限）
```

### 3.2 课程CDK激活流程
```
用户激活课程CDK →
├── CDKDomainService.activateCDK() 发布CDKActivatedEvent
└── CourseCDKEventListener 监听事件
    ├── 验证课程存在
    └── 调用UserPermissionAppService.grantCourseAccess()
        └── 创建user_courses记录（静态权限）
```

## 4. 已实现的核心组件

### 4.1 数据层
- ✅ `V14__Create_user_courses_table.sql` - 数据库迁移文件
- ✅ `UserCourseEntity` - 用户课程权限实体
- ✅ `UserCourseRepository` - 仓储接口

### 4.2 应用层
- ✅ `UserPermissionAppService` - 权限验证应用服务
  - `hasAccessToCourse()` - 统一权限验证逻辑
  - `grantCourseAccess()` - 授予课程权限
  - `getDirectCoursesIds()` - 查询用户直接权限

### 4.3 事件处理
- ✅ 完善的`CourseCDKEventListener` - 课程CDK激活事件监听
- ✅ 现有的`SubscriptionCDKEventListener` - 套餐CDK激活事件监听

## 5. 系统优势

### 5.1 架构优势
✅ **动态权限管理**：套餐课程调整后用户权限自动跟随  
✅ **清晰的权限来源**：区分静态权限和动态权限  
✅ **简化的数据模型**：避免过度设计的复杂性  
✅ **符合DDD原则**：权限验证作为应用层编排，不违反领域边界  

### 5.2 业务优势
✅ **灵活的管理方式**：支持套餐制和单课程购买两种模式  
✅ **统一的用户体验**：所有CDK激活流程一致  
✅ **完整的事件驱动**：充分利用现有CDK激活事件机制  
✅ **最小化代码改动**：在现有架构基础上扩展  

### 5.3 维护优势
✅ **逻辑集中**：权限验证逻辑统一在应用层服务中  
✅ **易于测试**：权限验证逻辑独立，便于单元测试  
✅ **易于扩展**：未来可以轻松添加新的权限类型和验证规则  

## 6. 使用示例

### 6.1 权限验证示例
```java
// 检查用户是否有课程访问权限
boolean hasAccess = userPermissionAppService.hasAccessToCourse(userId, courseId);
if (hasAccess) {
    // 允许访问课程
} else {
    // 拒绝访问
}
```

### 6.2 课程CDK激活示例
```java
// 用户激活课程CDK
userSubscriptionAppService.activateCDK(userId, cdkCode);
// 系统自动：
// 1. 验证CDK有效性
// 2. 发布CDK激活事件
// 3. 事件监听器验证课程并授予权限
```

## 7. 总结

本方案通过充分的讨论和设计，最终确定了一个既满足业务需求又符合架构原则的权限管理系统：

- **避免了权限复制的陷阱**，采用动态查询实现套餐权限管理
- **简化了数据模型设计**，去除了不必要的复杂字段  
- **将权限验证放在应用层**，避免了跨领域调用的架构问题
- **充分利用现有事件机制**，最小化代码改动
- **实现了完整的CDK激活权限流程**，支持套餐制和单课程购买两种模式

这个设计既解决了当前的业务需求，又为未来的功能扩展提供了坚实的基础。
