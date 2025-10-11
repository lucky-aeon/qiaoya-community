# 评论 AI 总结技术方案（Spring AI，对应 DDD 落地）

状态：设计稿（先行落地文档与骨架，不含接口实现）

## 1. 背景与目标

- 背景：当某篇文章的评论条数超过阈值（> 3）后，自动对“文章内容 + 当前所有评论内容”进行 AI 摘要；后续若有新评论，再次异步触发重算，保持摘要新鲜度。
- 目标：
  - 触发条件明确、异步执行、与主流程解耦；
  - 复用现有 DDD 分层，不破坏依赖规则；
  - 封装 Spring AI 调用，便于后续更换模型/厂商；
  - 保障并发、幂等、可观测与可回溯。

不在本期范围：对外 API（Controller）与前端展示，仅落地领域与应用内的“生成与存储”。

## 2. 需求细化

- 触发时机：
  - 当一条“针对文章（BusinessType.POST）”的评论创建成功后触发检查；
  - 当评论总数从 ≤3 变为 >3，或已有摘要但评论数发生增长时，应异步触发生成。
- 生成内容：结合文章标题/正文与（受限数量的）评论，输出简洁要点摘要（中文）。
- 更新策略：每次生成替换同文章的最新摘要；不在数据库保存模型/厂商/用量/状态等元信息。
- 失败处理：不影响主流程；可重试；保留错误信息便于排查。

## 3. 总体设计（DDD + 事件驱动 + Spring AI）

### 3.1 分层与依赖约束对齐

- Application层：编排流程（计数判断 → 取数 → 调 AI → 入库），事务管理；
- Domain层：聚焦核心规则与持久化（实体/仓储/跨实体规则）；
- Infrastructure层：技术实现（Spring AI 客户端、事件监听、分布式锁、配置）；

调用关系遵循规范：
- Application → DomainService；
- 任何层可用 Infrastructure 的通用能力；
- 事件监听器位于 Infrastructure，可调用 Application Service 进行业务编排（Infra → App 允许，Infra 禁止直连 Domain）。

### 3.2 异步触发

- 复用现有领域事件：`ContentPublishedEvent`（已在`CommentDomainService.createComment`中发布）。
  - 监听侧（Infrastructure）过滤 `contentType == COMMENT` 的事件。
  - 监听器查询评论详情，判定是否为“文章评论”；若是则委派给应用服务处理。
- 监听使用 `@Async` 执行，避免阻塞主流程；必要时可用 `@TransactionalEventListener(phase = AFTER_COMMIT)` 确保在提交后触发（如未来引入 App 层事务）。

### 3.3 去重与并发控制

- 针对同一 `postId`，在生成期间通过分布式锁（如 Redis）防重：`lock:post:summary:{postId}`；
- 监听到新评论时若已有生成在进行，则直接跳过；生成完成后进行“双检”：
  - 若“当前评论数”> “本次摘要对应评论数”，则再次异步入队，确保最终一致性。

### 3.4 幂等与重试

- 以 `postId` 作为天然幂等键：同一次生成只会最终写入 1 条最新记录（`post_id` 唯一约束）。
- 失败时记录 `error_message` 和 `status=FAILED`，由定时任务/人工重试或再次评论触发恢复。

### 3.5 观测与开关

- 增加日志与统计（失败重试次数、时延，可后续对接 Prometheus 指标）；
- 配置开关：`community.features.comment-summary.enabled=true/false`；
- 模型服务商与模型名通过环境变量提供，不落库。

## 4. 模块与代码落地（拟新增/改动）

目录遵循项目 DDD 规范（仅列关键骨架与方法签名）：

1) Application 层（新建 summary 子域）
- `org/xhy/community/application/summary/service/PostSummaryAppService.java`
  - `void handleCommentCreated(String commentId)`：由监听器调用，加载评论 → 判定是否文章 → 进行阈值检查与触发生成
  - `void generateAndSaveIfNeeded(String postId)`：阈值>3 时生成并保存；带防重与“双检”
- `org/xhy/community/application/summary/dto/PostSummaryDTO.java`
- `org/xhy/community/application/summary/assembler/PostSummaryAssembler.java`（静态方法 + BeanUtils.copy）

2) Domain 层（新建 summary 子域）
- `org/xhy/community/domain/summary/entity/PostSummaryEntity.java`（继承 BaseEntity）
  - 字段：`postId`、`summary`（仅此两类业务字段，遵循“只保存摘要内容”的约束）
- `org/xhy/community/domain/summary/repository/PostSummaryRepository.java`（extends BaseMapper<PostSummaryEntity>）
- `org/xhy/community/domain/summary/service/PostSummaryDomainService.java`
  - `PostSummaryEntity upsert(PostSummaryEntity entity)`（`post_id` 唯一约束实现幂等）
  - `PostSummaryEntity getByPostId(String postId)`

3) Infrastructure 层
- AI 客户端封装：`org/xhy/community/infrastructure/ai/CommentSummaryAIClient.java`
  - 依赖 Spring AI `ChatClient`；方法：`CommentSummaryResult summarize(PostEntity post, List<CommentEntity> comments)`（模型与厂商从环境变量读取）
- 事件监听：`org/xhy/community/infrastructure/event/CommentSummaryEventListener.java`
  - 监听 `ContentPublishedEvent`，筛选评论事件后调用 `PostSummaryAppService.handleCommentCreated`；`@Async`
- 分布式锁抽象（可复用既有 Lock 能力，若无则新增简单接口）：`org/xhy/community/infrastructure/lock/LockService.java` + `RedisLockService` 实现
- 类型转换注册：在 `MyBatisTypeHandlerConfig` 注册 `SummaryStatusConverter`
- Spring AI 配置：`org/xhy/community/infrastructure/config/SpringAIConfig.java`（如需定制 ChatClient/超时/模型名）

4) 复用现有 Domain 能力
- `CommentDomainService`：
  - `getCommentById(String)`、`getCommentCountByBusiness(String, BusinessType)`
- `PostDomainService`：
  - `getPostById(String)` 或至少提供 `getPostTitleMapByIds(Set<String>)` + 正文获取接口

## 5. 数据模型（Flyway）

迁移文件：`src/main/resources/db/migration/VXX__create_post_summary_table.sql`

```sql
CREATE TABLE IF NOT EXISTS post_summary (
    id VARCHAR(36) PRIMARY KEY,
    post_id VARCHAR(36) NOT NULL UNIQUE,
    summary TEXT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE post_summary IS '文章评论AI摘要（仅保存摘要内容）';
COMMENT ON COLUMN post_summary.post_id IS '文章ID（唯一）';
```

注意：
- 仍使用 `BaseEntity` 规范的字段（id/createTime/updateTime/deleted）；
- `post_id` 唯一约束保证每篇文章只有一条最新摘要记录；
- 索引遵循“非高频字段不建索引”，此处仅 `post_id` 唯一键即可。

## 6. Spring AI 接入与 Prompt 设计

### 6.1 依赖与配置（环境变量驱动）

Maven 依赖（示例，选择其一或并存按需启用）：

```xml
<dependency>
  <groupId>org.springframework.ai</groupId>
  <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
  <version>${spring.ai.version}</version>
</dependency>
<!-- 可选：Azure OpenAI -->
<dependency>
  <groupId>org.springframework.ai</groupId>
  <artifactId>spring-ai-azure-openai-spring-boot-starter</artifactId>
  <version>${spring.ai.version}</version>
</dependency>
<!-- 可选：本地/自建推理（Ollama） -->
<dependency>
  <groupId>org.springframework.ai</groupId>
  <artifactId>spring-ai-ollama-spring-boot-starter</artifactId>
  <version>${spring.ai.version}</version>
</dependency>
```

应用配置（示例，可扩展不同厂商）：

```yaml
community:
  features:
    comment-summary:
      enabled: true
      min-comment-threshold: 3
      max-post-chars: 2000
      max-comment-count: 50

spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:}
      base-url: ${OPENAI_BASE_URL:} # 可选，兼容代理/第三方通用OpenAI接口
      chat:
        options:
          model: ${COMMENT_SUMMARY_MODEL:gpt-4o-mini}
          temperature: 0.2
          max-tokens: 512
    # Azure OpenAI 示例（如启用）
    azure:
      openai:
        api-key: ${AZURE_OPENAI_API_KEY:}
        endpoint: ${AZURE_OPENAI_ENDPOINT:}
        chat:
          options:
            # Azure 使用部署名
            model: ${COMMENT_SUMMARY_MODEL:my-gpt4o-deployment}
```

需要设置的环境变量（按所选厂商）：
- OpenAI/兼容厂商：`OPENAI_API_KEY`、可选 `OPENAI_BASE_URL`、`COMMENT_SUMMARY_MODEL`
- Azure OpenAI：`AZURE_OPENAI_API_KEY`、`AZURE_OPENAI_ENDPOINT`、`COMMENT_SUMMARY_MODEL`

### 6.2 Prompt 结构（示例）

- system：你是资深社区编辑，请基于“文章标题/正文 + 评论”输出简洁要点摘要，避免重复与无关内容；中文输出；给出 3-5 条要点。
- user：
  - 文章标题：...
  - 文章正文（截断至 `max-post-chars`）
  - 评论列表（按时间/点赞排序，最多 `max-comment-count` 条）

## 7. 关键流程与时序

1) 用户创建评论（文章业务）：`CommentDomainService.createComment` 持久化并发布 `ContentPublishedEvent`；
2) 基础设施监听器捕获事件（仅处理 `COMMENT`）：`CommentSummaryEventListener`；
3) 监听器调用 `PostSummaryAppService.handleCommentCreated(commentId)`；
4) 应用服务加载评论，判定 `BusinessType.POST` 且评论数>阈值；
5) 尝试获取分布式锁（按 `postId`）；若已在生成中则直接返回；
6) 从 `PostDomainService` 与 `CommentDomainService` 取文章与评论，构建 Prompt；
7) 调用 `CommentSummaryAIClient.summarize(...)` 得到摘要文本；
8) 通过 `PostSummaryDomainService.upsert(...)` 写入或更新摘要；
9) 可选“双检”：在应用服务内以“开始前的评论数 vs 结束后的评论数”做内存对比，如增长则再触发一次（不落库计数）。

## 8. 示例方法签名与伪代码

```java
// Infrastructure: 监听器（@Async）
public class CommentSummaryEventListener {
  private final PostSummaryAppService postSummaryAppService;
  public void onContentPublished(ContentPublishedEvent evt) {
    if (evt.getContentType() != ContentType.COMMENT) return;
    postSummaryAppService.handleCommentCreated(evt.getContentId());
  }
}

// Application: 编排
@Service
public class PostSummaryAppService {
  private final CommentDomainService commentDomainService;
  private final PostDomainService postDomainService;
  private final PostSummaryDomainService postSummaryDomainService;
  private final CommentSummaryAIClient aiClient; // Infrastructure 能力
  private final LockService lockService;         // Infrastructure 能力

  public void handleCommentCreated(String commentId) {
    CommentEntity c = commentDomainService.getCommentById(commentId);
    if (c.getBusinessType() != BusinessType.POST) return;
    generateAndSaveIfNeeded(c.getBusinessId());
  }

  public void generateAndSaveIfNeeded(String postId) {
    if (!featureEnabled()) return;
    long startCount = commentDomainService.getCommentCountByBusiness(postId, BusinessType.POST);
    if (startCount <= threshold()) return;

    if (!lockService.tryLock("post:summary:"+postId, Duration.ofMinutes(2))) return;
    try {
      PostEntity post = postDomainService.getPostById(postId);
      List<CommentEntity> comments = commentDomainService.getRootCommentsByBusiness(postId, BusinessType.POST);
      CommentSummaryResult r = aiClient.summarize(post, trim(comments));

      PostSummaryEntity e = new PostSummaryEntity();
      e.setPostId(postId);
      e.setSummary(r.getText());
      postSummaryDomainService.upsert(e);
    } catch (Exception ex) {
      // 失败仅记录日志与监控，不写模型/状态到库
      log.warn("Post summary generate failed, postId={}", postId, ex);
    } finally {
      lockService.unlock("post:summary:"+postId);
      // 可选双检：若期间评论数增长，异步再触发一次
      long endCount = commentDomainService.getCommentCountByBusiness(postId, BusinessType.POST);
      if (endCount > startCount) asyncRequeue(postId);
    }
  }
}
```

## 9. DTO/Assembler 规范

- `PostSummaryDTO`：`id/postId/summary/updateTime`
- `PostSummaryAssembler`：全部静态方法 + `BeanUtils.copyProperties(...)`，仅用于对外返回（未来如提供查询摘要的 API）。

## 10. 枚举与类型处理器

- 不新增状态/模型相关的枚举与类型处理器；
- 若未来新增“摘要类型”或“可见性”等业务枚举，再进行注册。

## 11. 事务与一致性

- 评论创建与事件发布：保持与现状一致（发布即刻发生），监听器侧再次查询，避免未提交读取风险；
- 应用层对“写入摘要”操作加 `@Transactional` 保证同一篇文章的 upsert 原子性；
- 通过唯一键 + 锁 + 双检 保证最终一致。

## 12. 验证用例与监控

- 用例：
  - 评论数=3 → 不触发；=4 → 触发生成；
  - 生成期间叠加多条新评论 → 只有 1 次生成，结束后检测到落后再补 1 次；
  - AI 超时/失败 → `status=FAILED` + `error_message` 记录，后续新增评论可恢复；
  - 非文章业务（课程/章节/其他）评论 → 不触发。
- 观测：
  - 日志：开始/结束/失败；
  - 指标（可后续）：生成次数、失败次数、耗时、tokens 使用。

## 13. 落地步骤

1) 新增 Flyway 脚本：`VXX__create_post_summary_table.sql`；
2) 新增 Domain: summary 子域（Entity/Repository/Service/Enum/Converter）；
3) 新增 Infrastructure: `CommentSummaryAIClient`、监听器、锁实现/接入；
4) 新增 Application: `PostSummaryAppService` + DTO/Assembler；
5) 配置 Spring AI（密钥、模型、开关）；
6) 本地联调：制造 4 条以上文章评论，观察摘要入库；
7) 编写最小化单元/集成测试（Domain/Infra 轻量 Mock）。

## 14. 取舍与扩展

- 取舍：
  - 使用 Spring 应用事件而非 MQ，降低引入成本；未来可替换为 MQ（Infra 只需改监听适配）；
  - 简单“锁 + 双检”策略替代复杂的“中断在途任务”机制，保障最终一致；
  - 当前仅按评论条数阈值触发，未来可加入“评论质量/点赞数”等权重。
- 扩展：
  - 支持多模型 A/B（通过环境变量切换 `COMMENT_SUMMARY_MODEL`）；
  - 管理员手动重算/回滚；
  - 将摘要作为缓存，针对文章详情页返回（命中率高）。

---

附：命名与规范对齐
- 实体名以 `Entity` 结尾：`PostSummaryEntity`
- 应用服务以 `AppService` 结尾：`PostSummaryAppService`
- 领域服务以 `DomainService` 结尾：`PostSummaryDomainService`
- Repository 直接继承 `BaseMapper<Entity>`：`PostSummaryRepository`
- Assembler 静态方法 + BeanUtils.copy
- 仅在 Controller 使用 `UserContext`（本方案不涉及 Controller）

---

## 附录：Spring AI 使用速览（语法示例）

1) 注入与最小用法

```java
@Service
public class CommentSummaryAIClient {
  private final ChatClient chatClient; // 由 Boot Starter 自动配置

  public CommentSummaryAIClient(ChatClient chatClient) {
    this.chatClient = chatClient;
  }

  public CommentSummaryResult summarize(PostEntity post, List<CommentEntity> comments) {
    String userText = buildUserPrompt(post, comments);
    String content = chatClient
        .prompt()
        .system("你是资深社区编辑，请用中文输出3-5条要点摘要，避免无关与重复。")
        .user(userText)
        .call()
        .content();
    return new CommentSummaryResult(content);
  }
}
```

2) 构造 Prompt（示例）

```java
private String buildUserPrompt(PostEntity post, List<CommentEntity> comments) {
  String postPart = "文章标题：" + post.getTitle() + "\n" +
                    "正文（截断）：" + left(post.getContent(), maxPostChars) + "\n\n";
  String commentPart = comments.stream()
      .limit(maxCommentCount)
      .map(c -> "- " + sanitize(c.getContent()))
      .collect(Collectors.joining("\n"));
  return postPart + "评论要点：\n" + commentPart + "\n\n请总结关键信息，输出项目符号列表。";
}
```

3) 可选：流式生成/超时控制

```java
// 流式（按 token 推送内容片段）
Flux<String> stream = chatClient
    .prompt()
    .user(userText)
    .stream()
    .content();

// 自定义选项（以 OpenAI 为例）
String content = chatClient
    .prompt()
    .options(OpenAiChatOptions.builder()
        .withModel(System.getenv().getOrDefault("COMMENT_SUMMARY_MODEL", "gpt-4o-mini"))
        .withTemperature(0.2)
        .withMaxTokens(512)
        .build())
    .user(userText)
    .call()
    .content();
```

4) 本地/代理兼容

- 设置 `OPENAI_BASE_URL` 指向兼容 OpenAI 的网关/代理；
- 或使用 Ollama 启动本地模型：引入 `spring-ai-ollama-spring-boot-starter` 并配置 `spring.ai.ollama.base-url`、模型名环境变量。
