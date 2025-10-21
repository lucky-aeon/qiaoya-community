# OAuth2 授权服务器技术方案（Spring Authorization Server）

作者：后端
最后更新：2025-01-21
状态：设计稿
适用范围：敲鸭社区作为 OAuth2/OIDC 身份提供者（IdP），供第三方应用接入

维护人：后端
关键代码路径：
- 基础设施：src/main/java/org/xhy/community/infrastructure/oauth2/*（Spring Authorization Server 配置）
- 领域层：src/main/java/org/xhy/community/domain/oauth2/*（客户端管理、授权管理）
- 应用层：src/main/java/org/xhy/community/application/oauth2/*（管理后台服务、授权流程编排）
- 接口层：src/main/java/org/xhy/community/interfaces/oauth2/*（OAuth2 标准端点、管理后台接口）

---

## ⚠️ 前端开发者必读

**本方案采用前后端分离架构**，登录页面由前端实现。

**前端开发者请直接跳转到**：[第21章 - 前端登录页面集成方案](#21-前端登录页面集成方案重要)

该章节包含：
- ✅ 前端登录页面的完整实现示例（Vue 3）
- ✅ OAuth 回调参数的处理方式
- ✅ Token 传递配置
- ✅ 完整的测试流程
- ✅ 常见问题解答

---

## 1. 背景与目标

### 1.1 业务背景
敲鸭社区需要作为身份提供者（Identity Provider），允许第三方应用通过 OAuth2/OIDC 协议接入，实现"使用敲鸭账号登录"的能力，类似于"微信登录"、"GitHub登录"的开放平台模式。

### 1.2 核心目标
- **对外开放平台**：第三方开发者可以注册应用，获取 Client ID 和 Client Secret
- **标准化协议**：完整支持 OAuth2.0 授权码模式、客户端凭证模式和 OIDC 标准
- **管理后台**：提供客户端应用管理、授权记录查询、统计分析等功能
- **安全可控**：客户端审核、权限控制、令牌管理、审计日志等安全机制
- **集成现有体系**：与当前的 JWT 认证体系无缝集成，复用用户表和权限系统

### 1.3 技术选型理由
选择 **Spring Authorization Server**（Spring 官方方案）而非 Keycloak 或自研的原因：
- ✅ 与现有 Spring Boot 项目无缝集成，无需独立部署
- ✅ 完全掌控代码和逻辑，可深度定制
- ✅ Spring 官方支持，社区活跃，文档完善
- ✅ 符合项目的 DDD 架构规范，可按分层设计管理后台
- ❌ 需要自己实现管理 UI（但符合项目需求，可按业务定制）

### 1.4 范围与不做
- ✅ 实现 OAuth2 授权码模式（Authorization Code Flow）
- ✅ 实现 OAuth2 客户端凭证模式（Client Credentials Flow）
- ✅ 支持 PKCE（Proof Key for Code Exchange）增强安全性
- ✅ 实现管理后台（客户端 CRUD、授权记录、统计分析）
- ✅ 支持 OIDC（OpenID Connect）获取用户信息
- ❌ 暂不实现隐式模式（Implicit Flow）和密码模式（已废弃/不推荐）
- ❌ 暂不支持联合登录（Federation）和多租户

## 2. OAuth2/OIDC 核心概念

### 2.1 角色定义
- **资源所有者（Resource Owner）**：敲鸭社区的用户
- **客户端（Client）**：第三方应用（如某个学习平台、社区工具）
- **授权服务器（Authorization Server）**：敲鸭社区后端（本系统）
- **资源服务器（Resource Server）**：敲鸭社区的 API（提供用户信息、课程数据等）

### 2.2 授权码模式流程（最常用）
```
1. 用户访问第三方应用 → 点击"使用敲鸭账号登录"
2. 第三方应用重定向到敲鸭授权页面：
   GET /oauth2/authorize?
       response_type=code
       &client_id=xxx
       &redirect_uri=https://third-party.com/callback
       &scope=openid profile email
       &state=random_state
       &code_challenge=xxx (PKCE)
       &code_challenge_method=S256

3. 用户在敲鸭登录（如果未登录）→ 授权同意页面（首次）→ 同意授权

4. 敲鸭重定向回第三方应用并携带授权码：
   https://third-party.com/callback?code=AUTHORIZATION_CODE&state=random_state

5. 第三方应用后端使用授权码换取 Access Token：
   POST /oauth2/token
   Content-Type: application/x-www-form-urlencoded

   grant_type=authorization_code
   &code=AUTHORIZATION_CODE
   &redirect_uri=https://third-party.com/callback
   &client_id=xxx
   &client_secret=yyy
   &code_verifier=zzz (PKCE)

6. 敲鸭返回 Access Token 和 ID Token：
   {
     "access_token": "eyJhbGc...",
     "token_type": "Bearer",
     "expires_in": 3600,
     "refresh_token": "eyJhbGc...",
     "id_token": "eyJhbGc..." (OIDC)
   }

7. 第三方应用使用 Access Token 调用敲鸭 API：
   GET /oauth2/userinfo
   Authorization: Bearer eyJhbGc...

   返回用户信息：
   {
     "sub": "user-uuid",
     "name": "张三",
     "email": "zhangsan@example.com",
     "avatar": "https://..."
   }
```

### 2.3 客户端凭证模式流程（服务端对服务端）
```
1. 第三方应用后端直接请求 Token（无需用户参与）：
   POST /oauth2/token
   Content-Type: application/x-www-form-urlencoded

   grant_type=client_credentials
   &client_id=xxx
   &client_secret=yyy
   &scope=api:read

2. 敲鸭返回 Access Token：
   {
     "access_token": "eyJhbGc...",
     "token_type": "Bearer",
     "expires_in": 3600
   }

3. 第三方应用使用 Token 调用敲鸭 API
```

### 2.4 Scope 权限设计
- `openid`：必需，表示使用 OIDC 协议
- `profile`：获取用户基本信息（姓名、头像等）
- `email`：获取用户邮箱
- `courses:read`：读取用户的课程数据（示例）
- `posts:read`：读取用户的文章数据（示例）
- 管理员可为每个客户端配置允许的 Scope 范围

## 3. DDD 架构设计

### 3.1 架构分层
严格遵循项目的 DDD 架构规范：

```
interfaces/oauth2/
├── controller/
│   ├── OAuth2AuthorizationController.java  # OAuth2 标准端点（授权、Token）
│   ├── OAuth2UserInfoController.java       # OIDC UserInfo 端点
│   └── AdminOAuth2ClientController.java    # 管理后台：客户端管理
├── request/
│   ├── CreateOAuth2ClientRequest.java      # 创建客户端请求
│   ├── UpdateOAuth2ClientRequest.java      # 更新客户端请求
│   └── OAuth2ClientQueryRequest.java       # 客户端查询请求（分页）

application/oauth2/
├── service/
│   ├── AdminOAuth2ClientAppService.java    # 管理后台：客户端管理应用服务
│   └── OAuth2AuthorizationAppService.java  # 授权流程编排应用服务
├── dto/
│   ├── OAuth2ClientDTO.java                # 客户端信息 DTO
│   ├── OAuth2AuthorizationDTO.java         # 授权记录 DTO
│   └── OAuth2TokenResponseDTO.java         # Token 响应 DTO
└── assembler/
    └── OAuth2ClientAssembler.java          # 客户端转换器

domain/oauth2/
├── entity/
│   ├── OAuth2ClientEntity.java             # 客户端实体
│   ├── OAuth2AuthorizationEntity.java      # 授权记录实体
│   └── OAuth2ConsentEntity.java            # 用户授权同意实体
├── valueobject/
│   ├── GrantType.java                      # 授权类型枚举
│   ├── ClientAuthenticationMethod.java     # 客户端认证方式枚举
│   └── OAuth2Scope.java                    # Scope 枚举
├── repository/
│   ├── OAuth2ClientRepository.java         # 客户端仓储
│   ├── OAuth2AuthorizationRepository.java  # 授权记录仓储
│   └── OAuth2ConsentRepository.java        # 授权同意仓储
└── service/
    ├── OAuth2ClientDomainService.java      # 客户端领域服务
    └── OAuth2AuthorizationDomainService.java # 授权领域服务

infrastructure/oauth2/
├── config/
│   ├── AuthorizationServerConfig.java      # Spring Authorization Server 核心配置
│   ├── OAuth2SecurityConfig.java           # OAuth2 安全配置
│   └── JwkSetConfig.java                   # JWT/JWK 配置
├── customizer/
│   ├── CustomUserAuthenticationProvider.java # 自定义用户认证提供者
│   └── CustomConsentPage.java              # 自定义授权同意页面
└── service/
    ├── CustomRegisteredClientRepository.java # 客户端存储适配器
    ├── CustomAuthorizationService.java       # 授权记录存储适配器
    └── CustomAuthorizationConsentService.java # 授权同意存储适配器
```

### 3.2 调用关系规范
- **Application 层** → **Domain Service**（仅此）
- **Domain Service** → **Repository**（仅此）
- **Infrastructure 层** → **Application Service** / **Domain Service**（读取配置、适配）
- ❌ Application 层不能直接调用 Repository
- ❌ Infrastructure 层不能调用 Domain 层的业务逻辑（仅作适配）

## 4. 数据模型设计

### 4.1 OAuth2 客户端表（oauth2_clients）

存储第三方应用的客户端信息。

```sql
-- V67__Create_oauth2_clients_table.sql
CREATE TABLE oauth2_clients (
    id VARCHAR(36) PRIMARY KEY,

    -- 客户端基本信息
    client_id VARCHAR(100) NOT NULL UNIQUE COMMENT '客户端ID（对外暴露）',
    client_secret_enc VARCHAR(500) NOT NULL COMMENT '客户端密钥（加密存储）',
    client_name VARCHAR(200) NOT NULL COMMENT '客户端名称（应用名称）',

    -- 客户端元信息
    client_description TEXT COMMENT '客户端描述',
    client_logo_url VARCHAR(500) COMMENT '客户端Logo URL',
    client_website_url VARCHAR(500) COMMENT '客户端官网地址',

    -- 联系信息
    contact_email VARCHAR(255) COMMENT '联系邮箱',
    contact_name VARCHAR(100) COMMENT '联系人姓名',

    -- OAuth2 配置
    redirect_uris TEXT NOT NULL COMMENT '重定向URI列表（JSON数组）',
    grant_types TEXT NOT NULL COMMENT '授权类型列表（JSON数组）',
    scopes TEXT NOT NULL COMMENT '允许的Scope列表（JSON数组）',

    -- 认证方式
    client_authentication_methods TEXT NOT NULL COMMENT '客户端认证方式（JSON数组）',

    -- Token 配置
    access_token_validity_seconds INTEGER NOT NULL DEFAULT 3600 COMMENT 'Access Token有效期（秒）',
    refresh_token_validity_seconds INTEGER NOT NULL DEFAULT 2592000 COMMENT 'Refresh Token有效期（秒，默认30天）',

    -- PKCE 配置
    require_proof_key BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否强制要求PKCE',

    -- 授权同意配置
    require_authorization_consent BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否需要用户授权同意',

    -- 状态管理
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '客户端状态：ACTIVE/SUSPENDED/REVOKED',

    -- 审计字段
    created_by VARCHAR(36) COMMENT '创建人用户ID',

    -- BaseEntity 公共字段
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TIMESTAMP COMMENT '软删除时间戳'
);

-- 索引
CREATE INDEX idx_oauth2_clients_client_id ON oauth2_clients (client_id) WHERE deleted IS NULL;
CREATE INDEX idx_oauth2_clients_status ON oauth2_clients (status) WHERE deleted IS NULL;
CREATE INDEX idx_oauth2_clients_created_by ON oauth2_clients (created_by) WHERE deleted IS NULL;
CREATE INDEX idx_oauth2_clients_create_time ON oauth2_clients (create_time) WHERE deleted IS NULL;

COMMENT ON TABLE oauth2_clients IS 'OAuth2客户端应用表';
```

### 4.2 OAuth2 授权记录表（oauth2_authorizations）

存储授权码、Access Token、Refresh Token 等授权信息。

```sql
-- V68__Create_oauth2_authorizations_table.sql
CREATE TABLE oauth2_authorizations (
    id VARCHAR(36) PRIMARY KEY,

    -- 关联信息
    client_id VARCHAR(100) NOT NULL COMMENT '客户端ID',
    principal_name VARCHAR(255) NOT NULL COMMENT '用户标识（用户ID）',

    -- 授权类型
    authorization_grant_type VARCHAR(50) NOT NULL COMMENT '授权类型',

    -- 授权码相关
    authorization_code_value TEXT COMMENT '授权码值',
    authorization_code_issued_at TIMESTAMP COMMENT '授权码签发时间',
    authorization_code_expires_at TIMESTAMP COMMENT '授权码过期时间',
    authorization_code_metadata TEXT COMMENT '授权码元数据（JSON）',

    -- Access Token 相关
    access_token_value TEXT COMMENT 'Access Token值（加密存储）',
    access_token_issued_at TIMESTAMP COMMENT 'Access Token签发时间',
    access_token_expires_at TIMESTAMP COMMENT 'Access Token过期时间',
    access_token_metadata TEXT COMMENT 'Access Token元数据（JSON）',
    access_token_type VARCHAR(50) COMMENT 'Token类型（Bearer）',
    access_token_scopes TEXT COMMENT 'Access Token的Scope列表',

    -- Refresh Token 相关
    refresh_token_value TEXT COMMENT 'Refresh Token值（加密存储）',
    refresh_token_issued_at TIMESTAMP COMMENT 'Refresh Token签发时间',
    refresh_token_expires_at TIMESTAMP COMMENT 'Refresh Token过期时间',
    refresh_token_metadata TEXT COMMENT 'Refresh Token元数据（JSON）',

    -- ID Token 相关（OIDC）
    oidc_id_token_value TEXT COMMENT 'ID Token值',
    oidc_id_token_issued_at TIMESTAMP COMMENT 'ID Token签发时间',
    oidc_id_token_expires_at TIMESTAMP COMMENT 'ID Token过期时间',
    oidc_id_token_metadata TEXT COMMENT 'ID Token元数据（JSON）',

    -- 状态与扩展
    state VARCHAR(500) COMMENT 'State参数',
    attributes TEXT COMMENT '扩展属性（JSON）',

    -- BaseEntity 公共字段
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TIMESTAMP COMMENT '软删除时间戳'
);

-- 索引
CREATE INDEX idx_oauth2_auth_client_id ON oauth2_authorizations (client_id) WHERE deleted IS NULL;
CREATE INDEX idx_oauth2_auth_principal ON oauth2_authorizations (principal_name) WHERE deleted IS NULL;
CREATE INDEX idx_oauth2_auth_code ON oauth2_authorizations (authorization_code_value(100)) WHERE deleted IS NULL;
CREATE INDEX idx_oauth2_auth_access_token ON oauth2_authorizations (access_token_value(100)) WHERE deleted IS NULL;
CREATE INDEX idx_oauth2_auth_refresh_token ON oauth2_authorizations (refresh_token_value(100)) WHERE deleted IS NULL;
CREATE INDEX idx_oauth2_auth_expires ON oauth2_authorizations (access_token_expires_at) WHERE deleted IS NULL;

COMMENT ON TABLE oauth2_authorizations IS 'OAuth2授权记录表（包含授权码、Token等）';
```

### 4.3 OAuth2 授权同意表（oauth2_authorization_consents）

存储用户对客户端的授权同意记录（用户授权某客户端访问哪些 Scope）。

```sql
-- V69__Create_oauth2_authorization_consents_table.sql
CREATE TABLE oauth2_authorization_consents (
    id VARCHAR(36) PRIMARY KEY,

    -- 关联信息
    client_id VARCHAR(100) NOT NULL COMMENT '客户端ID',
    principal_name VARCHAR(255) NOT NULL COMMENT '用户标识（用户ID）',

    -- 授权信息
    authorities TEXT NOT NULL COMMENT '授权的权限列表（Scope，JSON数组）',

    -- 审计字段
    consent_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '授权同意时间',

    -- BaseEntity 公共字段
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TIMESTAMP COMMENT '软删除时间戳'
);

-- 唯一索引：同一用户对同一客户端只有一条有效的授权同意记录
CREATE UNIQUE INDEX uk_oauth2_consent_unique ON oauth2_authorization_consents (client_id, principal_name) WHERE deleted IS NULL;
CREATE INDEX idx_oauth2_consent_client ON oauth2_authorization_consents (client_id) WHERE deleted IS NULL;
CREATE INDEX idx_oauth2_consent_principal ON oauth2_authorization_consents (principal_name) WHERE deleted IS NULL;

COMMENT ON TABLE oauth2_authorization_consents IS 'OAuth2用户授权同意表';
```

### 4.4 枚举类型设计

#### 4.4.1 授权类型枚举（GrantType）
```java
package org.xhy.community.domain.oauth2.valueobject;

public enum GrantType {
    AUTHORIZATION_CODE("authorization_code", "授权码模式"),
    REFRESH_TOKEN("refresh_token", "刷新令牌模式"),
    CLIENT_CREDENTIALS("client_credentials", "客户端凭证模式");

    private final String value;
    private final String description;

    GrantType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}
```

#### 4.4.2 客户端认证方式枚举（ClientAuthenticationMethod）
```java
package org.xhy.community.domain.oauth2.valueobject;

public enum ClientAuthenticationMethod {
    CLIENT_SECRET_BASIC("client_secret_basic", "Basic认证"),
    CLIENT_SECRET_POST("client_secret_post", "POST参数认证"),
    NONE("none", "无需认证（公开客户端）");

    private final String value;
    private final String description;

    ClientAuthenticationMethod(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}
```

#### 4.4.3 客户端状态枚举（OAuth2ClientStatus）
```java
package org.xhy.community.domain.oauth2.valueobject;

public enum OAuth2ClientStatus {
    ACTIVE("激活", "客户端正常可用"),
    SUSPENDED("暂停", "客户端被暂停使用"),
    REVOKED("撤销", "客户端被永久撤销");

    private final String label;
    private final String description;

    OAuth2ClientStatus(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
}
```

### 4.5 枚举类型转换器配置

需要在 `MyBatisTypeHandlerConfig` 中注册枚举转换器：

```java
@Configuration
public class MyBatisTypeHandlerConfig {

    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> {
            // ... 现有的转换器注册 ...

            // OAuth2 枚举转换器
            configuration.getTypeHandlerRegistry().register(GrantType.class,
                new EnumTypeHandler<>(GrantType.class));
            configuration.getTypeHandlerRegistry().register(ClientAuthenticationMethod.class,
                new EnumTypeHandler<>(ClientAuthenticationMethod.class));
            configuration.getTypeHandlerRegistry().register(OAuth2ClientStatus.class,
                new EnumTypeHandler<>(OAuth2ClientStatus.class));
        };
    }
}
```

## 5. Domain 层实现

### 5.1 OAuth2ClientEntity（客户端实体）

```java
package org.xhy.community.domain.oauth2.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;
import org.xhy.community.domain.oauth2.valueobject.OAuth2ClientStatus;
import org.xhy.community.infrastructure.converter.OAuth2ClientStatusConverter;

import java.util.List;

@TableName("oauth2_clients")
public class OAuth2ClientEntity extends BaseEntity {

    /** 客户端ID（对外暴露） */
    private String clientId;

    /** 客户端密钥（加密存储） */
    private String clientSecretEnc;

    /** 客户端名称 */
    private String clientName;

    /** 客户端描述 */
    private String clientDescription;

    /** 客户端Logo URL */
    private String clientLogoUrl;

    /** 客户端官网地址 */
    private String clientWebsiteUrl;

    /** 联系邮箱 */
    private String contactEmail;

    /** 联系人姓名 */
    private String contactName;

    /** 重定向URI列表（JSON存储） */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private List<String> redirectUris;

    /** 授权类型列表（JSON存储） */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private List<String> grantTypes;

    /** 允许的Scope列表（JSON存储） */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private List<String> scopes;

    /** 客户端认证方式（JSON存储） */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private List<String> clientAuthenticationMethods;

    /** Access Token有效期（秒） */
    private Integer accessTokenValiditySeconds;

    /** Refresh Token有效期（秒） */
    private Integer refreshTokenValiditySeconds;

    /** 是否强制要求PKCE */
    private Boolean requireProofKey;

    /** 是否需要用户授权同意 */
    private Boolean requireAuthorizationConsent;

    /** 客户端状态 */
    @TableField(typeHandler = OAuth2ClientStatusConverter.class)
    private OAuth2ClientStatus status;

    /** 创建人用户ID */
    private String createdBy;

    // 构造函数
    public OAuth2ClientEntity() {
        this.status = OAuth2ClientStatus.ACTIVE;
        this.accessTokenValiditySeconds = 3600; // 默认1小时
        this.refreshTokenValiditySeconds = 2592000; // 默认30天
        this.requireProofKey = false;
        this.requireAuthorizationConsent = true;
    }

    // 业务方法

    /**
     * 激活客户端
     */
    public void activate() {
        this.status = OAuth2ClientStatus.ACTIVE;
    }

    /**
     * 暂停客户端
     */
    public void suspend() {
        this.status = OAuth2ClientStatus.SUSPENDED;
    }

    /**
     * 撤销客户端
     */
    public void revoke() {
        this.status = OAuth2ClientStatus.REVOKED;
    }

    /**
     * 检查客户端是否可用
     */
    public boolean isActive() {
        return OAuth2ClientStatus.ACTIVE.equals(this.status);
    }

    /**
     * 验证重定向URI是否合法
     */
    public boolean isValidRedirectUri(String redirectUri) {
        return redirectUris != null && redirectUris.contains(redirectUri);
    }

    /**
     * 验证授权类型是否支持
     */
    public boolean isGrantTypeSupported(String grantType) {
        return grantTypes != null && grantTypes.contains(grantType);
    }

    /**
     * 验证Scope是否允许
     */
    public boolean isScopeAllowed(String scope) {
        return scopes != null && scopes.contains(scope);
    }

    // Getters and Setters（省略）
    // ...
}
```

### 5.2 OAuth2ClientRepository（客户端仓储）

```java
package org.xhy.community.domain.oauth2.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xhy.community.domain.oauth2.entity.OAuth2ClientEntity;

@Mapper
public interface OAuth2ClientRepository extends BaseMapper<OAuth2ClientEntity> {
    // 继承 BaseMapper，使用 MyBatis Plus 提供的方法
}
```

### 5.3 OAuth2ClientDomainService（客户端领域服务）

```java
package org.xhy.community.domain.oauth2.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.xhy.community.domain.oauth2.entity.OAuth2ClientEntity;
import org.xhy.community.domain.oauth2.repository.OAuth2ClientRepository;
import org.xhy.community.domain.oauth2.valueobject.OAuth2ClientStatus;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.OAuth2ErrorCode;

import java.util.UUID;

@Service
public class OAuth2ClientDomainService {

    private final OAuth2ClientRepository oauth2ClientRepository;
    private final PasswordEncoder passwordEncoder;

    public OAuth2ClientDomainService(OAuth2ClientRepository oauth2ClientRepository,
                                     PasswordEncoder passwordEncoder) {
        this.oauth2ClientRepository = oauth2ClientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 创建OAuth2客户端
     */
    public OAuth2ClientEntity createClient(OAuth2ClientEntity client) {
        // 业务规则：clientId 必须唯一
        validateClientIdUnique(client.getClientId(), null);

        // 加密存储客户端密钥
        String plainSecret = client.getClientSecretEnc();
        String encryptedSecret = passwordEncoder.encode(plainSecret);
        client.setClientSecretEnc(encryptedSecret);

        oauth2ClientRepository.insert(client);
        return client;
    }

    /**
     * 更新OAuth2客户端
     */
    public OAuth2ClientEntity updateClient(OAuth2ClientEntity client) {
        // 业务规则：clientId 必须唯一
        validateClientIdUnique(client.getClientId(), client.getId());

        // 如果密钥被更新，需要重新加密
        OAuth2ClientEntity existing = oauth2ClientRepository.selectById(client.getId());
        if (existing == null) {
            throw new BusinessException(OAuth2ErrorCode.CLIENT_NOT_FOUND);
        }

        // 如果密钥被修改，重新加密
        if (!client.getClientSecretEnc().equals(existing.getClientSecretEnc())) {
            String encryptedSecret = passwordEncoder.encode(client.getClientSecretEnc());
            client.setClientSecretEnc(encryptedSecret);
        }

        oauth2ClientRepository.updateById(client);
        return client;
    }

    /**
     * 重新生成客户端密钥
     */
    public String regenerateClientSecret(String clientId) {
        OAuth2ClientEntity client = getClientByClientId(clientId);

        // 生成新的密钥
        String newSecret = generateClientSecret();
        String encryptedSecret = passwordEncoder.encode(newSecret);

        client.setClientSecretEnc(encryptedSecret);
        oauth2ClientRepository.updateById(client);

        // 返回明文密钥（仅此一次机会获取）
        return newSecret;
    }

    /**
     * 根据客户端ID查询客户端
     */
    public OAuth2ClientEntity getClientByClientId(String clientId) {
        LambdaQueryWrapper<OAuth2ClientEntity> queryWrapper = new LambdaQueryWrapper<OAuth2ClientEntity>()
            .eq(OAuth2ClientEntity::getClientId, clientId);

        OAuth2ClientEntity client = oauth2ClientRepository.selectOne(queryWrapper);
        if (client == null) {
            throw new BusinessException(OAuth2ErrorCode.CLIENT_NOT_FOUND);
        }
        return client;
    }

    /**
     * 根据ID查询客户端
     */
    public OAuth2ClientEntity getClientById(String id) {
        OAuth2ClientEntity client = oauth2ClientRepository.selectById(id);
        if (client == null) {
            throw new BusinessException(OAuth2ErrorCode.CLIENT_NOT_FOUND);
        }
        return client;
    }

    /**
     * 分页查询客户端列表
     */
    public IPage<OAuth2ClientEntity> pageClients(Integer pageNum, Integer pageSize,
                                                  String clientName, OAuth2ClientStatus status) {
        Page<OAuth2ClientEntity> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<OAuth2ClientEntity> queryWrapper = new LambdaQueryWrapper<OAuth2ClientEntity>()
            .like(clientName != null, OAuth2ClientEntity::getClientName, clientName)
            .eq(status != null, OAuth2ClientEntity::getStatus, status)
            .orderByDesc(OAuth2ClientEntity::getCreateTime);

        return oauth2ClientRepository.selectPage(page, queryWrapper);
    }

    /**
     * 验证客户端密钥
     */
    public boolean validateClientSecret(String clientId, String rawSecret) {
        OAuth2ClientEntity client = getClientByClientId(clientId);
        return passwordEncoder.matches(rawSecret, client.getClientSecretEnc());
    }

    /**
     * 删除客户端（软删除）
     */
    public void deleteClient(String id) {
        oauth2ClientRepository.deleteById(id);
    }

    // 私有方法

    /**
     * 验证clientId唯一性
     */
    private void validateClientIdUnique(String clientId, String excludeId) {
        LambdaQueryWrapper<OAuth2ClientEntity> queryWrapper = new LambdaQueryWrapper<OAuth2ClientEntity>()
            .eq(OAuth2ClientEntity::getClientId, clientId)
            .ne(excludeId != null, OAuth2ClientEntity::getId, excludeId);

        if (oauth2ClientRepository.exists(queryWrapper)) {
            throw new BusinessException(OAuth2ErrorCode.CLIENT_ID_ALREADY_EXISTS);
        }
    }

    /**
     * 生成客户端密钥（32位随机字符串）
     */
    private String generateClientSecret() {
        return UUID.randomUUID().toString().replace("-", "") +
               UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }
}
```


## 6. Application 层实现

### 6.1 OAuth2ClientDTO（客户端信息DTO）

```java
package org.xhy.community.application.oauth2.dto;

import java.time.LocalDateTime;
import java.util.List;

public class OAuth2ClientDTO {

    private String id;
    private String clientId;
    private String clientName;
    private String clientDescription;
    private String clientLogoUrl;
    private String clientWebsiteUrl;
    private String contactEmail;
    private String contactName;

    private List<String> redirectUris;
    private List<String> grantTypes;
    private List<String> scopes;
    private List<String> clientAuthenticationMethods;

    private Integer accessTokenValiditySeconds;
    private Integer refreshTokenValiditySeconds;
    private Boolean requireProofKey;
    private Boolean requireAuthorizationConsent;

    private String status;
    private String createdBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // Getters and Setters（省略）
}
```

### 6.2 OAuth2ClientAssembler（客户端转换器）

```java
package org.xhy.community.application.oauth2.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.oauth2.dto.OAuth2ClientDTO;
import org.xhy.community.domain.oauth2.entity.OAuth2ClientEntity;
import org.xhy.community.interfaces.oauth2.request.CreateOAuth2ClientRequest;
import org.xhy.community.interfaces.oauth2.request.UpdateOAuth2ClientRequest;

import java.util.List;
import java.util.stream.Collectors;

public class OAuth2ClientAssembler {

    /**
     * Entity 转 DTO
     */
    public static OAuth2ClientDTO toDTO(OAuth2ClientEntity entity) {
        if (entity == null) {
            return null;
        }

        OAuth2ClientDTO dto = new OAuth2ClientDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        return dto;
    }

    /**
     * Entity 列表转 DTO 列表
     */
    public static List<OAuth2ClientDTO> toDTOList(List<OAuth2ClientEntity> entities) {
        return entities.stream()
            .map(OAuth2ClientAssembler::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * CreateRequest 转 Entity
     */
    public static OAuth2ClientEntity fromCreateRequest(CreateOAuth2ClientRequest request) {
        if (request == null) {
            return null;
        }

        OAuth2ClientEntity entity = new OAuth2ClientEntity();
        BeanUtils.copyProperties(request, entity);
        return entity;
    }

    /**
     * UpdateRequest 转 Entity
     */
    public static OAuth2ClientEntity fromUpdateRequest(UpdateOAuth2ClientRequest request, String id) {
        if (request == null) {
            return null;
        }

        OAuth2ClientEntity entity = new OAuth2ClientEntity();
        BeanUtils.copyProperties(request, entity);
        entity.setId(id);
        return entity;
    }
}
```

### 6.3 AdminOAuth2ClientAppService（管理后台应用服务）

```java
package org.xhy.community.application.oauth2.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xhy.community.application.oauth2.assembler.OAuth2ClientAssembler;
import org.xhy.community.application.oauth2.dto.OAuth2ClientDTO;
import org.xhy.community.domain.oauth2.entity.OAuth2ClientEntity;
import org.xhy.community.domain.oauth2.service.OAuth2ClientDomainService;
import org.xhy.community.domain.oauth2.valueobject.OAuth2ClientStatus;
import org.xhy.community.interfaces.oauth2.request.CreateOAuth2ClientRequest;
import org.xhy.community.interfaces.oauth2.request.UpdateOAuth2ClientRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminOAuth2ClientAppService {

    private final OAuth2ClientDomainService oauth2ClientDomainService;

    public AdminOAuth2ClientAppService(OAuth2ClientDomainService oauth2ClientDomainService) {
        this.oauth2ClientDomainService = oauth2ClientDomainService;
    }

    /**
     * 创建OAuth2客户端
     * @return 包含 clientId 和 clientSecret 的 Map（clientSecret 仅此一次返回）
     */
    @Transactional
    public Map<String, Object> createClient(CreateOAuth2ClientRequest request, String createdBy) {
        OAuth2ClientEntity entity = OAuth2ClientAssembler.fromCreateRequest(request);
        entity.setCreatedBy(createdBy);

        // 临时保存明文密钥（创建时需要返回给管理员）
        String plainSecret = entity.getClientSecretEnc();

        OAuth2ClientEntity createdClient = oauth2ClientDomainService.createClient(entity);

        OAuth2ClientDTO dto = OAuth2ClientAssembler.toDTO(createdClient);

        Map<String, Object> result = new HashMap<>();
        result.put("client", dto);
        result.put("clientSecret", plainSecret); // 仅此一次返回明文密钥
        return result;
    }

    /**
     * 更新OAuth2客户端
     */
    @Transactional
    public OAuth2ClientDTO updateClient(String clientId, UpdateOAuth2ClientRequest request) {
        OAuth2ClientEntity entity = OAuth2ClientAssembler.fromUpdateRequest(request, clientId);
        OAuth2ClientEntity updatedClient = oauth2ClientDomainService.updateClient(entity);
        return OAuth2ClientAssembler.toDTO(updatedClient);
    }

    /**
     * 重新生成客户端密钥
     * @return 新的客户端密钥（明文，仅此一次返回）
     */
    @Transactional
    public Map<String, String> regenerateClientSecret(String clientId) {
        String newSecret = oauth2ClientDomainService.regenerateClientSecret(clientId);

        Map<String, String> result = new HashMap<>();
        result.put("clientId", clientId);
        result.put("clientSecret", newSecret); // 仅此一次返回明文密钥
        return result;
    }

    /**
     * 根据ID查询客户端
     */
    public OAuth2ClientDTO getClientById(String id) {
        OAuth2ClientEntity client = oauth2ClientDomainService.getClientById(id);
        return OAuth2ClientAssembler.toDTO(client);
    }

    /**
     * 分页查询客户端列表
     */
    public IPage<OAuth2ClientDTO> pageClients(Integer pageNum, Integer pageSize,
                                               String clientName, String status) {
        OAuth2ClientStatus statusEnum = status != null ? OAuth2ClientStatus.valueOf(status) : null;

        IPage<OAuth2ClientEntity> page = oauth2ClientDomainService.pageClients(
            pageNum, pageSize, clientName, statusEnum
        );

        return page.convert(OAuth2ClientAssembler::toDTO);
    }

    /**
     * 删除客户端
     */
    @Transactional
    public void deleteClient(String id) {
        oauth2ClientDomainService.deleteClient(id);
    }

    /**
     * 激活客户端
     */
    @Transactional
    public void activateClient(String id) {
        OAuth2ClientEntity client = oauth2ClientDomainService.getClientById(id);
        client.activate();
        oauth2ClientDomainService.updateClient(client);
    }

    /**
     * 暂停客户端
     */
    @Transactional
    public void suspendClient(String id) {
        OAuth2ClientEntity client = oauth2ClientDomainService.getClientById(id);
        client.suspend();
        oauth2ClientDomainService.updateClient(client);
    }

    /**
     * 撤销客户端
     */
    @Transactional
    public void revokeClient(String id) {
        OAuth2ClientEntity client = oauth2ClientDomainService.getClientById(id);
        client.revoke();
        oauth2ClientDomainService.updateClient(client);
    }
}
```

## 7. Infrastructure 层实现

### 7.1 Maven 依赖

首先在 `pom.xml` 中添加 Spring Authorization Server 依赖：

```xml
<!-- Spring Authorization Server -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-authorization-server</artifactId>
    <version>1.2.1</version>
</dependency>

<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Jose (JWT/JWK支持) -->
<dependency>
    <groupId>com.nimbusds</groupId>
    <artifactId>nimbus-jose-jwt</artifactId>
    <version>9.37.3</version>
</dependency>
```

### 7.2 AuthorizationServerConfig（核心配置）

```java
package org.xhy.community.infrastructure.oauth2.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.xhy.community.infrastructure.oauth2.service.CustomRegisteredClientRepository;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.UUID;

@Configuration
public class AuthorizationServerConfig {

    /**
     * OAuth2 授权服务器安全过滤链
     * Order(1) 确保优先级高于默认的安全配置
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        // 应用默认的 OAuth2 授权服务器配置
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
            // 启用 OIDC 1.0 支持
            .oidc(Customizer.withDefaults());

        // 配置异常处理：未认证时重定向到前端登录页面
        // 前端需要处理 OAuth 回调参数并在登录成功后重定向回授权页面
        http.exceptionHandling(exceptions -> exceptions
            .defaultAuthenticationEntryPointFor(
                new LoginUrlAuthenticationEntryPoint("/login"), // 前端路由
                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
            )
        );

        // 启用 OAuth2 资源服务器（用于验证 Access Token）
        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    /**
     * 注册客户端存储服务
     * 从数据库读取客户端配置
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository(
            CustomRegisteredClientRepository customRegisteredClientRepository) {
        return customRegisteredClientRepository;
    }

    /**
     * JWT 解码器
     */
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    /**
     * JWT 签名密钥源（JWK Set）
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .keyID(UUID.randomUUID().toString())
            .build();

        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    /**
     * 生成 RSA 密钥对
     */
    private static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate RSA key pair", ex);
        }
        return keyPair;
    }

    /**
     * 授权服务器设置
     * 配置各个端点的路径
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
            .issuer("https://qiaoya.com") // 改为实际域名
            .authorizationEndpoint("/oauth2/authorize")
            .tokenEndpoint("/oauth2/token")
            .tokenIntrospectionEndpoint("/oauth2/introspect")
            .tokenRevocationEndpoint("/oauth2/revoke")
            .jwkSetEndpoint("/oauth2/jwks")
            .oidcUserInfoEndpoint("/oauth2/userinfo")
            .oidcClientRegistrationEndpoint("/oauth2/register") // 动态客户端注册（可选）
            .build();
    }

    /**
     * Token 设置
     */
    @Bean
    public TokenSettings tokenSettings() {
        return TokenSettings.builder()
            .accessTokenTimeToLive(Duration.ofHours(1))      // Access Token 有效期 1小时
            .refreshTokenTimeToLive(Duration.ofDays(30))     // Refresh Token 有效期 30天
            .reuseRefreshTokens(true)                         // 刷新后复用 Refresh Token
            .build();
    }
}
```

### 7.3 CustomRegisteredClientRepository（客户端存储适配器）

```java
package org.xhy.community.infrastructure.oauth2.service;

import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;
import org.xhy.community.domain.oauth2.entity.OAuth2ClientEntity;
import org.xhy.community.domain.oauth2.service.OAuth2ClientDomainService;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 自定义客户端存储适配器
 * 将数据库中的 OAuth2ClientEntity 转换为 Spring Authorization Server 的 RegisteredClient
 */
@Service
public class CustomRegisteredClientRepository implements RegisteredClientRepository {

    private final OAuth2ClientDomainService oauth2ClientDomainService;

    public CustomRegisteredClientRepository(OAuth2ClientDomainService oauth2ClientDomainService) {
        this.oauth2ClientDomainService = oauth2ClientDomainService;
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        // 动态客户端注册时调用（本方案暂不支持）
        throw new UnsupportedOperationException("Dynamic client registration is not supported");
    }

    @Override
    public RegisteredClient findById(String id) {
        OAuth2ClientEntity client = oauth2ClientDomainService.getClientById(id);
        return toRegisteredClient(client);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        try {
            OAuth2ClientEntity client = oauth2ClientDomainService.getClientByClientId(clientId);
            return toRegisteredClient(client);
        } catch (Exception e) {
            return null; // 客户端不存在时返回 null
        }
    }

    /**
     * 将 OAuth2ClientEntity 转换为 RegisteredClient
     */
    private RegisteredClient toRegisteredClient(OAuth2ClientEntity client) {
        if (client == null || !client.isActive()) {
            return null;
        }

        // 转换授权类型
        Set<AuthorizationGrantType> grantTypes = client.getGrantTypes().stream()
            .map(AuthorizationGrantType::new)
            .collect(Collectors.toSet());

        // 转换客户端认证方式
        Set<ClientAuthenticationMethod> authMethods = client.getClientAuthenticationMethods().stream()
            .map(ClientAuthenticationMethod::new)
            .collect(Collectors.toSet());

        // 构建 RegisteredClient
        return RegisteredClient.withId(client.getId())
            .clientId(client.getClientId())
            .clientSecret(client.getClientSecretEnc()) // 已加密的密钥
            .clientName(client.getClientName())
            .clientAuthenticationMethods(methods -> methods.addAll(authMethods))
            .authorizationGrantTypes(types -> types.addAll(grantTypes))
            .redirectUris(uris -> uris.addAll(client.getRedirectUris()))
            .scopes(scopes -> scopes.addAll(client.getScopes()))
            .tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofSeconds(client.getAccessTokenValiditySeconds()))
                .refreshTokenTimeToLive(Duration.ofSeconds(client.getRefreshTokenValiditySeconds()))
                .build())
            .clientSettings(ClientSettings.builder()
                .requireProofKey(client.getRequireProofKey())
                .requireAuthorizationConsent(client.getRequireAuthorizationConsent())
                .build())
            .build();
    }
}
```

### 7.4 OAuth2SecurityConfig（全局安全配置）

```java
package org.xhy.community.infrastructure.oauth2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class OAuth2SecurityConfig {

    /**
     * 默认安全过滤链
     * Order(2) 优先级低于授权服务器过滤链
     */
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // OAuth2 公开端点
                .requestMatchers("/oauth2/**", "/.well-known/**").permitAll()
                // 公开 API
                .requestMatchers("/api/public/**", "/api/auth/**").permitAll()
                // 其他请求需要认证
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable()) // OAuth2 客户端使用 PKCE 防止 CSRF
            .formLogin(form -> form
                .loginPage("/api/auth/login")
                .permitAll()
            );

        return http.build();
    }

    /**
     * 密码编码器（与现有系统保持一致）
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```


## 8. Interfaces 层实现

### 8.1 CreateOAuth2ClientRequest（创建客户端请求）

```java
package org.xhy.community.interfaces.oauth2.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CreateOAuth2ClientRequest {

    @NotBlank(message = "客户端ID不能为空")
    @Size(max = 100, message = "客户端ID长度不能超过100")
    private String clientId;

    @NotBlank(message = "客户端密钥不能为空")
    @Size(min = 32, max = 64, message = "客户端密钥长度必须在32-64之间")
    private String clientSecretEnc;

    @NotBlank(message = "客户端名称不能为空")
    @Size(max = 200, message = "客户端名称长度不能超过200")
    private String clientName;

    @Size(max = 2000, message = "客户端描述长度不能超过2000")
    private String clientDescription;

    private String clientLogoUrl;
    private String clientWebsiteUrl;

    @Email(message = "联系邮箱格式不正确")
    private String contactEmail;

    private String contactName;

    @NotEmpty(message = "重定向URI列表不能为空")
    private List<String> redirectUris;

    @NotEmpty(message = "授权类型列表不能为空")
    private List<String> grantTypes;

    @NotEmpty(message = "Scope列表不能为空")
    private List<String> scopes;

    @NotEmpty(message = "客户端认证方式列表不能为空")
    private List<String> clientAuthenticationMethods;

    private Integer accessTokenValiditySeconds = 3600;
    private Integer refreshTokenValiditySeconds = 2592000;
    private Boolean requireProofKey = false;
    private Boolean requireAuthorizationConsent = true;

    // Getters and Setters（省略）
}
```

### 8.2 UpdateOAuth2ClientRequest（更新客户端请求）

```java
package org.xhy.community.interfaces.oauth2.request;

public class UpdateOAuth2ClientRequest extends CreateOAuth2ClientRequest {
    // 继承创建请求，差异仅在于主键（通过路径参数传递）
}
```

### 8.3 OAuth2ClientQueryRequest（客户端查询请求）

```java
package org.xhy.community.interfaces.oauth2.request;

import org.xhy.community.interfaces.common.request.PageRequest;

public class OAuth2ClientQueryRequest extends PageRequest {

    private String clientName;
    private String status; // ACTIVE, SUSPENDED, REVOKED

    // Getters and Setters（省略）
}
```

### 8.4 AdminOAuth2ClientController（管理后台控制器）

```java
package org.xhy.community.interfaces.oauth2.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.oauth2.dto.OAuth2ClientDTO;
import org.xhy.community.application.oauth2.service.AdminOAuth2ClientAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.context.UserContext;
import org.xhy.community.interfaces.oauth2.request.CreateOAuth2ClientRequest;
import org.xhy.community.interfaces.oauth2.request.OAuth2ClientQueryRequest;
import org.xhy.community.interfaces.oauth2.request.UpdateOAuth2ClientRequest;

import java.util.Map;

/**
 * OAuth2 客户端管理后台接口
 * 需要管理员权限
 */
@RestController
@RequestMapping("/api/admin/oauth2/clients")
public class AdminOAuth2ClientController {

    private final AdminOAuth2ClientAppService adminOAuth2ClientAppService;

    public AdminOAuth2ClientController(AdminOAuth2ClientAppService adminOAuth2ClientAppService) {
        this.adminOAuth2ClientAppService = adminOAuth2ClientAppService;
    }

    /**
     * 创建OAuth2客户端
     * 返回包含 clientSecret 的完整信息（仅此一次）
     */
    @PostMapping
    public ApiResponse<Map<String, Object>> createClient(@Valid @RequestBody CreateOAuth2ClientRequest request) {
        String createdBy = UserContext.getUserId();
        Map<String, Object> result = adminOAuth2ClientAppService.createClient(request, createdBy);
        return ApiResponse.success("OAuth2客户端创建成功", result);
    }

    /**
     * 更新OAuth2客户端
     */
    @PutMapping("/{id}")
    public ApiResponse<OAuth2ClientDTO> updateClient(@PathVariable String id,
                                                      @Valid @RequestBody UpdateOAuth2ClientRequest request) {
        OAuth2ClientDTO client = adminOAuth2ClientAppService.updateClient(id, request);
        return ApiResponse.success("OAuth2客户端更新成功", client);
    }

    /**
     * 重新生成客户端密钥
     * 返回新的密钥（仅此一次）
     */
    @PostMapping("/{id}/regenerate-secret")
    public ApiResponse<Map<String, String>> regenerateClientSecret(@PathVariable String id) {
        Map<String, String> result = adminOAuth2ClientAppService.regenerateClientSecret(id);
        return ApiResponse.success("客户端密钥已重新生成", result);
    }

    /**
     * 根据ID查询客户端
     */
    @GetMapping("/{id}")
    public ApiResponse<OAuth2ClientDTO> getClientById(@PathVariable String id) {
        OAuth2ClientDTO client = adminOAuth2ClientAppService.getClientById(id);
        return ApiResponse.success(client);
    }

    /**
     * 分页查询客户端列表
     */
    @GetMapping
    public ApiResponse<IPage<OAuth2ClientDTO>> pageClients(@Valid OAuth2ClientQueryRequest request) {
        IPage<OAuth2ClientDTO> page = adminOAuth2ClientAppService.pageClients(
            request.getPageNum(),
            request.getPageSize(),
            request.getClientName(),
            request.getStatus()
        );
        return ApiResponse.success(page);
    }

    /**
     * 删除客户端
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteClient(@PathVariable String id) {
        adminOAuth2ClientAppService.deleteClient(id);
        return ApiResponse.success("OAuth2客户端已删除");
    }

    /**
     * 激活客户端
     */
    @PostMapping("/{id}/activate")
    public ApiResponse<Void> activateClient(@PathVariable String id) {
        adminOAuth2ClientAppService.activateClient(id);
        return ApiResponse.success("OAuth2客户端已激活");
    }

    /**
     * 暂停客户端
     */
    @PostMapping("/{id}/suspend")
    public ApiResponse<Void> suspendClient(@PathVariable String id) {
        adminOAuth2ClientAppService.suspendClient(id);
        return ApiResponse.success("OAuth2客户端已暂停");
    }

    /**
     * 撤销客户端
     */
    @PostMapping("/{id}/revoke")
    public ApiResponse<Void> revokeClient(@PathVariable String id) {
        adminOAuth2ClientAppService.revokeClient(id);
        return ApiResponse.success("OAuth2客户端已撤销");
    }
}
```

### 8.5 OAuth2UserInfoController（OIDC UserInfo 端点）

```java
package org.xhy.community.interfaces.oauth2.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xhy.community.application.user.dto.UserDTO;
import org.xhy.community.application.user.service.UserAppService;

import java.util.HashMap;
import java.util.Map;

/**
 * OIDC UserInfo 端点
 * 第三方应用使用 Access Token 获取用户信息
 */
@RestController
@RequestMapping("/oauth2")
public class OAuth2UserInfoController {

    private final UserAppService userAppService;

    public OAuth2UserInfoController(UserAppService userAppService) {
        this.userAppService = userAppService;
    }

    /**
     * 获取用户信息（OIDC UserInfo 端点）
     * 第三方应用携带 Access Token 请求
     */
    @GetMapping("/userinfo")
    public Map<String, Object> userInfo(Authentication authentication) {
        // 从 JWT Token 中提取用户ID
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String userId = jwt.getSubject();

        // 查询用户信息
        UserDTO user = userAppService.getUserById(userId);

        // 构建 OIDC UserInfo 响应
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("sub", user.getId());              // subject (用户唯一标识)
        userInfo.put("name", user.getName());           // 用户姓名
        userInfo.put("email", user.getEmail());         // 用户邮箱
        userInfo.put("avatar", user.getAvatar());       // 用户头像
        userInfo.put("email_verified", true);           // 邮箱已验证

        // 可根据 scope 返回更多信息
        // if (hasScope(jwt, "profile")) { ... }

        return userInfo;
    }
}
```

## 9. 异常处理与错误码

### 9.1 OAuth2ErrorCode（OAuth2 异常错误码）

```java
package org.xhy.community.infrastructure.exception;

public enum OAuth2ErrorCode implements ErrorCode {

    CLIENT_NOT_FOUND(3201, "OAuth2客户端不存在"),
    CLIENT_ID_ALREADY_EXISTS(3202, "客户端ID已存在"),
    CLIENT_SUSPENDED(3203, "客户端已被暂停"),
    CLIENT_REVOKED(3204, "客户端已被撤销"),
    INVALID_CLIENT_CREDENTIALS(3205, "客户端认证失败"),
    INVALID_REDIRECT_URI(3206, "无效的重定向URI"),
    INVALID_GRANT_TYPE(3207, "不支持的授权类型"),
    INVALID_SCOPE(3208, "无效的Scope"),
    AUTHORIZATION_DENIED(3209, "用户拒绝授权"),
    INVALID_AUTHORIZATION_CODE(3210, "无效的授权码"),
    EXPIRED_AUTHORIZATION_CODE(3211, "授权码已过期"),
    INVALID_ACCESS_TOKEN(3212, "无效的Access Token"),
    EXPIRED_ACCESS_TOKEN(3213, "Access Token已过期"),
    INVALID_REFRESH_TOKEN(3214, "无效的Refresh Token"),
    EXPIRED_REFRESH_TOKEN(3215, "Refresh Token已过期");

    private final int code;
    private final String message;

    OAuth2ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
```

## 10. 安全设计

### 10.1 客户端密钥安全
- **加密存储**：使用 `BCryptPasswordEncoder` 加密客户端密钥
- **仅一次可见**：创建/重置密钥时返回明文，后续无法查看
- **定期轮换**：建议客户端定期更换密钥

### 10.2 PKCE 支持（防止授权码拦截）
- **Code Challenge**：客户端生成随机字符串 `code_verifier`，计算 `code_challenge = SHA256(code_verifier)`
- **授权请求**：携带 `code_challenge` 和 `code_challenge_method=S256`
- **Token 请求**：携带原始 `code_verifier`，服务端验证 `SHA256(code_verifier) == code_challenge`

### 10.3 State 参数（防止 CSRF）
- 客户端生成随机 `state` 参数
- 授权服务器回调时原样返回 `state`
- 客户端验证 `state` 是否一致

### 10.4 Scope 权限控制
- 管理员为每个客户端配置允许的 `scopes` 列表
- 授权时校验请求的 `scope` 是否在允许范围内
- 用户授权页面展示具体的权限说明

### 10.5 Token 安全
- **JWT 签名**：使用 RSA 私钥签名，公钥公开（/oauth2/jwks 端点）
- **短生命周期**：Access Token 默认 1小时，Refresh Token 默认 30天
- **Token 撤销**：支持通过 `/oauth2/revoke` 端点撤销 Token

### 10.6 审计日志
- 记录客户端创建、更新、删除
- 记录用户授权、拒绝授权
- 记录 Token 颁发、刷新、撤销

### 10.7 速率限制
- 限制同一客户端的授权请求频率
- 限制 Token 刷新频率
- 限制用户授权页面访问频率

## 11. 用户授权同意页面

### 11.1 授权同意流程
```
1. 用户访问授权端点 /oauth2/authorize
2. 系统检查用户是否已登录（未登录则重定向到登录页）
3. 系统检查用户是否已授权该客户端（首次授权需要用户同意）
4. 展示授权同意页面，显示：
   - 客户端名称、Logo、描述
   - 请求的权限列表（Scope）
   - 同意/拒绝按钮
5. 用户点击同意 → 生成授权码并重定向回客户端
6. 用户点击拒绝 → 重定向回客户端并携带 error=access_denied
```

### 11.2 自定义授权同意页面（示例）

```html
<!-- src/main/resources/templates/oauth2/consent.html -->
<!DOCTYPE html>
<html>
<head>
    <title>授权确认 - 敲鸭社区</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        /* 样式省略 */
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <img src="${client.clientLogoUrl}" alt="${client.clientName}">
            <h2>${client.clientName}</h2>
            <p>请求访问您的敲鸭账号</p>
        </div>

        <div class="permissions">
            <h3>该应用将获得以下权限：</h3>
            <ul>
                <li th:each="scope : ${scopes}">
                    <strong th:text="${scope.label}"></strong>
                    <span th:text="${scope.description}"></span>
                </li>
            </ul>
        </div>

        <div class="user-info">
            <p>您当前登录的账号：<strong th:text="${user.email}"></strong></p>
        </div>

        <div class="actions">
            <form method="post" action="/oauth2/authorize">
                <input type="hidden" name="client_id" th:value="${clientId}">
                <input type="hidden" name="state" th:value="${state}">
                <input type="hidden" name="scope" th:value="${scopeString}">

                <button type="submit" name="consent" value="approve" class="btn-approve">
                    授权
                </button>
                <button type="submit" name="consent" value="deny" class="btn-deny">
                    拒绝
                </button>
            </form>
        </div>

        <div class="footer">
            <p>授权后，该应用可在授权范围内访问您的账号信息。</p>
            <p>您可以随时在账号设置中撤销授权。</p>
        </div>
    </div>
</body>
</html>
```

## 12. 第三方应用集成示例

### 12.1 授权码模式集成（Node.js 示例）

```javascript
// 第三方应用后端代码示例
const express = require('express');
const axios = require('axios');
const crypto = require('crypto');

const app = express();

// OAuth2 配置
const config = {
  clientId: 'your-client-id',
  clientSecret: 'your-client-secret',
  redirectUri: 'https://your-app.com/callback',
  authorizationUrl: 'https://qiaoya.com/oauth2/authorize',
  tokenUrl: 'https://qiaoya.com/oauth2/token',
  userInfoUrl: 'https://qiaoya.com/oauth2/userinfo'
};

// 步骤1：重定向用户到敲鸭授权页面
app.get('/login/qiaoya', (req, res) => {
  // 生成 state 和 code_verifier (PKCE)
  const state = crypto.randomBytes(16).toString('hex');
  const codeVerifier = crypto.randomBytes(32).toString('base64url');
  const codeChallenge = crypto
    .createHash('sha256')
    .update(codeVerifier)
    .digest('base64url');

  // 保存 state 和 codeVerifier 到 session
  req.session.oauthState = state;
  req.session.codeVerifier = codeVerifier;

  // 构建授权 URL
  const authUrl = new URL(config.authorizationUrl);
  authUrl.searchParams.set('response_type', 'code');
  authUrl.searchParams.set('client_id', config.clientId);
  authUrl.searchParams.set('redirect_uri', config.redirectUri);
  authUrl.searchParams.set('scope', 'openid profile email');
  authUrl.searchParams.set('state', state);
  authUrl.searchParams.set('code_challenge', codeChallenge);
  authUrl.searchParams.set('code_challenge_method', 'S256');

  // 重定向用户
  res.redirect(authUrl.toString());
});

// 步骤2：处理授权回调
app.get('/callback', async (req, res) => {
  const { code, state } = req.query;

  // 验证 state
  if (state !== req.session.oauthState) {
    return res.status(400).send('Invalid state parameter');
  }

  try {
    // 步骤3：使用授权码换取 Token
    const tokenResponse = await axios.post(config.tokenUrl, new URLSearchParams({
      grant_type: 'authorization_code',
      code: code,
      redirect_uri: config.redirectUri,
      client_id: config.clientId,
      client_secret: config.clientSecret,
      code_verifier: req.session.codeVerifier
    }), {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    });

    const { access_token, refresh_token, id_token } = tokenResponse.data;

    // 步骤4：使用 Access Token 获取用户信息
    const userInfoResponse = await axios.get(config.userInfoUrl, {
      headers: { Authorization: `Bearer ${access_token}` }
    });

    const userInfo = userInfoResponse.data;

    // 步骤5：创建本地会话
    req.session.user = userInfo;
    req.session.accessToken = access_token;
    req.session.refreshToken = refresh_token;

    res.redirect('/dashboard');
  } catch (error) {
    console.error('OAuth2 error:', error);
    res.status(500).send('Authentication failed');
  }
});

// 步骤6：刷新 Token（可选）
app.post('/refresh-token', async (req, res) => {
  const refreshToken = req.session.refreshToken;

  try {
    const tokenResponse = await axios.post(config.tokenUrl, new URLSearchParams({
      grant_type: 'refresh_token',
      refresh_token: refreshToken,
      client_id: config.clientId,
      client_secret: config.clientSecret
    }), {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    });

    const { access_token } = tokenResponse.data;
    req.session.accessToken = access_token;

    res.json({ success: true });
  } catch (error) {
    console.error('Token refresh error:', error);
    res.status(500).json({ error: 'Token refresh failed' });
  }
});

app.listen(3000, () => {
  console.log('App running on http://localhost:3000');
});
```


### 12.2 .well-known/openid-configuration（服务发现）

第三方应用可以访问 `https://qiaoya.com/.well-known/openid-configuration` 自动发现 OAuth2/OIDC 端点：

```json
{
  "issuer": "https://qiaoya.com",
  "authorization_endpoint": "https://qiaoya.com/oauth2/authorize",
  "token_endpoint": "https://qiaoya.com/oauth2/token",
  "userinfo_endpoint": "https://qiaoya.com/oauth2/userinfo",
  "jwks_uri": "https://qiaoya.com/oauth2/jwks",
  "registration_endpoint": "https://qiaoya.com/oauth2/register",
  "scopes_supported": ["openid", "profile", "email", "courses:read", "posts:read"],
  "response_types_supported": ["code"],
  "grant_types_supported": ["authorization_code", "refresh_token", "client_credentials"],
  "subject_types_supported": ["public"],
  "id_token_signing_alg_values_supported": ["RS256"],
  "token_endpoint_auth_methods_supported": ["client_secret_basic", "client_secret_post"],
  "code_challenge_methods_supported": ["S256"]
}
```

## 13. 测试策略

### 13.1 单元测试

#### 13.1.1 OAuth2ClientDomainService 测试

```java
@SpringBootTest
class OAuth2ClientDomainServiceTest {

    @Autowired
    private OAuth2ClientDomainService oauth2ClientDomainService;

    @Test
    void testCreateClient() {
        OAuth2ClientEntity client = new OAuth2ClientEntity();
        client.setClientId("test-client");
        client.setClientSecretEnc("test-secret-123456");
        client.setClientName("测试客户端");
        client.setRedirectUris(List.of("https://example.com/callback"));
        client.setGrantTypes(List.of("authorization_code", "refresh_token"));
        client.setScopes(List.of("openid", "profile"));
        client.setClientAuthenticationMethods(List.of("client_secret_basic"));

        OAuth2ClientEntity created = oauth2ClientDomainService.createClient(client);

        assertNotNull(created.getId());
        assertEquals("test-client", created.getClientId());
        assertNotEquals("test-secret-123456", created.getClientSecretEnc()); // 已加密
    }

    @Test
    void testClientIdUniqueness() {
        // 创建第一个客户端
        OAuth2ClientEntity client1 = createTestClient("duplicate-client");
        oauth2ClientDomainService.createClient(client1);

        // 尝试创建相同 clientId 的客户端
        OAuth2ClientEntity client2 = createTestClient("duplicate-client");

        assertThrows(BusinessException.class, () -> {
            oauth2ClientDomainService.createClient(client2);
        });
    }

    @Test
    void testValidateClientSecret() {
        OAuth2ClientEntity client = createTestClient("secret-test");
        String plainSecret = client.getClientSecretEnc();
        OAuth2ClientEntity created = oauth2ClientDomainService.createClient(client);

        boolean valid = oauth2ClientDomainService.validateClientSecret(
            created.getClientId(), plainSecret
        );

        assertTrue(valid);
    }

    private OAuth2ClientEntity createTestClient(String clientId) {
        OAuth2ClientEntity client = new OAuth2ClientEntity();
        client.setClientId(clientId);
        client.setClientSecretEnc("test-secret-" + UUID.randomUUID());
        client.setClientName("测试客户端");
        client.setRedirectUris(List.of("https://example.com/callback"));
        client.setGrantTypes(List.of("authorization_code"));
        client.setScopes(List.of("openid"));
        client.setClientAuthenticationMethods(List.of("client_secret_basic"));
        return client;
    }
}
```

### 13.2 集成测试

#### 13.2.1 OAuth2 授权码流程测试

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class OAuth2AuthorizationFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OAuth2ClientDomainService oauth2ClientDomainService;

    private String clientId = "test-client";
    private String clientSecret = "test-secret";
    private String redirectUri = "https://example.com/callback";

    @BeforeEach
    void setUp() {
        // 创建测试客户端
        OAuth2ClientEntity client = new OAuth2ClientEntity();
        client.setClientId(clientId);
        client.setClientSecretEnc(clientSecret);
        client.setClientName("测试客户端");
        client.setRedirectUris(List.of(redirectUri));
        client.setGrantTypes(List.of("authorization_code", "refresh_token"));
        client.setScopes(List.of("openid", "profile", "email"));
        client.setClientAuthenticationMethods(List.of("client_secret_basic"));
        oauth2ClientDomainService.createClient(client);
    }

    @Test
    void testAuthorizationCodeFlow() throws Exception {
        // 步骤1：请求授权码
        MvcResult authResult = mockMvc.perform(get("/oauth2/authorize")
                .param("response_type", "code")
                .param("client_id", clientId)
                .param("redirect_uri", redirectUri)
                .param("scope", "openid profile")
                .param("state", "random-state")
                .with(user("test-user")))
            .andExpect(status().is3xxRedirection())
            .andReturn();

        // 提取授权码
        String location = authResult.getResponse().getHeader("Location");
        assertNotNull(location);
        assertTrue(location.startsWith(redirectUri));

        String code = extractCodeFromRedirectUri(location);
        assertNotNull(code);

        // 步骤2：使用授权码换取 Token
        MvcResult tokenResult = mockMvc.perform(post("/oauth2/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("grant_type", "authorization_code")
                .param("code", code)
                .param("redirect_uri", redirectUri)
                .param("client_id", clientId)
                .param("client_secret", clientSecret))
            .andExpect(status().isOk())
            .andReturn();

        String response = tokenResult.getResponse().getContentAsString();
        assertNotNull(response);
        assertTrue(response.contains("access_token"));
        assertTrue(response.contains("refresh_token"));
        assertTrue(response.contains("id_token"));
    }

    private String extractCodeFromRedirectUri(String location) {
        // 从重定向 URI 中提取授权码
        String[] parts = location.split("code=");
        if (parts.length > 1) {
            return parts[1].split("&")[0];
        }
        return null;
    }
}
```

### 13.3 性能测试

使用 JMeter 或 Gatling 进行压力测试：

- **授权端点 QPS**：目标 500 QPS
- **Token 端点 QPS**：目标 1000 QPS
- **UserInfo 端点 QPS**：目标 2000 QPS
- **Token 刷新 QPS**：目标 500 QPS

## 14. 实施计划

### 14.1 Phase 1：基础设施搭建（1-2天）
- ✅ 添加 Maven 依赖
- ✅ 创建数据库表（Flyway 迁移）
- ✅ 配置 Spring Authorization Server
- ✅ 生成 RSA 密钥对（JWT 签名）
- ✅ 配置 OAuth2 端点路径

### 14.2 Phase 2：Domain 层实现（2-3天）
- ✅ 创建 OAuth2ClientEntity
- ✅ 创建 OAuth2ClientRepository
- ✅ 实现 OAuth2ClientDomainService（客户端管理）
- ✅ 实现枚举类型（GrantType、ClientAuthenticationMethod、OAuth2ClientStatus）
- ✅ 注册枚举转换器

### 14.3 Phase 3：Application 层与管理后台（2-3天）
- ✅ 创建 DTO 和 Assembler
- ✅ 实现 AdminOAuth2ClientAppService
- ✅ 实现管理后台控制器（CRUD 接口）
- ✅ 实现客户端密钥重置功能

### 14.4 Phase 4：OAuth2 标准端点实现（3-4天）
- ✅ 实现 CustomRegisteredClientRepository（数据库适配器）
- ✅ 实现授权端点（/oauth2/authorize）
- ✅ 实现 Token 端点（/oauth2/token）
- ✅ 实现 UserInfo 端点（/oauth2/userinfo）
- ✅ 实现授权同意页面

### 14.5 Phase 5：安全加固与测试（2-3天）
- ✅ 实现 PKCE 支持
- ✅ 实现 State 参数校验
- ✅ 实现速率限制
- ✅ 编写单元测试
- ✅ 编写集成测试
- ✅ 性能测试

### 14.6 Phase 6：文档与示例（1-2天）
- ✅ 编写第三方应用集成文档
- ✅ 提供示例代码（Node.js、Python、Java）
- ✅ 提供 Postman 测试集合

**总计：11-17 个工作日**

## 15. 配置示例

### 15.1 application.yml 配置

```yaml
spring:
  security:
    oauth2:
      authorizationserver:
        issuer: https://qiaoya.com
        # JWT 签名密钥（生产环境建议使用外部 KeyStore）
        # jwk-set-uri: classpath:jwks.json

  # 数据源配置（复用现有配置）
  datasource:
    url: jdbc:postgresql://localhost:5432/qiaoya_community
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

# 自定义 OAuth2 配置
qiaoya:
  oauth2:
    # 授权码有效期（秒）
    authorization-code-time-to-live: 300
    # Access Token 默认有效期（秒）
    access-token-time-to-live: 3600
    # Refresh Token 默认有效期（秒）
    refresh-token-time-to-live: 2592000
    # 是否启用授权同意页面
    enable-consent-page: true
    # 授权同意页面 URL
    consent-page-uri: /oauth2/consent
```

### 15.2 客户端注册示例

通过管理后台 API 注册客户端：

```bash
POST /api/admin/oauth2/clients
Content-Type: application/json
Authorization: Bearer <admin-token>

{
  "clientId": "my-app",
  "clientSecretEnc": "super-secret-password-at-least-32-chars",
  "clientName": "我的应用",
  "clientDescription": "这是一个测试应用",
  "clientLogoUrl": "https://example.com/logo.png",
  "clientWebsiteUrl": "https://example.com",
  "contactEmail": "contact@example.com",
  "contactName": "张三",
  "redirectUris": [
    "https://example.com/callback",
    "https://example.com/oauth/callback"
  ],
  "grantTypes": [
    "authorization_code",
    "refresh_token"
  ],
  "scopes": [
    "openid",
    "profile",
    "email"
  ],
  "clientAuthenticationMethods": [
    "client_secret_basic",
    "client_secret_post"
  ],
  "accessTokenValiditySeconds": 3600,
  "refreshTokenValiditySeconds": 2592000,
  "requireProofKey": true,
  "requireAuthorizationConsent": true
}
```

响应（包含客户端密钥，仅此一次）：

```json
{
  "code": 0,
  "message": "OAuth2客户端创建成功",
  "data": {
    "client": {
      "id": "uuid-xxx",
      "clientId": "my-app",
      "clientName": "我的应用",
      "status": "ACTIVE",
      "createTime": "2025-01-21T10:00:00"
    },
    "clientSecret": "super-secret-password-at-least-32-chars"
  }
}
```

## 16. 常见问题与边界

### 16.1 Token 过期策略
**问题**：Access Token 过期后如何处理？

**方案**：
- 第三方应用收到 401 错误时，使用 Refresh Token 刷新 Access Token
- 如果 Refresh Token 也过期，引导用户重新登录

### 16.2 多租户支持
**问题**：是否支持多租户（不同组织有独立的客户端）？

**方案**：
- 当前方案不支持多租户
- 若需要支持，可在客户端表添加 `tenant_id` 字段，并在查询时增加租户隔离

### 16.3 第三方应用审核流程
**问题**：如何审核第三方应用，防止恶意应用？

**方案**：
- 新注册的客户端状态默认为 `SUSPENDED`（待审核）
- 管理员审核通过后，修改状态为 `ACTIVE`
- 定期审查客户端的活跃度和使用情况

### 16.4 Scope 动态扩展
**问题**：如何动态添加新的 Scope？

**方案**：
- 在代码中定义 Scope 枚举（如 `OAuth2Scope`）
- 管理员在创建/更新客户端时，从枚举中选择允许的 Scope
- 新增 Scope 时，需要更新枚举类并重新部署

### 16.5 用户撤销授权
**问题**：用户如何撤销对第三方应用的授权？

**方案**：
- 在用户个人中心提供"已授权应用"列表
- 用户可以查看已授权的应用和权限
- 点击"撤销授权"后，删除 `oauth2_authorization_consents` 中的记录，并使相关 Token 失效

### 16.6 跨域问题
**问题**：前端应用（SPA）如何接入 OAuth2？

**方案**：
- 使用授权码模式 + PKCE（无需客户端密钥）
- 前端重定向到授权页面 → 用户同意 → 回调到前端 → 前端携带授权码调用后端 → 后端换取 Token
- 配置 CORS 允许前端域名

## 17. 开发规范对齐检查清单

- ✅ **DDD 分层**：严格按照 Domain → Application → Interfaces 分层
- ✅ **命名规范**：
  - Entity 以 `Entity` 结尾
  - AppService 以 `AppService` 结尾
  - DomainService 以 `DomainService` 结尾
  - Assembler 以 `Assembler` 结尾
  - Repository 继承 `BaseMapper`
- ✅ **依赖注入**：使用构造器注入
- ✅ **Assembler**：静态方法 + `BeanUtils.copyProperties`
- ✅ **Repository**：仅继承 `BaseMapper`，不写自定义 SQL，使用 `LambdaQueryWrapper`
- ✅ **UUID 主键**：使用 `@TableId(type = IdType.ASSIGN_UUID)`
- ✅ **软删除**：查询时不显式 `.eq(deleted, false)`（全局已处理）
- ✅ **枚举类型**：使用枚举传递，并在 `MyBatisTypeHandlerConfig` 注册
- ✅ **异常处理**：使用 `BusinessException` + `OAuth2ErrorCode`
- ✅ **Git 提交**：格式 `feat(oauth2): 实现OAuth2客户端管理`

## 18. 验收标准

- ✅ 第三方应用可通过授权码模式完成登录流程
- ✅ 管理员可在后台创建、更新、删除 OAuth2 客户端
- ✅ 客户端密钥加密存储，仅在创建/重置时返回明文
- ✅ 支持 PKCE 增强安全性
- ✅ 用户授权同意页面展示清晰的权限说明
- ✅ Access Token 和 Refresh Token 正常颁发和刷新
- ✅ OIDC UserInfo 端点正常返回用户信息
- ✅ 所有代码符合 DDD 分层和命名规范
- ✅ 单元测试和集成测试覆盖核心流程
- ✅ 性能测试达到预期 QPS 目标
- ✅ 安全措施（PKCE、State、速率限制）生效
- ✅ 文档完善，第三方应用可按文档快速接入

## 19. 参考资料

- **OAuth 2.0 RFC**：https://datatracker.ietf.org/doc/html/rfc6749
- **OIDC 规范**：https://openid.net/specs/openid-connect-core-1_0.html
- **PKCE RFC**：https://datatracker.ietf.org/doc/html/rfc7636
- **Spring Authorization Server 官方文档**：https://docs.spring.io/spring-authorization-server/reference/
- **Spring Security OAuth2 指南**：https://spring.io/guides/tutorials/spring-boot-oauth2/

## 20. 附录

### 20.1 Scope 列表与权限说明

| Scope | 权限说明 | 返回的用户信息 |
|-------|---------|---------------|
| `openid` | 基础 OIDC 协议（必需） | `sub`（用户ID） |
| `profile` | 用户基本信息 | `name`, `avatar`, `description` |
| `email` | 用户邮箱 | `email`, `email_verified` |
| `courses:read` | 读取用户的课程数据 | 用户购买的课程列表 |
| `posts:read` | 读取用户的文章数据 | 用户发布的文章列表 |
| `follows:read` | 读取用户的关注数据 | 用户关注的用户/课程/文章 |

### 20.2 错误响应示例

授权码兑换失败（客户端密钥错误）：

```json
{
  "error": "invalid_client",
  "error_description": "Client authentication failed"
}
```

授权码过期：

```json
{
  "error": "invalid_grant",
  "error_description": "Authorization code has expired"
}
```

Scope 不允许：

```json
{
  "error": "invalid_scope",
  "error_description": "Requested scope is not allowed for this client"
}
```

### 20.3 监控指标

- **客户端数量**：总客户端数、激活客户端数、暂停客户端数
- **授权请求 QPS**：/oauth2/authorize 端点的 QPS
- **Token 请求 QPS**：/oauth2/token 端点的 QPS
- **Token 刷新 QPS**：refresh_token 请求的 QPS
- **授权成功率**：授权请求成功/总授权请求
- **Token 颁发成功率**：Token 颁发成功/总 Token 请求
- **平均响应时间**：各端点的 P50、P95、P99 响应时间

---

**文档状态**：设计稿（可落地）
**最后更新**：2025-01-21
**维护人**：后端团队

如有疑问或需要补充，请联系后端团队。

## 21. 前端登录页面集成方案（重要）

### 21.1 设计说明

本方案采用**前后端分离架构**，登录页面由前端渲染，后端仅提供 API。

**核心流程**：
1. 用户未登录访问 `/oauth2/authorize` → 后端重定向到 `/login`
2. 前端检测到 URL 参数中有 `redirect` → 识别为 OAuth 登录流程
3. 用户登录成功 → 前端重定向回 `/oauth2/authorize`（携带原始参数）
4. 后端检测已登录 → 显示授权同意页面（或后端渲染，或返回前端处理）

### 21.2 后端配置

```java
// AuthorizationServerConfig.java
@Bean
@Order(1)
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) 
        throws Exception {
    
    OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
    
    http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
        .oidc(Customizer.withDefaults());
    
    // 🔑 关键配置：未认证时重定向到前端登录页面
    http.exceptionHandling(exceptions -> exceptions
        .defaultAuthenticationEntryPointFor(
            new LoginUrlAuthenticationEntryPoint("/login"), // 前端路由
            new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
        )
    );
    
    http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
    
    return http.build();
}
```

### 21.3 前端路由配置（Vue 3 示例）

```javascript
// router/index.js
import { createRouter, createWebHistory } from 'vue-router';
import LoginPage from '@/views/LoginPage.vue';

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: LoginPage,
    beforeEnter: (to, from, next) => {
      // 保存 OAuth 重定向参数到 sessionStorage
      if (to.fullPath.includes('redirect=')) {
        sessionStorage.setItem('oauth_login_redirect', to.fullPath);
      }
      next();
    }
  },
  // ... 其他路由
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;
```

### 21.4 前端登录页面处理（Vue 3 示例）

```vue
<!-- views/LoginPage.vue -->
<template>
  <div class="login-container">
    <h2>登录敲鸭社区</h2>
    
    <!-- OAuth 提示（如果是第三方应用跳转） -->
    <div v-if="isOAuthLogin" class="oauth-hint">
      <p>第三方应用请求登录</p>
      <p class="text-muted">登录后将跳转到授权页面</p>
    </div>
    
    <form @submit.prevent="handleLogin">
      <input 
        v-model="email" 
        type="email" 
        placeholder="邮箱" 
        required 
      />
      <input 
        v-model="password" 
        type="password" 
        placeholder="密码" 
        required 
      />
      <button type="submit">登录</button>
    </form>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import axios from 'axios';

const router = useRouter();
const route = useRoute();

const email = ref('');
const password = ref('');

// 判断是否是 OAuth 登录流程
const isOAuthLogin = computed(() => {
  return route.query.redirect && 
         route.query.redirect.includes('/oauth2/authorize');
});

// 处理登录
async function handleLogin() {
  try {
    // 调用后端登录 API
    const response = await axios.post('/api/auth/login', {
      email: email.value,
      password: password.value
    });
    
    // 保存 Token
    localStorage.setItem('token', response.data.data.token);
    
    // 检查是否是 OAuth 登录流程
    const savedRedirect = sessionStorage.getItem('oauth_login_redirect');
    
    if (savedRedirect) {
      // OAuth 登录：解析重定向参数
      const urlParams = new URLSearchParams(savedRedirect.split('?')[1]);
      const redirectUrl = urlParams.get('redirect');
      
      if (redirectUrl) {
        // 清除缓存
        sessionStorage.removeItem('oauth_login_redirect');
        
        // 🔑 关键：重定向回 OAuth 授权页面（保留所有原始参数）
        window.location.href = decodeURIComponent(redirectUrl);
      } else {
        router.push('/');
      }
    } else {
      // 普通登录：跳转到首页
      router.push('/');
    }
  } catch (error) {
    console.error('登录失败:', error);
    alert('登录失败，请检查邮箱和密码');
  }
}
</script>
```

### 21.5 完整流程示例

#### 场景：用户未登录，第三方应用请求授权

```
步骤1：第三方应用发起授权请求
URL: https://qiaoya.com/oauth2/authorize?
     response_type=code
     &client_id=my-app
     &redirect_uri=https://third-party.com/callback
     &scope=openid profile email
     &state=random-state

步骤2：后端检测到未登录，重定向到前端登录页面
URL: https://qiaoya.com/login?redirect=%2Foauth2%2Fauthorize%3F
     response_type%3Dcode%26client_id%3Dmy-app%26...

步骤3：前端登录页面展示，用户输入账号密码

步骤4：登录成功，前端重定向回授权页面
URL: https://qiaoya.com/oauth2/authorize?
     response_type=code
     &client_id=my-app
     &redirect_uri=https://third-party.com/callback
     &scope=openid profile email
     &state=random-state

步骤5：后端检测已登录，显示授权同意页面（见下文）

步骤6：用户点击"授权"，生成授权码并回调第三方应用
URL: https://third-party.com/callback?code=AUTHORIZATION_CODE&state=random-state
```

### 21.6 授权同意页面的两种方案

#### 方案A：后端渲染授权同意页面（推荐，简单）

```java
// 配置自定义授权同意页面
http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
    .authorizationEndpoint(authz -> authz
        .consentPage("/oauth2/consent") // 后端渲染的页面
    );
```

```html
<!-- src/main/resources/templates/oauth2/consent.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>授权确认</title>
    <meta charset="UTF-8">
</head>
<body>
    <h2>授权确认</h2>
    <p><strong th:text="${clientName}"></strong> 请求访问您的账号</p>
    
    <h3>将获得以下权限：</h3>
    <ul>
        <li th:each="scope : ${scopes}" th:text="${scope}"></li>
    </ul>
    
    <form method="post" th:action="@{/oauth2/authorize}">
        <input type="hidden" name="client_id" th:value="${clientId}">
        <input type="hidden" name="state" th:value="${state}">
        <input type="hidden" name="scope" th:value="${scopeString}">
        
        <button type="submit" name="consent" value="approve">授权</button>
        <button type="submit" name="consent" value="deny">拒绝</button>
    </form>
</body>
</html>
```

#### 方案B：前端渲染授权同意页面（高级，需要额外开发）

需要前端创建授权同意页面组件，并与后端 API 交互。此方案较复杂，建议先使用方案A。

### 21.7 前端需要注意的事项

#### ✅ 必须保留所有 URL 参数
登录成功后重定向时，必须携带所有原始的 OAuth 参数：
- `response_type`
- `client_id`
- `redirect_uri`
- `scope`
- `state`
- `code_challenge`（PKCE）
- `code_challenge_method`

#### ✅ 使用 `window.location.href` 而非 Vue Router
```javascript
// ❌ 错误：使用 Vue Router 会导致页面不刷新，后端无法检测到登录状态
router.push(redirectUrl);

// ✅ 正确：使用原生跳转，触发完整的页面加载
window.location.href = redirectUrl;
```

#### ✅ Token 传递方式
登录成功后，前端需要在后续请求中携带 Token：

**方式1：Cookie（推荐）**
```javascript
// 后端登录 API 设置 HttpOnly Cookie
response.addCookie(new Cookie("SESSION", token));

// 前端无需手动处理，浏览器自动携带 Cookie
```

**方式2：Header（需要前端配置）**
```javascript
// axios 全局配置
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

### 21.8 测试流程

#### 本地测试步骤：

1. **启动后端**（端口 8080）
2. **启动前端**（端口 3000）
3. **配置反向代理**（Nginx 或 Vite Proxy）：
```nginx
# Nginx 配置示例
location /api/ {
    proxy_pass http://localhost:8080/api/;
}

location /oauth2/ {
    proxy_pass http://localhost:8080/oauth2/;
}

location / {
    proxy_pass http://localhost:3000/;
}
```

4. **创建测试客户端**：
```bash
curl -X POST http://localhost:8080/api/admin/oauth2/clients \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin-token>" \
  -d '{
    "clientId": "test-app",
    "clientSecretEnc": "test-secret-1234567890123456789012",
    "clientName": "测试应用",
    "redirectUris": ["http://localhost:3000/callback"],
    "grantTypes": ["authorization_code", "refresh_token"],
    "scopes": ["openid", "profile", "email"],
    "clientAuthenticationMethods": ["client_secret_basic"]
  }'
```

5. **模拟第三方应用发起授权**：
```
访问：http://localhost:8080/oauth2/authorize?
      response_type=code
      &client_id=test-app
      &redirect_uri=http://localhost:3000/callback
      &scope=openid profile
      &state=test-state
```

6. **验证流程**：
   - 应该重定向到 `/login`
   - 登录成功后重定向回 `/oauth2/authorize`
   - 显示授权同意页面
   - 授权后回调到 `http://localhost:3000/callback?code=xxx&state=test-state`

### 21.9 常见问题

#### Q1：登录后无法进入授权页面，一直循环重定向
**原因**：Token 未正确传递给后端，导致后端认为仍未登录。

**解决**：
- 确保使用 Cookie 方式传递 Token
- 或者配置 axios 全局拦截器携带 Authorization Header

#### Q2：授权同意页面样式无法加载
**原因**：前端静态资源路径配置问题。

**解决**：
- 如果使用后端渲染，将 CSS/JS 放到 `src/main/resources/static/` 目录
- 或者使用内联样式

#### Q3：CORS 跨域问题
**原因**：前后端分离时，前端（3000）请求后端（8080）。

**解决**：
```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
```

---

**前端开发者参考此章节**进行登录页面和授权流程的前端集成。
