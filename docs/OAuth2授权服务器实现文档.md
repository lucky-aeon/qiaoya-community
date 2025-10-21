# OAuth2 授权服务器实现文档（前后端分离版）

## 概述

本项目实现了完整的 OAuth2 授权服务器，**采用前后端分离架构**，支持以下授权模式：
- **授权码模式 (Authorization Code Flow)** - 最常用的授权方式，适用于有后端的 Web 应用
- **刷新令牌 (Refresh Token)** - 允许客户端使用刷新令牌获取新的访问令牌
- **客户端凭证模式 (Client Credentials)** - 用于服务端对服务端的授权，无需用户参与

## 前后端分离架构设计

### 架构特点
1. **后端**：提供标准的 OAuth2 授权端点，通过重定向将未登录用户引导到前端
2. **前端**：负责用户登录、授权同意UI展示
3. **交互方式**：302 重定向 + RESTful API

### 授权流程（前后端分离版）

```
┌─────────────┐
│ 第三方应用  │
└──────┬──────┘
       │ 1. 跳转授权 GET /api/public/oauth2/authorize?client_id=xxx&redirect_uri=xxx&...
       ▼
┌──────────────────┐
│ 后端授权端点     │
│ (检查登录状态)   │
└──────┬───────────┘
       │ 2. 用户未登录 → 302 重定向到前端授权页面
       │    http://localhost:5173/oauth2/authorize?client_id=xxx&redirect_uri=xxx&...
       ▼
┌──────────────────┐
│ 前端授权页面     │
│ (React)          │
└──────┬───────────┘
       │ 3. 用户登录并同意授权
       │    POST /api/public/oauth2/authorize (生成授权码)
       ▼
┌──────────────────┐
│ 后端授权API      │
│ (生成授权码)     │
└──────┬───────────┘
       │ 4. 返回授权码
       │    { "code": 200, "data": "authorization_code_xyz" }
       ▼
┌──────────────────┐
│ 前端授权页面     │
│ (重定向回第三方) │
└──────┬───────────┘
       │ 5. 重定向回第三方应用
       │    https://third-party-app.com/callback?code=xyz&state=abc
       ▼
┌─────────────┐
│ 第三方应用  │
└──────┬──────┘
       │ 6. 换取 Access Token
       │    POST /api/public/oauth2/token
       ▼
┌──────────────────┐
│ 后端令牌端点     │
└──────────────────┘
```

## 架构设计

### 分层架构

```
interfaces/     - API 接口层
  └── PublicOAuth2AuthorizationController    # OAuth2 授权端点

application/    - 应用服务层
  ├── OAuth2AuthorizationAppService          # 授权流程编排
  ├── dto/                                   # 数据传输对象
  └── assembler/                             # 转换器

domain/         - 领域层
  ├── entity/                                # 领域实体
  │   ├── OAuth2AuthorizationEntity          # 授权记录实体
  │   └── OAuth2AuthorizationConsentEntity   # 用户授权同意实体
  ├── service/                               # 领域服务
  │   └── OAuth2AuthorizationDomainService   # 授权核心业务逻辑
  ├── valueobject/                           # 值对象
  │   ├── GrantType                          # 授权类型枚举
  │   ├── OAuth2Scope                        # 权限范围枚举
  │   └── TokenType                          # Token 类型枚举
  └── repository/                            # Repository 接口

infrastructure/ - 基础设施层
  └── oauth/
      └── OAuth2TokenService                 # Token 生成服务
```

### 数据库表结构

#### oauth2_clients (OAuth2 客户端表)
- 存储第三方应用的客户端信息
- 管理后台已实现 CRUD 操作

#### oauth2_authorizations (OAuth2 授权记录表)
- 存储授权码、Access Token、Refresh Token
- 记录用户授权历史

#### oauth2_authorization_consents (OAuth2 用户授权同意表)
- 记录用户对客户端的授权同意
- 避免用户重复授权

## API 接口

### 1. 授权端点 (Authorization Endpoint)

#### GET /api/public/oauth2/authorize

用于授权码流程的第一步，第三方应用跳转到此端点请求授权。

**行为（前后端分离版）：**
1. 验证客户端和参数
2. 检查用户登录状态：
   - **未登录** → `302` 重定向到前端授权页面
     `http://localhost:5173/oauth2/authorize?client_id=xxx&redirect_uri=xxx&...`
   - **已登录但未授权** → `302` 重定向到前端授权页面
   - **已登录且已授权** → 直接生成授权码，`302` 重定向回第三方应用

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| client_id | String | 是 | 客户端ID |
| redirect_uri | String | 是 | 重定向URI |
| response_type | String | 是 | 固定值 "code" |
| scope | String | 否 | 权限范围（空格分隔） |
| state | String | 否 | CSRF 防护参数 |

**成功重定向示例（已授权用户）：**
```
{redirect_uri}?code={authorization_code}&state={state}
```

**未登录重定向示例：**
```
http://localhost:5173/oauth2/authorize?client_id=test-client&redirect_uri=https://app.com/callback&response_type=code&scope=openid+profile&state=xyz
```

#### POST /api/public/oauth2/authorize

**前端授权页面**在用户同意授权后调用此接口生成授权码。

**请求体：**
```json
{
  "clientId": "test-client",
  "redirectUri": "https://third-party-app.com/callback",
  "responseType": "code",
  "scope": "openid profile email",
  "state": "random-state",
  "approved": true
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "授权成功",
  "data": "authorization_code_xyz123"
}
```

**前端处理：**
前端收到授权码后，需要将用户重定向回第三方应用：
```javascript
const authCode = response.data;
const redirectUrl = `${redirectUri}?code=${encodeURIComponent(authCode)}&state=${encodeURIComponent(state)}`;
window.location.href = redirectUrl;
```

### 2. 令牌端点 (Token Endpoint)

#### POST /api/public/oauth2/token

用于获取 Access Token，支持三种授权模式。

**请求头：**
```
Content-Type: application/json
```

**请求体参数：**

##### 授权码模式
```json
{
  "grant_type": "authorization_code",
  "client_id": "your_client_id",
  "client_secret": "your_client_secret",
  "code": "authorization_code",
  "redirect_uri": "https://your-app.com/callback"
}
```

##### 刷新令牌模式
```json
{
  "grant_type": "refresh_token",
  "client_id": "your_client_id",
  "client_secret": "your_client_secret",
  "refresh_token": "your_refresh_token"
}
```

##### 客户端凭证模式
```json
{
  "grant_type": "client_credentials",
  "client_id": "your_client_id",
  "client_secret": "your_client_secret",
  "scope": "read write"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "token_type": "Bearer",
    "expires_in": 3600,
    "refresh_token": "XYZ123...",
    "scope": "openid profile email"
  }
}
```

## 使用流程

### 授权码模式完整流程

1. **第三方应用请求授权**
   ```
   GET https://api.example.com/api/public/oauth2/authorize
       ?client_id=test-client
       &redirect_uri=https://third-party-app.com/callback
       &response_type=code
       &scope=openid profile email
       &state=random-state
   ```

2. **用户登录并同意授权**
   - 如果未登录，跳转到登录页面
   - 如果已登录，显示授权同意页面

3. **重定向回第三方应用**
   ```
   https://third-party-app.com/callback
       ?code=xyz123
       &state=random-state
   ```

4. **第三方应用换取 Access Token**
   ```bash
   POST https://api.example.com/api/public/oauth2/token
   Content-Type: application/json

   {
     "grant_type": "authorization_code",
     "client_id": "test-client",
     "client_secret": "secret",
     "code": "xyz123",
     "redirect_uri": "https://third-party-app.com/callback"
   }
   ```

5. **使用 Access Token 访问资源**
   ```bash
   GET https://api.example.com/api/user/profile
   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```

6. **刷新 Access Token**
   ```bash
   POST https://api.example.com/api/public/oauth2/token
   Content-Type: application/json

   {
     "grant_type": "refresh_token",
     "client_id": "test-client",
     "client_secret": "secret",
     "refresh_token": "XYZ123..."
   }
   ```

## 安全特性

### 1. 客户端认证
- 支持 `client_secret_basic` - Basic 认证
- 支持 `client_secret_post` - POST 参数认证
- 客户端密钥使用 BCrypt 加密存储

### 2. 授权码安全
- 授权码使用安全随机数生成（32 字节）
- 授权码有效期：10 分钟
- 授权码使用后立即失效（一次性）

### 3. Token 安全
- Access Token 使用 JWT 格式，包含签名验证
- Refresh Token 使用安全随机数生成（64 字节）
- Token 过期时间严格控制

### 4. CSRF 防护
- State 参数验证，防止 CSRF 攻击

### 5. 重定向URI 白名单
- 严格验证重定向URI，防止重定向劫持

### 6. 客户端状态管理
- 只有激活状态的客户端可以使用
- 支持暂停、撤销客户端

## 权限范围 (Scope)

系统支持以下权限范围：

| Scope | 说明 |
|-------|------|
| openid | OpenID Connect 基础权限 |
| profile | 用户基本信息（用户名、昵称） |
| email | 用户邮箱 |
| read | 读取用户数据 |
| write | 写入用户数据 |

## 管理后台

OAuth2 客户端管理已实现，管理员可以：
- 创建新的 OAuth2 客户端应用
- 更新客户端配置
- 重新生成客户端密钥
- 查看客户端列表
- 激活/暂停/撤销客户端
- 删除客户端（软删除）

**管理后台接口路径：** `/api/admin/oauth2/clients`

## 测试说明

### 前置条件
1. 启动项目
2. 通过管理后台创建一个 OAuth2 客户端
3. 记录 client_id 和 client_secret

### 测试授权码流程

使用 Postman 或浏览器进行测试：

1. 在浏览器中访问授权端点（需要先登录）
2. 同意授权
3. 获取授权码
4. 使用 Postman 调用 Token 端点换取 Access Token

### 测试客户端凭证模式

使用 Postman 测试：

```bash
POST /api/public/oauth2/token
Content-Type: application/json

{
  "grant_type": "client_credentials",
  "client_id": "your_client_id",
  "client_secret": "your_client_secret",
  "scope": "read"
}
```

## 后续优化建议

1. **PKCE 支持** - 增强授权码流程安全性
2. **授权同意页面** - 创建友好的 UI 界面
3. **Token 撤销端点** - 允许撤销 Access Token
4. **用户信息端点** - 提供 /oauth2/userinfo 接口
5. **OAuth2 元数据端点** - 提供 /.well-known/oauth-authorization-server
6. **日志审计** - 记录所有授权操作
7. **速率限制** - 防止暴力破解和滥用

## 相关文件

### Domain 层
- `OAuth2AuthorizationEntity.java` - 授权记录实体
- `OAuth2AuthorizationConsentEntity.java` - 授权同意实体
- `OAuth2AuthorizationDomainService.java` - 授权领域服务
- `OAuth2Scope.java` - 权限范围枚举
- `TokenType.java` - Token 类型枚举

### Application 层
- `OAuth2AuthorizationAppService.java` - 授权应用服务
- `OAuth2TokenDTO.java` - Token 响应 DTO
- `OAuth2AuthorizationAssembler.java` - 转换器

### Infrastructure 层
- `OAuth2TokenService.java` - Token 生成服务

### Interfaces 层
- `PublicOAuth2AuthorizationController.java` - OAuth2 公开接口
- `OAuth2AuthorizeRequest.java` - 授权请求参数
- `OAuth2TokenRequest.java` - Token 请求参数

### 数据库迁移
- `V67__Create_oauth2_clients_table.sql`
- `V68__Create_oauth2_authorizations_table.sql`
- `V69__Create_oauth2_authorization_consents_table.sql`
