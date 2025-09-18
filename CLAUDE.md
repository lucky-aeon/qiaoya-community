# 敲鸭社区后端项目架构规范

## DDD架构设计

### 架构概述
本项目采用自定义的领域驱动设计(DDD)架构，结合MyBatis Plus进行数据持久化。

### 目录结构
```
src/main/java/org/xhy/community/
├── interfaces/          # 接口层（当前项目暂不使用）
├── application/         # 应用层  
│   └── {subdomain}/     # 各子域应用层
│       ├── service/     # 应用服务
│       ├── dto/         # 数据传输对象
│       └── assembler/   # 转换器
├── domain/             # 领域层
│   ├── entity/         # 通用实体基类
│   └── {subdomain}/    # 各子域
│       ├── entity/     # 领域实体
│       ├── valueobject/ # 值对象
│       ├── repository/ # Repository接口
│       └── service/    # 领域服务
└── infrastructure/     # 基础设施层
    └── config/         # 配置类
```

### 层级职责

#### 1. Application层 (应用层)
- **AppService**: 应用服务，编排业务流程
- **DTO**: 数据传输对象，用于对外输出
- **Assembler**: 转换器，负责Domain实体到DTO的转换
- **职责**: 
  - 业务流程编排
  - 领域对象到DTO的转换
  - 事务管理

#### 2. Domain层 (领域层)
- **BaseEntity**: 通用实体基类，包含公共字段
- **Entity**: 领域实体，包含业务逻辑（以Entity结尾）
- **ValueObject**: 值对象，不可变对象
- **Repository**: 仓储接口，直接继承MyBatis Plus的BaseMapper
- **Service**: 领域服务，处理跨实体的业务逻辑
- **职责**: 核心业务逻辑，数据持久化

#### 3. Infrastructure层 (基础设施层)
- **Config**: 配置类
- **职责**: 提供技术实现，如缓存、消息队列等

### 调用关系规范

#### 严格的分层调用关系
1. **Application Layer** → **Domain Service** （仅此）
2. **Domain Service** → **Repository** （仅此）
3. **任何层** → **Infrastructure** 
4. **Infrastructure** → **Application Service** (仅此)

#### 禁止的调用关系
- **Application层不能直接调用Repository层**
- **Infrastructure不能调用Domain层**
- **Domain层不能调用Application层**

#### 层级职责分工

**Application层职责：**
- 业务流程编排
- 调用Domain服务完成业务操作
- Domain实体到DTO的转换
- 事务管理
- **不负责参数校验（由API层负责）**

**Domain层职责：**
- 核心业务逻辑
- 数据持久化操作
- 领域规则实现
- **不负责参数格式校验（由API层负责）**
- **只负责业务规则校验（如邮箱唯一性等）**

### 数据流转规范

#### 响应数据流
1. **Domain实体** → **DTO转换** (Application层Assembler)
2. **DTO** → **返回结果** (Application层)

## 开发规范

### 1. 命名规范

#### 实体命名
- 所有领域实体必须以`Entity`结尾
- 示例: `UserEntity`, `PostEntity`, `CommentEntity`

#### 服务命名
- 应用服务必须以`AppService`结尾
- 领域服务以`DomainService`结尾
- 示例: `UserAppService`, `UserDomainService`

#### Repository命名
- 仓储接口以`Repository`结尾
- 直接继承`BaseMapper<EntityType>`
- 示例: `UserRepository extends BaseMapper<UserEntity>`

### 2. Repository使用规范

#### Repository接口定义
```java
@Repository
public interface UserRepository extends BaseMapper<UserEntity> {
    // 不需要写自定义SQL方法
    // 使用MyBatis Plus提供的方法和条件构造器
}
```

#### 在领域服务中使用
```java
@Service
public class UserDomainService {
    
    @Autowired
    private UserRepository userRepository;
    
    public void validateUniqueEmail(String email, String excludeUserId) {
        LambdaQueryWrapper<UserEntity> queryWrapper = 
            new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getEmail, email)
                .eq(UserEntity::getDeleted, false);
        
        if (excludeUserId != null) {
            queryWrapper.ne(UserEntity::getId, excludeUserId);
        }
        
        if (userRepository.exists(queryWrapper)) {
            throw new IllegalArgumentException("该邮箱已被注册");
        }
    }
}
```

### 3. 转换层规范

#### Assembler命名和位置
- **位置**: 转换器必须位于Application层的assembler包中
- **命名**: 转换器必须以`Assembler`结尾
- **示例**: `UserAssembler`, `PostAssembler`, `CommentAssembler`

#### 转换器使用静态方法
```java
// 位置：org.xhy.community.application.user.assembler.UserAssembler
public class UserAssembler {
    
    public static UserDTO toDTO(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
```

#### 在应用服务中调用
```java
@Service
public class UserAppService {
    
    public UserDTO getUserById(String userId) {
        UserEntity user = getUserEntityById(userId);
        return UserAssembler.toDTO(user);  // 静态方法调用
    }
}
```

### 4. 实体设计规范

#### BaseEntity使用
```java
// 通用基类位置：org.xhy.community.domain.entity.BaseEntity
public abstract class BaseEntity {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Boolean deleted;
    
    // getters and setters...
}
```

#### 具体实体继承
```java
@TableName("users")
public class UserEntity extends BaseEntity {
    private String name;
    private String email;
    // 其他业务字段...
    
    // 业务方法
    public void activate() {
        this.status = UserStatus.ACTIVE;
    }
}
```

### 5. 参数校验规范

#### 校验职责分工
- **API层（Controller）**：负责所有参数格式校验
  - 空值校验、长度校验、格式校验（如邮箱格式）
  - 使用@Valid、@NotBlank、@Email等注解
- **Application层**：不负责参数校验，专注业务流程编排
- **Domain层**：只负责业务规则校验
  - 如邮箱唯一性校验、业务状态校验等
  - 不包含参数格式校验逻辑

#### Domain层业务规则校验示例
```java
@Service
public class UserDomainService {
    
    // ✅ 正确：业务规则校验
    public boolean isEmailExists(String email, String excludeUserId) {
        // 只校验业务规则：邮箱是否已存在
        return userRepository.exists(queryWrapper);
    }
    
    // ❌ 错误：不应该在Domain层做格式校验
    public void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
        // 这些应该在API层完成
    }
}
```

#### Application层调用示例
```java
@Service
public class UserAppService {
    
    public UserDTO createUser(String name, String email, String password) {
        // API层已完成参数格式校验，这里直接进行业务校验
        if (userDomainService.isEmailExists(email, null)) {
            throw new IllegalArgumentException("该邮箱已被注册");
        }
        
        UserEntity user = userDomainService.createUser(name, email, password);
        return UserAssembler.toDTO(user);
    }
}
```

### 6. 不使用的规范

1. **不使用值对象封装简单类型**：如Email等直接使用String
2. **不写自定义SQL**：Repository只继承BaseMapper，使用条件构造器
3. **暂不实现API接口**：不创建Controller和Request对象
4. **不使用复杂的Repository实现**：直接在Domain层使用MyBatis Plus

### 7. MyBatis Plus使用规范

#### 条件查询示例
```java
// 根据邮箱查询用户
UserEntity user = userRepository.selectOne(
    new LambdaQueryWrapper<UserEntity>()
        .eq(UserEntity::getEmail, email)
        .eq(UserEntity::getDeleted, false)
);

// 分页查询
Page<UserEntity> page = new Page<>(pageNum, pageSize);
IPage<UserEntity> result = userRepository.selectPage(page,
    new LambdaQueryWrapper<UserEntity>()
        .like(UserEntity::getName, name)
        .eq(UserEntity::getDeleted, false)
        .orderByDesc(UserEntity::getCreateTime));
```

### 8. 数据库迁移规范

使用FlyWay管理数据库版本：
- 迁移文件位置：`src/main/resources/db/migration`
- 命名格式：`V{version}__{description}.sql`
- 示例：`V1__Create_user_table.sql`
- 语法规范是 postgre sql
- 非高频的查询字段不要加索引

### 9. UUID主键使用规范

#### 主键生成策略
- **BaseEntity主键类型**: 使用`String`类型，数据库存储为`VARCHAR(36)`
- **ID生成方式**: 使用`@TableId(type = IdType.ASSIGN_UUID)`，由MyBatis Plus自动生成UUID
- **自动填充**: 在`MyBatisPlusMetaObjectHandler`中配置UUID自动生成

#### UUID主键配置示例
```java
// BaseEntity配置
@TableId(type = IdType.ASSIGN_UUID)
private String id;

// MetaObjectHandler配置
@Component
public class MyBatisPlusMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "id", String.class, UUID.randomUUID().toString());
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
```

#### 数据库表结构
```sql
-- 主键字段定义
id VARCHAR(36) PRIMARY KEY,
```

#### DTO和服务层类型
- **DTO中ID字段**: `private String id;`
- **服务方法参数**: 所有涉及用户ID的方法参数使用`String userId`
- **Repository查询**: 查询条件中ID使用String类型

### 技术栈
- JDK 17
- Spring Boot 3.2.0
- MyBatis Plus 3.5.4
- FlyWay (数据库迁移)
- PostgreSQL
- Spring Security Crypto (密码加密)
- Maven


### 10. 开发规范
- 依赖注入使用构造函数的方式
- assembler 使用 BeanUtils.copy 的方式
- 如果要使用到分页查询，则 request 对象继承 org/xhy/community/interfaces/common/request/PageRequest.java
- request 对象的创建对象和修改对象差异就只有主键，因此修改对象可直接继承创建对象
- 如果是管理员接口，路由规范/api/admin/xx，app 层规范：AdminXxxAppService
- 实体中需要使用枚举类类型，需要添加转换类后还需要添加到 MyBatisTypeHandlerConfig 中
- 抛出异常类的时候需要使用对应模块的异常类以及异常码进行处理:/Users/xhy/IdeaProjects/qiaoya-community/qiaoya-community-backend/src/main/java/org/xhy/community/infrastructure/exception
- api层如果接收的是 request 对象，那么传入 app 层也是 request 对象，app 层传入 domain 一般是实体，而不是多个入参。通过 assembler 转换。可参考：org/xhy/community/application/course/service/AdminCourseAppService.java
- 修改数据的时候，接收 api 层的 update 对象，转换成 entity 进行修改，例如：

```java
  public CourseDTO updateCourse(String courseId, UpdateCourseRequest request) {
        CourseEntity course = CourseAssembler.fromUpdateRequest(request, courseId);
        
        CourseEntity updatedCourse = courseDomainService.updateCourse(course);
        
        return CourseAssembler.toDTO(updatedCourse);
    }

public CourseEntity updateCourse(CourseEntity course) {
  courseRepository.updateById(course);
  return course;
}


```

- 查数据的忽略软删除，因配置文件做了处理，反向案例：

```java
 public List<ChapterEntity> getChaptersByCourseId(String courseId) {
        LambdaQueryWrapper<ChapterEntity> queryWrapper = new LambdaQueryWrapper<ChapterEntity>()
            .eq(ChapterEntity::getCourseId, courseId)
            .eq(ChapterEntity::getDeleted, false)
            .orderByAsc(ChapterEntity::getSortOrder);
        
        return chapterRepository.selectList(queryWrapper);
    }
```

正确案例
```java
 public List<ChapterEntity> getChaptersByCourseId(String courseId) {
        LambdaQueryWrapper<ChapterEntity> queryWrapper = new LambdaQueryWrapper<ChapterEntity>()
            .eq(ChapterEntity::getCourseId, courseId)
            .orderByAsc(ChapterEntity::getSortOrder);
        
        return chapterRepository.selectList(queryWrapper);
    }
```


- git commit message 提交需要使用规范：feat/fix(模块):消息
- 三种 api 路由的方式：前台/用户管理后台/管理员后台 
- 项目中涉及到枚举类类型的传递必须使用枚举类型！而不是其他类型！
- 条件查询的正确案例

```java
Children like(boolean condition, R column, Object val);
```

### 11. 权限控制设计规范

#### 权限标识枚举设计
使用枚举类型定义访问权限级别，支持扩展性和类型安全：

```java
// 位置：org.xhy.community.domain.common.valueobject.AccessLevel
public enum AccessLevel {
    USER,   // 普通用户权限 - 只能访问自己的数据
    ADMIN   // 管理员权限 - 可以访问所有数据
}
```

#### 权限控制在Domain层的优雅实现
**设计原则：**
- 复用现有查询逻辑，避免代码重复
- 通过权限标识参数控制查询范围
- Domain层根据权限标识决定是否添加用户隔离条件
- 前提条件：如果一个 domain 方法既可以被管理员使用，也可以被用户使用，那么就需要权限标识来隔离复用该方法

**实现示例：**
```java
// PostDomainService中的统一查询方法
public IPage<PostEntity> getUserPosts(String authorId, Integer pageNum, Integer pageSize, 
                                     PostStatus status, AccessLevel accessLevel) {
    Page<PostEntity> page = new Page<>(pageNum, pageSize);
    
    LambdaQueryWrapper<PostEntity> queryWrapper = new LambdaQueryWrapper<PostEntity>()
            // 关键设计：根据权限级别决定是否添加用户隔离条件
            .eq(accessLevel == AccessLevel.USER && authorId != null, PostEntity::getAuthorId, authorId)
            .eq(status != null, PostEntity::getStatus, status)
            .orderByDesc(PostEntity::getCreateTime);
    
    return postRepository.selectPage(page, queryWrapper);
}
```

**调用示例：**
```java
// 普通用户查询自己的文章
postDomainService.getUserPosts(userId, pageNum, pageSize, status, AccessLevel.USER);

// 管理员查询所有文章
postDomainService.getUserPosts(null, pageNum, pageSize, status, AccessLevel.ADMIN);
```

#### 权限控制的优势
1. **代码复用**: 避免为管理员和用户分别写查询方法
2. **扩展性强**: 后续可轻松添加其他权限级别（如版主等）
3. **类型安全**: 枚举避免布尔参数的歧义性
4. **语义清晰**: `AccessLevel.ADMIN` 比 `true` 更直观
5. **维护性好**: 权限逻辑集中在Domain层，便于统一管理

#### 管理员接口实现模式
遵循标准的DDD分层架构，为管理员功能创建独立的服务和控制器：

**文件结构：**
- `AdminPostController` - 管理员接口控制器（路由：`/api/admin/posts`）
- `AdminPostAppService` - 管理员应用服务（传入 `AccessLevel.ADMIN`）
- `AdminPostDTO` - 管理员专用DTO（包含作者名称、分类名称等扩展信息）
- `AdminPostAssembler` - 管理员专用转换器

**关联数据查询模式：**
```java
// 批量查询关联数据，避免N+1查询问题
Set<String> authorIds = posts.stream().map(PostEntity::getAuthorId).collect(Collectors.toSet());
Set<String> categoryIds = posts.stream().map(PostEntity::getCategoryId).collect(Collectors.toSet());

Map<String, String> authorNames = userDomainService.getUserNameMapByIds(authorIds);
Map<String, String> categoryNames = categoryService.getCategoryNameMapByIds(categoryIds);

// 在Assembler中组装完整的DTO
List<AdminPostDTO> dtos = AdminPostAssembler.toDTOList(posts, authorNames, categoryNames);
```
