# Skills 市场后端 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 新增 Skills 子域，提供公开市场分页查询与用户自管理 CRUD 能力。

**Architecture:** 通过新增 `skills` 表和 `skill` 子域实现四字段技能内容模型；公开接口挂在 `/api/public/skills`，用户自管理接口挂在 `/api/user/skills`；公开列表返回卡片字段，详情接口单独返回 Markdown 描述，领域服务负责作者校验与业务校验。

**Tech Stack:** Spring Boot, Java 17, Maven, MyBatis Plus, Flyway, JUnit 5

## Related docs

- Spec: `docs/superpowers/specs/skills/2026-03-27-feat-skills-market-design.md`
- Frontend plan: `../../../../../qiaoya-community-frontend/docs/superpowers/plans/skills/2026-03-27-feat-skills-market.md`

---

### Task 1: 数据库、实体与领域基础

**Files:**
- Create: `src/main/resources/db/migration/V26__Create_skills_table.sql`
- Create: `src/main/java/org/xhy/community/domain/skill/entity/SkillEntity.java`
- Create: `src/main/java/org/xhy/community/domain/skill/repository/SkillRepository.java`
- Create: `src/main/java/org/xhy/community/domain/skill/service/SkillDomainService.java`
- Create: `src/main/java/org/xhy/community/infrastructure/exception/SkillErrorCode.java`

- [ ] **Step 1: 新增 Skills 表迁移**

```sql
CREATE TABLE skills (
  id VARCHAR(36) PRIMARY KEY,
  user_id VARCHAR(36) NOT NULL,
  name VARCHAR(100) NOT NULL,
  summary VARCHAR(255) NOT NULL,
  description TEXT NOT NULL,
  github_url VARCHAR(500) NOT NULL,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_skills_user_id ON skills(user_id);
CREATE INDEX idx_skills_create_time ON skills(create_time DESC);
```

- [ ] **Step 2: 建立实体与 Repository**

```java
@TableName("skills")
public class SkillEntity extends BaseEntity {
    private String userId;
    private String name;
    private String summary;
    private String description;
    private String githubUrl;
}
```

- [ ] **Step 3: 在领域服务中实现创建与作者校验**

```java
public SkillEntity create(String userId, String name, String summary, String description, String githubUrl) {
    SkillEntity entity = new SkillEntity();
    entity.setUserId(userId);
    entity.setName(name.trim());
    entity.setSummary(summary.trim());
    entity.setDescription(description.trim());
    entity.setGithubUrl(githubUrl.trim());
    skillRepository.insert(entity);
    return entity;
}
```

- [ ] **Step 4: 运行最小编译验证新领域类可被项目识别**

Run: `mvn -DskipTests compile`
Expected: build success with new skill domain classes

- [ ] **Step 5: 提交本任务**

```bash
git add src/main/resources/db/migration/V26__Create_skills_table.sql src/main/java/org/xhy/community/domain/skill/entity/SkillEntity.java src/main/java/org/xhy/community/domain/skill/repository/SkillRepository.java src/main/java/org/xhy/community/domain/skill/service/SkillDomainService.java src/main/java/org/xhy/community/infrastructure/exception/SkillErrorCode.java
git commit -m "feat(skills): 添加 skills 领域模型与表结构" -m "- 新增 skills 表迁移与索引\n- 建立 skill 实体仓储与领域服务"
```

### Task 2: DTO、应用服务与公开/用户接口

**Files:**
- Create: `src/main/java/org/xhy/community/application/skill/dto/SkillDTO.java`
- Create: `src/main/java/org/xhy/community/application/skill/assembler/SkillAssembler.java`
- Create: `src/main/java/org/xhy/community/application/skill/service/SkillAppService.java`
- Create: `src/main/java/org/xhy/community/interfaces/skill/request/CreateSkillRequest.java`
- Create: `src/main/java/org/xhy/community/interfaces/skill/request/UpdateSkillRequest.java`
- Create: `src/main/java/org/xhy/community/interfaces/skill/request/SkillQueryRequest.java`
- Create: `src/main/java/org/xhy/community/interfaces/skill/controller/PublicSkillController.java`
- Create: `src/main/java/org/xhy/community/interfaces/skill/controller/UserSkillController.java`

- [ ] **Step 1: 定义 DTO 与 Assembler**

```java
public class SkillDTO {
    private String id;
    private String userId;
    private String name;
    private String summary;
    private String description;
    private String githubUrl;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

- [ ] **Step 2: 在应用服务中实现公开查询与用户 CRUD 编排**

```java
public IPage<SkillDTO> queryPublicSkills(SkillQueryRequest request) {
    return skillDomainService.queryPublicSkills(request.getPageNum(), request.getPageSize())
            .convert(SkillAssembler::toPublicDTO);
}

public SkillDTO createSkill(CreateSkillRequest request, String userId) {
    SkillEntity created = skillDomainService.create(userId, request.getName(), request.getSummary(), request.getDescription(), request.getGithubUrl());
    return SkillAssembler.toDTO(created);
}
```

- [ ] **Step 3: 暴露公开接口与用户接口**

```java
@RestController
@RequestMapping("/api/public/skills")
public class PublicSkillController {
    @GetMapping
    public ApiResponse<IPage<SkillDTO>> list(SkillQueryRequest request) {
        return ApiResponse.success(skillAppService.queryPublicSkills(request));
    }
}

@RestController
@RequestMapping("/api/user/skills")
public class UserSkillController {
    @PostMapping
    public ApiResponse<SkillDTO> create(@Valid @RequestBody CreateSkillRequest request) {
        return ApiResponse.success("创建成功", skillAppService.createSkill(request, UserContext.getCurrentUserId()));
    }
}
```

- [ ] **Step 4: 运行编译验证控制器、DTO、请求对象全部通过**

Run: `mvn -DskipTests compile`
Expected: build success with new skill controllers and request classes

- [ ] **Step 5: 提交本任务**

```bash
git add src/main/java/org/xhy/community/application/skill/dto/SkillDTO.java src/main/java/org/xhy/community/application/skill/assembler/SkillAssembler.java src/main/java/org/xhy/community/application/skill/service/SkillAppService.java src/main/java/org/xhy/community/interfaces/skill/request/CreateSkillRequest.java src/main/java/org/xhy/community/interfaces/skill/request/UpdateSkillRequest.java src/main/java/org/xhy/community/interfaces/skill/request/SkillQueryRequest.java src/main/java/org/xhy/community/interfaces/skill/controller/PublicSkillController.java src/main/java/org/xhy/community/interfaces/skill/controller/UserSkillController.java
git commit -m "feat(skills): 添加 skills 应用服务与接口" -m "- 新增公开市场和用户自管理接口\n- 完成 DTO Assembler 与请求对象"
```

### Task 3: 权限校验与测试

**Files:**
- Create: `src/test/java/org/xhy/community/application/skill/SkillAppServiceTest.java`
- Modify: `src/main/java/org/xhy/community/domain/skill/service/SkillDomainService.java`
- Modify: `src/main/java/org/xhy/community/application/skill/service/SkillAppService.java`

- [ ] **Step 1: 为作者校验与公开列表写测试**

```java
@Test
void should_only_allow_owner_to_update_skill() {
    CreateSkillRequest create = new CreateSkillRequest();
    create.setName("Codex Skill");
    create.setSummary("Summary");
    create.setDescription("**markdown**");
    create.setGithubUrl("https://github.com/acme/codex-skill");
    SkillDTO created = skillAppService.createSkill(create, "user-a");

    UpdateSkillRequest update = new UpdateSkillRequest();
    update.setName("Updated");
    update.setSummary("Updated Summary");
    update.setDescription("Updated **markdown**");
    update.setGithubUrl("https://github.com/acme/codex-skill-updated");

    assertThrows(BusinessException.class, () -> skillAppService.updateSkill(created.getId(), update, "user-b"));
}
```

- [ ] **Step 2: 补齐删除后不出现在公开列表的验证**

```java
@Test
void should_hide_deleted_skill_from_public_list() {
    CreateSkillRequest create = new CreateSkillRequest();
    create.setName("Skill");
    create.setSummary("Summary");
    create.setDescription("Description");
    create.setGithubUrl("https://github.com/acme/skill");
    SkillDTO created = skillAppService.createSkill(create, "user-a");

    skillAppService.deleteSkill(created.getId(), "user-a");

    SkillQueryRequest query = new SkillQueryRequest();
    query.setPageNum(1L);
    query.setPageSize(10L);
    IPage<SkillDTO> page = skillAppService.queryPublicSkills(query);

    assertThat(page.getRecords()).noneMatch(skill -> created.getId().equals(skill.getId()));
}
```

- [ ] **Step 3: 运行后端测试**

Run: `mvn -Dtest=SkillAppServiceTest test`
Expected: tests pass for create, owner check, public list and delete scenarios

- [ ] **Step 4: 运行回归编译**

Run: `mvn test`
Expected: no regression in existing backend tests

- [ ] **Step 5: 提交本任务**

```bash
git add src/test/java/org/xhy/community/application/skill/SkillAppServiceTest.java src/main/java/org/xhy/community/domain/skill/service/SkillDomainService.java src/main/java/org/xhy/community/application/skill/service/SkillAppService.java
git commit -m "test(skills): 补充 skills 领域与应用层验证" -m "- 覆盖本人编辑删除与公开可见规则\n- 验证删除后公开列表过滤"
```
