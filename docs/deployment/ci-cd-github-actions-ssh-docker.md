# 敲鸭社区后端 CI/CD 技术方案（GitHub Actions + SSH + Docker）

本文档描述如何通过 GitHub Actions 在两种场景下自动构建与部署后端到服务器：
- dev 分支：开发环境自动构建与部署
- 打 Tag（v*）：生产/预发环境自动构建与部署

采用 SSH 直连服务器，在服务器上用 Docker 拉取镜像并运行容器（方案 B：使用服务器上的 env-file，不使用 docker compose）。仅容器化后端应用本身，Redis 使用远程实例（不在容器编排内启动 Redis）。

---

## 1. 目标与约束

- 单一源码，多环境部署（dev、prod/tag）。
- 通过 GitHub Actions 完成构建、镜像推送与 SSH 远程部署。
- 使用 JDK 17、Maven 构建；镜像仓库推荐 GHCR（GitHub Container Registry）。
- 服务器使用 env-file 管理配置，通过 `docker run` 部署应用。

---

## 2. 流程总览（方案 B：env-file + docker run）

1) 触发条件
- dev 环境：推送到 `dev` 分支
- prod 环境：推送 Tag（命名约定：`v*`，例如 `v1.2.3`）

2) CI（构建与推镜像）
- Checkout 代码 → Maven 构建 Jar → 使用 Buildx 构建并推送镜像到 GHCR
- 镜像命名约定：
  - dev：`ghcr.io/<owner>/<repo>:dev-latest`、`dev-<short-sha>`
  - prod（tag）：`ghcr.io/<owner>/<repo>:<tag>`、`prod-latest`

3) CD（部署）
- GitHub Actions 通过 SSH 登录服务器
- 登录 GHCR（私有镜像时需要，详见第 7 节）
- 执行：`docker pull` → 停旧容器 → `docker run -d --env-file /etc/qiaoya.backend.env -p 8520:8520 ...`
- 通过 `--env-file` 中的变量注入 DB/Redis/OSS/邮件等配置；`-e SPRING_PROFILES_ACTIVE=dev|prod` 指定运行环境

---

## 3. 服务器准备（方案 B）

1) 安装 Docker 与 docker compose 插件（以 Ubuntu 为例）
```bash
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# 将当前用户加入 docker 组（重新登录生效）
sudo usermod -aG docker $USER
```

2) 准备运行期 env-file（服务器）
- 统一路径建议：`/etc/qiaoya.backend.env`（可自定义）
- dev/prod 可各自放置不同文件路径（例如 `/etc/qiaoya.backend.dev.env`、`/etc/qiaoya.backend.prod.env`），也可复用一个文件

示例 env-file 内容（值参考 application.yml）：
```env
# Java 启动参数（可选）
JAVA_TOOL_OPTIONS=-Xms512m -Xmx512m

# 以下环境变量来自 src/main/resources/application.yml 的占位符
# 数据库
DB_HOST=127.0.0.1
DB_PORT=5432
DB_USERNAME=postgres
DB_PASSWORD=your-password

# Redis（远程连接，不在 compose 内启动）
REDIS_HOST=127.0.0.1
REDIS_PORT=6379
REDIS_PASSWORD=
REDIS_DATABASE=0

# 阿里云 OSS / 邮件（如未使用可保留默认或留空）
ALIYUN_OSS_ENDPOINT=https://oss-cn-hangzhou.aliyuncs.com
ALIYUN_ACCESS_KEY_ID=your-access-key-id
ALIYUN_ACCESS_KEY_SECRET=your-access-key-secret
ALIYUN_OSS_BUCKET=your-bucket-name
ALIYUN_OSS_REGION=cn-hangzhou
ALIYUN_STS_ROLE_ARN=acs:ram::your-account-id:role/your-role-name
ALIYUN_OSS_CALLBACK_URL=https://your-domain/api/public/oss-callback

ALIYUN_EMAIL_SMTP_HOST=smtpdm.aliyun.com
ALIYUN_EMAIL_SMTP_PORT=25
ALIYUN_EMAIL_USERNAME=your-email@your-domain.com
ALIYUN_EMAIL_PASSWORD=your-smtp-password
ALIYUN_EMAIL_SENDER_NAME=敲鸭社区
ALIYUN_EMAIL_ENABLED=true
```

---

## 4. 推荐 Dockerfile（多阶段构建）

若仓库中尚无 `Dockerfile`，建议在项目根目录新增（容器内端口 8520 与 application.yml 的 `server.port` 对齐）：
```Dockerfile
# ========== 构建阶段 ==========
FROM maven:3.9.8-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

# ========== 运行阶段 ==========
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /build/target/*.jar /app/app.jar
ENV JAVA_TOOL_OPTIONS=""
EXPOSE 8520
ENTRYPOINT ["sh", "-c", "java $JAVA_TOOL_OPTIONS -jar /app/app.jar"]
```

---

## 5. GitHub Secrets 与权限

在仓库设置 → Secrets and variables → Actions 中新增：

- DEV_HOST：开发服务器 IP 或域名
- PROD_HOST：生产服务器 IP 或域名
- SSH_USERNAME：用于 SSH 的用户名
- SSH_PRIVATE_KEY：私钥内容（与服务器上 `~/.ssh/authorized_keys` 匹配）
- GHCR_USERNAME（私有镜像必需）：GitHub 用户名（或机器人账号）
- GHCR_TOKEN（私有镜像必需）：PAT，至少 `read:packages`，并完成组织 SSO 授权（如适用）
- DEPLOY_SCRIPT_PATH（可选）：自定义脚本路径；默认 `/www/project/qiaoya/deploy-qiaoya.sh`

- CN_REGISTRY（可选/推荐国内）：国内镜像仓库登录地址
  - 例：阿里云 `registry.cn-hangzhou.aliyuncs.com`；腾讯云 `ccr.ccs.tencentyun.com` 或企业实例域名；华为云 `swr.cn-east-3.myhuaweicloud.com`
- CN_IMAGE（可选/推荐国内）：完整的国内仓库镜像名（含命名空间）
  - 例：`registry.cn-hangzhou.aliyuncs.com/your_ns/qiaoya-community`
- CN_USERNAME（可选/推荐国内）：国内仓库登录用户名/凭证用户名
- CN_PASSWORD（可选/推荐国内）：国内仓库登录密码/Token
- SSH_PORT（可选）：默认 22

使用 GHCR 时，Workflow 需要 `packages: write` 权限；使用默认 `GITHUB_TOKEN` 即可推送到 GHCR（包默认私有，见第 7 节）。

---

## 6. GitHub Actions 工作流（docker run + env-file）

为清晰起见，建议两个工作流：`dev` 与 `release`。

### 6.1 dev 环境（.github/workflows/dev-deploy.yml）
```yaml
name: CI/CD Dev

on:
  push:
    branches: [ dev ]

concurrency:
  group: dev-deploy
  cancel-in-progress: true

jobs:
  build-and-push:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write
    env:
      REGISTRY: ghcr.io
      IMAGE_NAME: ${{ github.repository }}
      TAG_SHA: dev-${{ github.sha }}
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 17

      - name: Cache Maven
        uses: actions/cache@v4
        with:
          path: ~/.m2/repository
          key: maven-${{ runner.os }}-${{ hashFiles('**/pom.xml') }}
          restore-keys: |
            maven-${{ runner.os }}-

      - name: Build with Maven
        run: mvn -B -DskipTests package

      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3

      - name: Login to GHCR
        uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Build and push image
        uses: docker/build-push-action@v6
        with:
          context: .
          push: true
          tags: |
            ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:dev-latest
            ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ env.TAG_SHA }}
          cache-from: type=gha
          cache-to: type=gha,mode=max

      - name: Deploy via SSH (dev)
        uses: appleboy/ssh-action@v1.0.3
        env:
          GHCR_USERNAME: ${{ secrets.GHCR_USERNAME }}
          GHCR_TOKEN: ${{ secrets.GHCR_TOKEN }}
          IMAGE: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:dev-latest
          PROFILE: dev
          PORT: 8520
          CONTAINER_NAME: qiaoya-community-backend
        with:
          host: ${{ secrets.DEV_HOST }}
          username: ${{ secrets.SSH_USERNAME }}
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          script_stop: true
          envs: GHCR_USERNAME,GHCR_TOKEN,IMAGE,PROFILE,PORT,CONTAINER_NAME
          script: |
            set -e
            SCRIPT_PATH="/www/project/qiaoya/deploy-qiaoya.sh"
            chmod +x "$SCRIPT_PATH" || true
            "$SCRIPT_PATH"
```

### 6.2 生产/发布（.github/workflows/release-deploy.yml）
```yaml
name: CI/CD Release

on:
  push:
    tags:
      - 'v*'

concurrency:
  group: release-deploy
  cancel-in-progress: true

jobs:
  build-and-push:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write
    env:
      REGISTRY: ghcr.io
      IMAGE_NAME: ${{ github.repository }}
      TAG: ${{ github.ref_name }} # 例如 v1.2.3
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 17

      - name: Cache Maven
        uses: actions/cache@v4
        with:
          path: ~/.m2/repository
          key: maven-${{ runner.os }}-${{ hashFiles('**/pom.xml') }}
          restore-keys: |
            maven-${{ runner.os }}-

      - name: Build with Maven
        run: mvn -B -DskipTests package

      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3

      - name: Login to GHCR
        uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Build and push image
        uses: docker/build-push-action@v6
        with:
          context: .
          push: true
          tags: |
            ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ env.TAG }}
            ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:prod-latest
          cache-from: type=gha
          cache-to: type=gha,mode=max

      - name: Deploy via SSH (prod)
        uses: appleboy/ssh-action@v1.0.3
        env:
          GHCR_USERNAME: ${{ secrets.GHCR_USERNAME }}
          GHCR_TOKEN: ${{ secrets.GHCR_TOKEN }}
          IMAGE: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ env.TAG }}
          PROFILE: prod
          PORT: 8520
          CONTAINER_NAME: qiaoya-community-backend
        with:
          host: ${{ secrets.PROD_HOST }}
          username: ${{ secrets.SSH_USERNAME }}
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          script_stop: true
          envs: GHCR_USERNAME,GHCR_TOKEN,IMAGE,PROFILE,PORT,CONTAINER_NAME
          script: |
            set -e
            SCRIPT_PATH="/www/project/qiaoya/deploy-qiaoya.sh"
            chmod +x "$SCRIPT_PATH" || true
            "$SCRIPT_PATH"
```

> 注：如果不希望使用 `prod-latest`，可以只保留 `:v*` 的版本化标签，回滚更直观。

---

## 7. GHCR 私有镜像拉取与可选公开

私有 GitHub 仓库下推送到 GHCR 的容器镜像（Package）默认是私有包。服务器拉取时需要认证。

1) 生成 GitHub Personal Access Token（PAT）
- 位置：GitHub → 用户头像 → Settings → Developer settings → Personal access tokens
- 选择：Tokens (classic) 或 Fine-grained（推荐 classic 简单）
- 权限勾选：`read:packages`（仅拉取镜像）
- 若镜像在组织（Organization）下，且启用了 SSO：点击该 PAT 的“Configure SSO”，对目标组织点击“Authorize”。

2) 在仓库 Secrets 设置 GHCR 凭据
- 新增 `GHCR_USERNAME`：你的 GitHub 用户名（或有权限的机器人账号）
- 新增 `GHCR_TOKEN`：上一步创建的 PAT（具备 read:packages）

3) 工作流中的登录与拉取
- 工作流已在部署步骤中：
  ```bash
  echo "$GHCR_TOKEN" | docker login ghcr.io -u "$GHCR_USERNAME" --password-stdin
  docker pull ghcr.io/<owner>/<repo>:<tag>
  ```

4) 可选：将镜像设为 Public（不推荐生产）
- 位置：GitHub → 你的仓库 → Packages → 进入对应容器包 → Package settings → Change visibility → Public
- 公开后服务器可免登录 `docker pull`，但任何人都可拉取该镜像。

5) 验证
- 在服务器上手动执行一次：
  ```bash
  echo "$GHCR_TOKEN" | docker login ghcr.io -u "$GHCR_USERNAME" --password-stdin
  docker pull ghcr.io/<owner>/<repo>:dev-latest
  ```

---

## 8. 无镜像仓库的替代方案（可选）

若暂不接入 GHCR/Docker Hub，可在 CI 中将镜像打包传到服务器再加载：

1) 在 CI 构建完成后：
```bash
docker build -t qiaoya/backend:${GITHUB_SHA} .
docker save qiaoya/backend:${GITHUB_SHA} | gzip > app-image.tar.gz
```

2) 使用 `scp` 上传到服务器（或 `appleboy/scp-action`），然后通过 SSH：
```bash
gunzip -c app-image.tar.gz | docker load
docker tag qiaoya/backend:${GITHUB_SHA} ghcr.io/<owner>/<repo>:dev-latest
docker stop qiaoya-community-backend || true
docker rm qiaoya-community-backend || true
IMAGE=ghcr.io/<owner>/<repo>:dev-latest PROFILE=dev /www/project/qiaoya/deploy-qiaoya.sh
```

此方案省去镜像仓库，但网络传输量较大，且多台服务器时维护成本更高，故仅作备选。

---

## 9. 回滚策略

- 使用 GHCR 方案：选择目标历史 Tag（如 `v1.2.3`），然后在服务器执行：
```bash
IMAGE=ghcr.io/<owner>/<repo>:v1.2.3 PROFILE=prod /www/project/qiaoya/deploy-qiaoya.sh
```
- 亦可通过 GitHub 界面对该 Tag 重新运行 release 工作流（Re-run job）。
- 无仓库方案：保留本地历史镜像或手动重新加载 tar 包，指定旧镜像执行部署脚本。

---

## 10. 健康检查与排障（可选）

- 若开启 Spring Boot Actuator，可在部署后加入健康检查：
```bash
curl -fsS http://127.0.0.1:${APP_PORT}/actuator/health | jq
```
- 部署失败时：
```bash
docker logs -n 200 qiaoya-community-backend
docker ps -a
```

---

## 11. 安全建议

- 使用专用部署用户，最小权限原则。
- 仅在必要主机开放 22 端口，并设置防火墙规则。
- SSH 私钥仅存放在 GitHub Secrets，服务器侧配置 `authorized_keys`。
- GHCR 包权限控制：生产镜像可设为私有，仅允许必要拉取。

---

## 12. 落地检查清单

- [ ] 服务器已安装 Docker（可选：docker-buildx-plugin）
- [ ] 已创建并妥善保存 env-file：`/etc/qiaoya.backend.env`（或通过 Secrets 传入路径）
- [ ] 仓库已添加 Secrets：`DEV_HOST`、`PROD_HOST`、`SSH_USERNAME`、`SSH_PRIVATE_KEY`
- [ ] 若 GHCR 为私有：已添加 `GHCR_USERNAME`、`GHCR_TOKEN`（PAT 含 read:packages，并完成组织 SSO 授权）
- [ ] Workflow（dev/release）已启用，首次推送到 `dev` 或打 `v*` Tag 验证自动部署

---

## 13. 环境变量映射（来自 application.yml）

以下变量名与 `src/main/resources/application.yml` 的占位符一一对应：

- 数据库
  - `DB_HOST` → `spring.datasource.url`
  - `DB_PORT` → `spring.datasource.url`
  - `DB_USERNAME` → `spring.datasource.username`
  - `DB_PASSWORD` → `spring.datasource.password`

- Redis（远程）
  - `REDIS_HOST` → `spring.data.redis.host`
  - `REDIS_PORT` → `spring.data.redis.port`
  - `REDIS_PASSWORD` → `spring.data.redis.password`
  - `REDIS_DATABASE` → `spring.data.redis.database`

- 阿里云 OSS
  - `ALIYUN_OSS_ENDPOINT` → `aliyun.oss.endpoint`
  - `ALIYUN_ACCESS_KEY_ID` → `aliyun.oss.access-key-id`
  - `ALIYUN_ACCESS_KEY_SECRET` → `aliyun.oss.access-key-secret`
  - `ALIYUN_OSS_BUCKET` → `aliyun.oss.bucket-name`
  - `ALIYUN_OSS_REGION` → `aliyun.oss.region`
  - `ALIYUN_STS_ROLE_ARN` → `aliyun.oss.role-arn`
  - `ALIYUN_OSS_CALLBACK_URL` → `aliyun.oss.callback.url`

- 阿里云邮件
  - `ALIYUN_EMAIL_SMTP_HOST` → `aliyun.email.smtp.host`
  - `ALIYUN_EMAIL_SMTP_PORT` → `aliyun.email.smtp.port`
  - `ALIYUN_EMAIL_USERNAME` → `aliyun.email.smtp.username`
  - `ALIYUN_EMAIL_PASSWORD` → `aliyun.email.smtp.password`
  - `ALIYUN_EMAIL_SENDER_NAME` → `aliyun.email.smtp.sender-name`
  - `ALIYUN_EMAIL_ENABLED` → `aliyun.email.smtp.enabled`

- 应用端口
  - 容器内端口为 `8520`（见 `server.port`）；如需覆盖，可在 `.env` 中设置 `SERVER_PORT=xxxx`（Spring Boot 支持环境变量覆盖），并在 `compose.yml` 调整 `ports` 映射。

---

## 14. 国内镜像仓库（双推与部署）

为解决国内服务器拉取海外仓库不稳定的问题，工作流已支持“双推”：
- 始终推送到 GHCR 作为主仓库（回退/历史版本可用）
- 按需再推送一份到国内仓库，并在部署时从国内仓库拉取

### 所需 Secrets
- `CN_REGISTRY`：国内镜像仓库登录域名
- `CN_IMAGE`：完整镜像名（含命名空间）
- `CN_USERNAME`：仓库用户名/凭证用户名
- `CN_PASSWORD`：仓库密码/Token

工作流行为：若配置了 `CN_IMAGE`，则构建后会额外推送至国内仓库；部署时优先从 `CN_IMAGE` 拉取。未配置时自动回退到 GHCR。

### 常见厂商配置示例

- 阿里云 ACR
  - `CN_REGISTRY`：`registry.cn-<region>.aliyuncs.com`（个人版）或企业实例域名 `<instance-id>.registry.<region>.aliyuncs.com`
  - `CN_IMAGE`：`registry.cn-hangzhou.aliyuncs.com/<namespace>/qiaoya-community`
  - `CN_USERNAME`/`CN_PASSWORD`：进入“容器镜像服务 ACR → 访问凭证/登录凭证”设置并获取（注意与控制台密码区分）。
  - 控制台路径：阿里云控制台 → 容器镜像服务 ACR → 实例/命名空间/镜像仓库。

- 腾讯云 TCR
  - `CN_REGISTRY`：基础版为 `ccr.ccs.tencentyun.com`；企业版实例域名通常为 `<instance-name>.tencentcloudcr.com`
  - `CN_IMAGE`：`ccr.ccs.tencentyun.com/<namespace>/qiaoya-community` 或 `<instance-name>.tencentcloudcr.com/<ns>/qiaoya-community`
  - `CN_USERNAME`/`CN_PASSWORD`：TCR 控制台“访问凭证/令牌管理”创建并获取。
  - 控制台路径：腾讯云控制台 → 容器镜像服务 TCR → 实例/命名空间/镜像仓库。

- 华为云 SWR
  - `CN_REGISTRY`：`swr.cn-<region>.myhuaweicloud.com`
  - `CN_IMAGE`：`swr.cn-east-3.myhuaweicloud.com/<namespace>/qiaoya-community`
  - `CN_USERNAME`/`CN_PASSWORD`：在 SWR 控制台“访问凭证/登录密码”处设置并使用（IAM 用户需先开通并授权）。
  - 控制台路径：华为云控制台 → SWR → 命名空间/镜像仓库。

### 获取配置的通用步骤
1) 在云厂商控制台创建命名空间与镜像仓库（名称建议 `qiaoya-community`）
2) 记录仓库域名（`CN_REGISTRY`）与镜像全名（`CN_IMAGE`）
3) 在“访问凭证/登录凭证”创建用户名与密码（`CN_USERNAME`/`CN_PASSWORD`）
4) 将上述 4 个值填入 GitHub 仓库的 Secrets（Actions）
5) 触发一次 dev 或 tag 流程，验证构建后能在国内仓库看到新标签，且服务器部署能成功拉取

### 本地/服务器验证
```bash
docker login $CN_REGISTRY -u $CN_USERNAME -p $CN_PASSWORD
docker pull $CN_IMAGE:dev-latest
```

如需仅 dev 使用国内仓库、prod 使用 GHCR，可分别在两个工作流中按需配置 `CN_*`（或让 prod 留空以回退 GHCR）。
