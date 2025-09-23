# 消息中心技术方案

## 1. 项目背景

### 1.1 需求概述
敲鸭社区项目需要实现消息中心功能，用于管理站内收到的消息，支持：
- 查看最新消息列表
- 消息已读/未读状态管理
- 前端消息通知红点数量显示
- 消息分页查询

### 1.2 现状分析
经过全面技术调研，项目中**已经具备完整的消息中心底层架构**：
- ✅ Domain层：`NotificationEntity`、`NotificationDomainService`等核心业务逻辑
- ✅ Application层：`NotificationAppService`、`NotificationDTO`等应用服务
- ✅ Infrastructure层：数据库表、索引、转换器等基础设施
- ❌ Interface层：缺少Controller提供REST API接口

## 2. 架构分析

### 2.1 现有架构优势

#### 2.1.1 Domain层设计（已完善）
**NotificationEntity**
```java
@TableName("notifications")
public class NotificationEntity extends BaseEntity {
    private String recipientId;        // 接收者ID
    private NotificationType type;     // 通知类型
    private ChannelType channelType;   // 渠道类型(IN_APP/EMAIL/SMS)
    private String title;              // 通知标题
    private String content;            // 通知内容
    private NotificationStatus status; // 通知状态(PENDING/SENT/READ/FAILED)
}
```

**NotificationDomainService核心功能**
- 📧 多渠道消息发送（站内、邮件、短信）
- 📋 分页查询用户消息列表
- 🔢 统计未读消息数量
- ✅ 单个/批量消息已读标记
- 🚀 批量消息发送处理

#### 2.1.2 数据库设计（已优化）
```sql
CREATE TABLE notifications (
    id VARCHAR(36) PRIMARY KEY,
    recipient_id VARCHAR(36) NOT NULL,
    type VARCHAR(50) NOT NULL,
    channel_type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    -- ... 其他字段
);

-- 高效索引设计
CREATE INDEX idx_notifications_recipient_channel ON notifications(recipient_id, channel_type);
CREATE INDEX idx_notifications_status ON notifications(status);
```

#### 2.1.3 支持的通知类型
```java
public enum NotificationType {
    NEW_FOLLOWER("新关注者"),
    FOLLOWED_USER_POST("关注用户发布新内容"),
    CDK_ACTIVATED("CDK激活成功"),
    SUBSCRIPTION_EXPIRED("订阅即将过期"),
    POST_COMMENT("文章被评论"),
    COURSE_COMMENT("课程被评论");
}
```

### 2.2 架构完整性评估

| 架构层级 | 实现状态 | 完整度 | 说明 |
|---------|---------|--------|------|
| Interface层 | ❌ 缺失 | 0% | 需要创建Controller |
| Application层 | ✅ 完整 | 100% | 应用服务、DTO、转换器齐全 |
| Domain层 | ✅ 完整 | 100% | 实体、领域服务、Repository完善 |
| Infrastructure层 | ✅ 完整 | 100% | 数据库表、索引、配置完备 |

## 3. 技术方案

### 3.1 方案概述
**推荐方案：最小化实现**
- 仅需创建`UserNotificationController`提供REST API
- 无需修改现有Domain、Application层代码
- 充分利用现有完善架构

### 3.2 API接口设计

#### 3.2.1 Controller结构
```java
@RestController
@RequestMapping("/api/user/notifications")
@Validated
public class UserNotificationController {

    private final NotificationAppService notificationAppService;

    public UserNotificationController(NotificationAppService notificationAppService) {
        this.notificationAppService = notificationAppService;
    }
}
```

#### 3.2.2 核心接口列表

| 接口 | 方法 | 路径 | 功能描述 |
|------|------|------|----------|
| 获取消息列表 | GET | `/api/user/notifications` | 分页查询用户消息 |
| 获取未读数量 | GET | `/api/user/notifications/unread-count` | 红点数字统计 |
| 标记已读 | PUT | `/api/user/notifications/{id}/read` | 单个消息已读 |
| 全部已读 | PUT | `/api/user/notifications/read-all` | 批量消息已读 |

#### 3.2.3 详细接口设计

**1. 获取消息列表**
```java
@GetMapping
public Result<IPage<NotificationDTO>> getNotifications(
    @RequestParam(defaultValue = "1") @Min(1) Integer pageNum,
    @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer pageSize,
    HttpServletRequest request
) {
    String userId = UserContextHolder.getCurrentUserId();
    IPage<NotificationDTO> notifications =
        notificationAppService.getUserNotifications(userId, pageNum, pageSize);
    return Result.success(notifications);
}
```

**响应示例**
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "records": [
            {
                "id": "550e8400-e29b-41d4-a716-446655440000",
                "recipientId": "user123",
                "type": "POST_COMMENT",
                "channelType": "IN_APP",
                "title": "您的文章收到新评论",
                "content": "用户张三评论了您的文章《技术分享》",
                "status": "SENT",
                "createTime": "2024-01-15T10:30:00",
                "updateTime": "2024-01-15T10:30:00"
            }
        ],
        "total": 25,
        "size": 20,
        "current": 1,
        "pages": 2
    }
}
```

**2. 获取未读数量**
```java
@GetMapping("/unread-count")
public Result<Long> getUnreadCount(HttpServletRequest request) {
    String userId = UserContextHolder.getCurrentUserId();
    Long count = notificationAppService.getUnreadNotificationCount(userId);
    return Result.success(count);
}
```

**3. 标记单个消息已读**
```java
@PutMapping("/{notificationId}/read")
public Result<Void> markAsRead(
    @PathVariable @NotBlank String notificationId,
    HttpServletRequest request
) {
    String userId = UserContextHolder.getCurrentUserId();
    notificationAppService.markNotificationAsRead(userId, notificationId);
    return Result.success();
}
```

**4. 标记全部消息已读**
```java
@PutMapping("/read-all")
public Result<Void> markAllAsRead(HttpServletRequest request) {
    String userId = UserContextHolder.getCurrentUserId();
    notificationAppService.markAllNotificationsAsRead(userId);
    return Result.success();
}
```

### 3.3 文件结构

```
src/main/java/org/xhy/community/interfaces/
└── notification/
    └── controller/
        └── UserNotificationController.java
```

## 4. 实施计划

### 4.1 开发任务清单

| 任务 | 工作量 | 优先级 | 备注 |
|------|--------|--------|------|
| 创建UserNotificationController | 0.5人天 | P0 | 核心功能 |
| 接口参数校验 | 0.2人天 | P0 | 数据安全 |
| 异常处理 | 0.2人天 | P1 | 用户体验 |
| 接口文档 | 0.3人天 | P1 | 开发协作 |
| 单元测试 | 0.5人天 | P1 | 质量保证 |

**总工作量预估：1.7人天**

### 4.2 开发步骤

1. **第一步：创建Controller基础结构**
   - 创建`UserNotificationController`类
   - 注入`NotificationAppService`依赖
   - 实现基础的CRUD接口

2. **第二步：完善参数校验**
   - 添加`@Validated`注解
   - 实现分页参数校验
   - 添加用户权限校验

3. **第三步：异常处理**
   - 统一异常处理
   - 错误码定义
   - 日志记录

4. **第四步：测试验证**
   - 单元测试
   - 集成测试
   - 接口文档生成

## 5. 技术细节

### 5.1 权限控制
- 使用`UserContextHolder.getCurrentUserId()`获取当前用户ID
- 确保用户只能访问自己的消息
- Domain层已实现用户隔离逻辑

### 5.2 性能优化
- **数据库查询优化**：现有索引支持高效查询
  ```sql
  -- 用户消息查询优化
  idx_notifications_recipient_channel(recipient_id, channel_type)
  -- 未读数量统计优化
  idx_notifications_status(status)
  ```

- **分页查询优化**：使用MyBatis Plus分页插件
- **缓存策略**：未读数量可考虑Redis缓存

### 5.3 错误处理
```java
// 统一异常处理示例
@ExceptionHandler(NotificationException.class)
public Result<Void> handleNotificationException(NotificationException e) {
    log.error("消息中心异常: {}", e.getMessage(), e);
    return Result.error(e.getErrorCode(), e.getMessage());
}
```

### 5.4 日志记录
```java
// 关键操作日志
log.info("用户查询消息列表: userId={}, pageNum={}, pageSize={}",
         userId, pageNum, pageSize);
log.info("用户标记消息已读: userId={}, notificationId={}",
         userId, notificationId);
```

## 6. 扩展性设计

### 6.1 未来功能扩展

1. **消息分类展示**
   - 按通知类型分组
   - 不同类型消息图标

2. **实时消息推送**
   - WebSocket集成
   - Server-Sent Events

3. **消息模板管理**
   - 动态消息模板
   - 多语言支持

4. **高级查询功能**
   - 按类型筛选
   - 按时间范围查询
   - 关键词搜索

### 6.2 性能扩展

1. **缓存优化**
   - Redis缓存未读数量
   - 消息列表缓存

2. **数据库优化**
   - 分库分表
   - 读写分离

3. **异步处理**
   - 消息队列
   - 批量处理优化

## 7. 风险评估

### 7.1 技术风险

| 风险项 | 风险等级 | 影响范围 | 应对措施 |
|--------|----------|----------|----------|
| API性能问题 | 低 | 用户体验 | 现有索引优化充分 |
| 并发访问 | 低 | 数据一致性 | Domain层已处理 |
| 数据量增长 | 中 | 查询性能 | 定期清理+分页限制 |

### 7.2 业务风险

| 风险项 | 风险等级 | 影响范围 | 应对措施 |
|--------|----------|----------|----------|
| 用户隐私 | 低 | 数据安全 | 用户隔离已实现 |
| 垃圾消息 | 中 | 用户体验 | 消息过滤机制 |
| 系统依赖 | 低 | 可用性 | 利用现有稳定架构 |

## 8. 总结

### 8.1 方案优势

1. **实施成本低**：仅需创建Controller，工作量约1.7人天
2. **技术风险小**：充分利用现有完善架构
3. **扩展性强**：为未来功能扩展留足空间
4. **性能优异**：数据库索引和查询逻辑已优化

### 8.2 核心价值

- **快速交付**：最小化开发投入，快速实现消息中心功能
- **架构复用**：充分利用现有投资，避免重复建设
- **质量保证**：基于已验证的Domain层逻辑，稳定性高
- **用户体验**：提供完整的消息管理功能，满足业务需求

### 8.3 实施建议

1. **立即开始**：技术方案成熟，可立即进入开发阶段
2. **分步实施**：先实现核心功能，后续迭代扩展功能
3. **充分测试**：虽然底层逻辑稳定，但API层需要充分测试
4. **文档完善**：及时更新接口文档，便于前端对接

---

**文档版本**：v1.0
**创建时间**：2024年9月23日
**作者**：Claude Code
**审核状态**：待审核