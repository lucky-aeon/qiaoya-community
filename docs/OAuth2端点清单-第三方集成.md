# OAuth2 端点清单 - 第三方应用集成

> 本文档提供敲鸭社区 OAuth2 授权服务器的所有端点信息，供第三方应用集成使用。

## ⚠️ 重要说明

**命名规范：本 API 统一使用驼峰命名（camelCase）**

- ✅ **请求参数**：`clientId`, `redirectUri`, `grantType`, `accessToken`
- ✅ **响应字段**：`accessToken`, `tokenType`, `expiresIn`, `refreshToken`
- ❌ **不使用下划线**：~~`client_id`~~, ~~`access_token`~~, ~~`expires_in`~~

这与 OAuth2 RFC 标准的下划线命名不同，但更符合 JavaScript/TypeScript 开发习惯。

---

## 📋 快速参考

### 开发环境端点

```bash
授权端点: http://localhost:8520/api/public/oauth2/authorize
令牌端点: http://localhost:8520/api/public/oauth2/token
用户信息端点: http://localhost:8520/api/user/profile
```

### 生产环境端点

```bash
授权端点: https://qiaoya.com/api/public/oauth2/authorize
令牌端点: https://qiaoya.com/api/public/oauth2/token
用户信息端点: https://qiaoya.com/api/user/profile
```

---

## 🔧 环境变量配置模板

将以下内容复制到你的 `.env` 文件中：

```bash
# ========== OAuth2 端点配置 ==========
# 开发环境
QIAOYA_AUTHORIZE_URL=http://localhost:8520/api/public/oauth2/authorize
QIAOYA_TOKEN_URL=http://localhost:8520/api/public/oauth2/token
QIAOYA_USERINFO_URL=http://localhost:8520/api/user/profile

# 生产环境（取消注释以使用）
# QIAOYA_AUTHORIZE_URL=https://qiaoya.com/api/public/oauth2/authorize
# QIAOYA_TOKEN_URL=https://qiaoya.com/api/public/oauth2/token
# QIAOYA_USERINFO_URL=https://qiaoya.com/api/user/profile

# ========== 客户端凭证（由敲鸭社区管理员提供）==========
QIAOYA_CLIENT_ID=your-client-id
QIAOYA_CLIENT_SECRET=your-client-secret

# ========== 回调地址配置 ==========
# 开发环境
QIAOYA_CALLBACK_URL=http://localhost:3000/oauth/callback

# 生产环境（取消注释以使用）
# QIAOYA_CALLBACK_URL=https://yourdomain.com/oauth/callback
```

---

## 📖 端点详细说明

### 1. 授权端点 (Authorization Endpoint)

**用途：** 用户授权，第三方应用引导用户跳转到此端点进行授权

**URL：** `https://qiaoya.com/api/public/oauth2/authorize`

**请求方法：** `GET`

**请求参数：**（使用驼峰命名）
| 参数 | 必填 | 说明 | 示例值 |
|------|------|------|--------|
| `clientId` | ✅ | 客户端ID | `your-app-name` |
| `redirectUri` | ✅ | 授权回调地址 | `https://yourdomain.com/oauth/callback` |
| `responseType` | ✅ | 响应类型，固定为 `code` | `code` |
| `scope` | ✅ | 权限范围，空格分隔 | `openid profile email` |
| `state` | ⭕ 推荐 | 随机字符串，防止CSRF攻击 | `xyz123` |
| `codeChallenge` | ⭕ | PKCE 代码挑战（可选） | `...` |
| `codeChallengeMethod` | ⭕ | PKCE 方法（可选） | `S256` |

**示例请求：**
```bash
GET https://qiaoya.com/api/public/oauth2/authorize?clientId=your-app&redirectUri=https://yourdomain.com/callback&responseType=code&scope=openid+profile+email&state=xyz123
```

**响应：**
- 用户未登录 → 重定向到敲鸭社区登录页面
- 用户已登录但未授权 → 显示授权同意页面
- 用户已授权 → 重定向回 `redirect_uri?code=xxx&state=xyz123`

---

### 2. 令牌端点 (Token Endpoint)

**用途：** 使用授权码换取 Access Token 和 Refresh Token

**URL：** `https://qiaoya.com/api/public/oauth2/token`

**请求方法：** `POST`

**Content-Type：** `application/json`

**请求体参数：**（使用驼峰命名）
| 参数 | 必填 | 说明 | 示例值 |
|------|------|------|--------|
| `grantType` | ✅ | 授权类型 | `authorization_code` |
| `clientId` | ✅ | 客户端ID | `your-app-name` |
| `clientSecret` | ✅ | 客户端密钥 | `your-client-secret` |
| `code` | ✅ | 授权码 | `auth_code_xxx` |
| `redirectUri` | ✅ | 回调地址（必须与授权时一致） | `https://yourdomain.com/callback` |

**示例请求：**
```bash
POST https://qiaoya.com/api/public/oauth2/token
Content-Type: application/json

{
  "grantType": "authorization_code",
  "clientId": "your-app-name",
  "clientSecret": "your-client-secret",
  "code": "auth_code_xxx",
  "redirectUri": "https://yourdomain.com/callback"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "refreshToken": "rt_xyz789...",
    "scope": "openid profile email"
  }
}
```

---

### 3. 用户信息端点 (UserInfo Endpoint)

**用途：** 使用 Access Token 获取用户信息

**URL：** `https://qiaoya.com/api/user/profile`

**请求方法：** `GET`

**请求头：**
```bash
Authorization: Bearer {access_token}
```

**示例请求：**
```bash
GET https://qiaoya.com/api/user/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "张三",
    "email": "zhangsan@example.com",
    "avatar": "https://qiaoya.com/avatars/zhangsan.jpg",
    "phone": "13800138000",
    "bio": "这是一段个人简介",
    "createTime": "2025-01-01T00:00:00",
    "updateTime": "2025-01-15T10:30:00"
  }
}
```

**⚠️ 注意：** 返回的字段取决于用户授权的 scope：
- `openid` → 只返回 `id`
- `profile` → 返回 `id`, `name`, `avatar`, `bio`
- `email` → 返回 `email`

---

### 4. 刷新令牌 (Refresh Token)

**用途：** 当 Access Token 过期时，使用 Refresh Token 获取新的 Access Token

**URL：** `https://qiaoya.com/api/public/oauth2/token`

**请求方法：** `POST`

**请求体参数：**（使用驼峰命名）
| 参数 | 必填 | 说明 | 示例值 |
|------|------|------|--------|
| `grantType` | ✅ | 固定为 `refresh_token` | `refresh_token` |
| `clientId` | ✅ | 客户端ID | `your-app-name` |
| `clientSecret` | ✅ | 客户端密钥 | `your-client-secret` |
| `refreshToken` | ✅ | Refresh Token | `rt_xyz789...` |

**示例请求：**
```bash
POST https://qiaoya.com/api/public/oauth2/token
Content-Type: application/json

{
  "grantType": "refresh_token",
  "clientId": "your-app-name",
  "clientSecret": "your-client-secret",
  "refreshToken": "rt_xyz789..."
}
```

**响应格式：** 与授权码换 Token 相同

---

## 🔐 用户授权管理端点

敲鸭社区为用户提供了授权管理功能，用户可以查看和撤销已授权的第三方应用。

### 查看已授权应用列表

**用途：** 用户查看自己已授权的所有第三方应用

**URL：** `https://qiaoya.com/api/user/oauth2/authorizations`

**请求方法：** `GET`

**请求头：**
```bash
Authorization: Bearer {user_access_token}
```

**示例请求：**
```bash
GET https://qiaoya.com/api/user/oauth2/authorizations
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "auth-uuid-1",
      "clientId": "your-app-name",
      "clientName": "您的应用名称",
      "clientLogoUrl": "https://example.com/logo.png",
      "clientDescription": "应用描述",
      "scopes": "openid,profile,email",
      "accessTokenIssuedAt": "2025-01-15T10:00:00",
      "accessTokenExpiresAt": "2025-01-15T11:00:00",
      "accessTokenValid": true,
      "refreshTokenExpiresAt": "2025-02-14T10:00:00",
      "createTime": "2025-01-15T10:00:00"
    }
  ]
}
```

### 撤销应用授权

**用途：** 用户撤销对某个第三方应用的授权

**URL：** `https://qiaoya.com/api/user/oauth2/authorizations/{clientId}`

**请求方法：** `DELETE`

**请求头：**
```bash
Authorization: Bearer {user_access_token}
```

**URL参数：**
| 参数 | 必填 | 说明 | 示例值 |
|------|------|------|--------|
| `clientId` | ✅ | 要撤销授权的客户端ID | `your-app-name` |

**示例请求：**
```bash
DELETE https://qiaoya.com/api/user/oauth2/authorizations/your-app-name
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

**撤销授权的影响：**
1. 删除所有该用户对该应用的授权记录
2. 删除授权同意记录
3. 该应用持有的 Access Token 和 Refresh Token 立即失效
4. 用户下次访问该应用时需要重新授权

---

## 🔒 安全要求

### 必须遵守的安全规范

✅ **HTTPS：** 生产环境必须使用 HTTPS
✅ **State 参数：** 必须验证 state 参数，防止 CSRF 攻击
✅ **Client Secret：** 绝不能暴露到前端代码或代码库
✅ **Token 存储：** 使用加密存储，不要使用 localStorage
✅ **Token 过期处理：** 自动刷新过期的 Access Token

❌ **不要在 URL 中传递 Access Token**
❌ **不要在前端代码中硬编码 Client Secret**
❌ **不要忽略 state 参数验证**
❌ **不要在开发环境使用 HTTP（本地测试除外）**

---

## 📊 Token 有效期

| Token 类型 | 有效期 | 说明 |
|-----------|--------|------|
| Authorization Code | 10 分钟 | 用于换取 Access Token，仅可使用一次 |
| Access Token | 1 小时 | 用于访问受保护资源 |
| Refresh Token | 30 天 | 用于获取新的 Access Token |

---

## 🆘 常见问题

### Q1: 授权码换 Token 失败？
**可能原因：**
- 授权码已被使用（仅可使用一次）
- 授权码已过期（有效期 10 分钟）
- Client ID 或 Client Secret 错误
- Redirect URI 不匹配

### Q2: Access Token 过期了怎么办？
**解决方案：** 使用 Refresh Token 刷新，无需用户重新授权

### Q3: 用户拒绝授权怎么办？
**回调参数：**
```
https://yourdomain.com/callback?error=access_denied&error_description=用户拒绝授权&state=xyz123
```

---

## 📞 技术支持

对接过程中遇到问题？

- 📧 技术支持邮箱: tech@qiaoya.com
- 📚 完整集成指南: 见《第三方应用对接指南-UltraThink示例.md》
- 💬 开发者社区: https://qiaoya.com/developers

---

**最后更新**: 2025-01-15
**文档版本**: v1.0.1
**文档维护**: 敲鸭社区技术团队
