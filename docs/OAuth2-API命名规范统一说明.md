# OAuth2 API 命名规范统一说明

## 📝 变更说明

**日期：** 2025-01-15
**版本：** v1.1.0
**变更类型：** API 命名规范统一

---

## 🎯 变更内容

### 统一为驼峰命名（camelCase）

为了提供更好的开发体验，敲鸭社区 OAuth2 API 统一使用**驼峰命名**，而非 OAuth2 RFC 标准的下划线命名。

### 对比表

| OAuth2 标准（下划线） | 敲鸭社区 API（驼峰） | 说明 |
|---------------------|-------------------|------|
| `client_id` | `clientId` | 客户端ID |
| `client_secret` | `clientSecret` | 客户端密钥 |
| `redirect_uri` | `redirectUri` | 重定向URI |
| `response_type` | `responseType` | 响应类型 |
| `grant_type` | `grantType` | 授权类型 |
| `access_token` | `accessToken` | 访问令牌 |
| `token_type` | `tokenType` | 令牌类型 |
| `expires_in` | `expiresIn` | 过期时间 |
| `refresh_token` | `refreshToken` | 刷新令牌 |
| `code_challenge` | `codeChallenge` | PKCE 挑战码 |
| `code_challenge_method` | `codeChallengeMethod` | PKCE 方法 |
| `code_verifier` | `codeVerifier` | PKCE 验证器 |

---

## 📖 示例对比

### 授权端点请求

**❌ 旧方式（下划线）：**
```bash
GET /api/public/oauth2/authorize?client_id=demo&redirect_uri=http://localhost:3000/callback&response_type=code&scope=openid+profile+email&state=xyz
```

**✅ 新方式（驼峰）：**
```bash
GET /api/public/oauth2/authorize?clientId=demo&redirectUri=http://localhost:3000/callback&responseType=code&scope=openid+profile+email&state=xyz
```

### Token 端点请求

**❌ 旧方式（下划线）：**
```json
{
  "grant_type": "authorization_code",
  "client_id": "demo",
  "client_secret": "secret123",
  "code": "auth_code_xyz",
  "redirect_uri": "http://localhost:3000/callback"
}
```

**✅ 新方式（驼峰）：**
```json
{
  "grantType": "authorization_code",
  "clientId": "demo",
  "clientSecret": "secret123",
  "code": "auth_code_xyz",
  "redirectUri": "http://localhost:3000/callback"
}
```

### Token 响应

**❌ 旧方式（下划线）：**
```json
{
  "access_token": "eyJhbGci...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "refresh_token": "rt_xyz...",
  "scope": "openid profile email"
}
```

**✅ 新方式（驼峰）：**
```json
{
  "accessToken": "eyJhbGci...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "refreshToken": "rt_xyz...",
  "scope": "openid profile email"
}
```

---

## 💻 前端代码示例

### TypeScript 接口定义

```typescript
// OAuth2 Token 请求
interface OAuth2TokenRequest {
  grantType: 'authorization_code' | 'refresh_token';
  clientId: string;
  clientSecret: string;
  code?: string;
  redirectUri?: string;
  refreshToken?: string;
  codeVerifier?: string;
}

// OAuth2 Token 响应
interface OAuth2TokenResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  refreshToken?: string;
  scope: string;
}
```

### 使用示例

```typescript
// 换取 Token
const response = await fetch('http://localhost:8520/api/public/oauth2/token', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    grantType: 'authorization_code',
    clientId: 'demo',
    clientSecret: 'secret123',
    code: authCode,
    redirectUri: callbackUrl,
    codeVerifier: pkceVerifier
  })
});

const data: OAuth2TokenResponse = await response.json();
console.log('Access Token:', data.accessToken);
console.log('Expires In:', data.expiresIn);
```

---

## 🔄 迁移指南

### 第三方应用需要修改的地方

如果你的应用已经集成了旧版 API（使用下划线命名），需要进行以下修改：

#### 1. 更新 URL 查询参数（GET 请求）

```javascript
// 旧代码
const authUrl = `${baseUrl}/authorize?client_id=${clientId}&redirect_uri=${redirectUri}&response_type=code`;

// 新代码
const authUrl = `${baseUrl}/authorize?clientId=${clientId}&redirectUri=${redirectUri}&responseType=code`;
```

#### 2. 更新 JSON 请求体（POST 请求）

```javascript
// 旧代码
const body = {
  grant_type: 'authorization_code',
  client_id: clientId,
  client_secret: clientSecret,
  code: authCode,
  redirect_uri: redirectUri
};

// 新代码
const body = {
  grantType: 'authorization_code',
  clientId: clientId,
  clientSecret: clientSecret,
  code: authCode,
  redirectUri: redirectUri
};
```

#### 3. 更新响应字段解析

```javascript
// 旧代码
const accessToken = response.data.access_token;
const expiresIn = response.data.expires_in;
const refreshToken = response.data.refresh_token;

// 新代码
const accessToken = response.data.accessToken;
const expiresIn = response.data.expiresIn;
const refreshToken = response.data.refreshToken;
```

---

## ✅ 优势

### 为什么选择驼峰命名？

1. **JavaScript/TypeScript 友好** ✨
   - 符合 JavaScript 命名习惯
   - 不需要使用引号访问属性
   - IDE 自动补全更友好

2. **前后端一致** 🔄
   - Java 后端使用驼峰
   - 前端也使用驼峰
   - 减少命名转换

3. **代码更简洁** 📝
   - 不需要 `@JsonProperty` 注解
   - 减少样板代码

4. **类型安全** 🛡️
   - TypeScript 类型定义更直观
   - 减少拼写错误

---

## 📞 技术支持

如有疑问或需要协助迁移，请联系：

- 📧 技术支持邮箱: tech@qiaoya.com
- 💬 开发者社区: https://qiaoya.com/developers

---

**最后更新**: 2025-01-15
**文档版本**: v1.1.0
**文档维护**: 敲鸭社区技术团队
