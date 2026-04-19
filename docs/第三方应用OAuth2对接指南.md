# 敲鸭 OAuth2 对接指南

## 环境

| 环境 | 后端端口 | 前端端口 |
|------|----------|----------|
| 本地 | 8520 | 5173 |
| 生产 | https://code.xhyovo.cn | — |

## 端点（snake_case 参数，标准 OAuth2）

| 用途 | URL |
|------|-----|
| 授权页跳转 | `GET /api/public/oauth2/authorize` |
| 换取 Token | `POST /api/public/oauth2/token` |
| 用户信息（OIDC） | `GET /api/public/oauth2/userinfo` |
| 用户详情（完整） | `GET /api/user` |
| 客户端信息 | `GET /api/public/oauth2/clients/{clientId}` |

## 关键细节

### 1. 参数命名：全用 snake_case
- authorize: `client_id`, `redirect_uri`, `response_type`
- token: `grant_type`, `client_id`, `client_secret`, `redirect_uri`

### 2. Token 请求：form-urlencoded，不是 JSON
```http
POST /api/public/oauth2/token
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code&client_id=xxx&client_secret=xxx&code=xxx&redirect_uri=xxx
```

### 3. Token 响应：标准顶层字段
```json
{ "access_token": "...", "token_type": "Bearer", "expires_in": 3600 }
```
（不是 `data.accessToken`）

### 4. userinfo 响应：标准 OIDC，无包装
```json
{ "sub": "13", "name": "xhy", "picture": "...", "email": "..." }
```

### 5. /api/user 响应：有 `data` 包装，字段更完整
```json
{
  "code": 200,
  "data": { "id": "13", "name": "xhy", "avatar": "589", "email": "..." }
}
```
推荐用 `/api/user` 获取用户信息（有 id、avatar 等完整字段）。

### 6. redirect_uri 必须完全匹配数据库注册值
在管理后台或数据库中为客户端注册回调地址，请求时必须与注册值完全一致。

## 授权流程

```
第三方前端 /login
  → GET 第三方后端 /auth/login
  → 302 → 敲鸭 /api/public/oauth2/authorize?client_id=...
  → 用户在敲鸭登录并授权
  → 302 → 第三方回调地址?code=xxx&state=xxx
  → POST /api/public/oauth2/token (form-urlencoded)
  → GET /api/user (Bearer token)
  → 第三方生成自己的会话（JWT/Session）
  → 302 → 第三方首页
```

## Node.js 接入示例（Express）

```typescript
// 1. 发起授权
app.get('/auth/login', (req, res) => {
  const state = crypto.randomBytes(16).toString('hex')
  const params = new URLSearchParams({
    client_id: CLIENT_ID,
    redirect_uri: REDIRECT_URI,
    response_type: 'code',
    scope: 'openid profile',
    state,
  })
  res.redirect(`${QIAOYA_BASE}/api/public/oauth2/authorize?${params}`)
})

// 2. 处理回调
app.get('/auth/callback/qiaoya', async (req, res) => {
  const { code } = req.query

  // 换 token（form-urlencoded）
  const tokenRes = await axios.post(
    `${QIAOYA_BASE}/api/public/oauth2/token`,
    new URLSearchParams({ grant_type: 'authorization_code', client_id: CLIENT_ID,
      client_secret: CLIENT_SECRET, code, redirect_uri: REDIRECT_URI }),
    { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
  )
  const accessToken = tokenRes.data.access_token

  // 获取用户信息
  const userRes = await axios.get(`${QIAOYA_BASE}/api/user`, {
    headers: { Authorization: `Bearer ${accessToken}` },
  })
  const user = userRes.data.data  // { id, name, avatar, email, ... }

  // 生成自己的会话 ...
})
```
