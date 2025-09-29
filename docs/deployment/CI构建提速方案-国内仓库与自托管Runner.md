# CI 构建提速方案（国内仓库优先 + 一次构建 + 缓存 + 自托管 Runner 可选）

作者：后端
最后更新：2025-09-29
状态：实践指引（可分阶段落地）
适用范围：GitHub Actions（dev/release），Docker Buildx 推送国内镜像仓库

---

## 1. 目标与现状瓶颈

目标：将 `build-and-push` 从 15–20 分钟降至 5–8 分钟（无额外硬件），结合自托管 Runner 再降至 3–6 分钟。

常见瓶颈（结合当前工作流）：
- 重复构建/推送：GHCR 与国内仓库各跑一遍 build-push（等于构建两次、上传两次）。
- 跨境网络：向国内仓库推送时网络慢，占用大头时长（层上传）。
- 冗余步骤：工作流先 `mvn package`，镜像内又 `mvn package`（重复编译）。
- 镜像体积偏大：运行镜像基础层较大，上传耗时长。

---

## 2. 分阶段优化路线

### 阶段 A（零硬件，今天即可落地）

1) dev 只推国内仓库（取消 GHCR 推送）
- 思路：开发环境仅从国内仓库拉取，保留一个仓库即可。
- 要点：
  - 删除 GHCR 登录与推送步骤，仅保留国内仓库登录/推送。
  - `tags` 仅保留不可变 `dev-<sha>`；是否保留 `dev-latest` 自行权衡（建议去掉以减时）。

2) 单次构建（一次 build-push，写多个 tag）
- 思路：如需多标签（如 `dev-<sha>` 与 `dev-latest`），在同一次 `docker/build-push-action` 的 `tags` 中同时写入，构建一次，推多标签。

3) 删除重复的 Maven 构建
- 思路：Dockerfile 已多阶段构建并执行 `mvn package`，可删除工作流中的“Build with Maven -DskipTests”。

4) 限定单架构与基础并发
- `platforms: linux/amd64`（避免误触多架构）。
- `concurrency` 已启用，保持组内互斥、自动取消旧运行。

5) 触发过滤（避免无效构建）
- `on.push.paths-ignore`: 文档/README 变更不触发镜像构建（按需）。

> 预期收益：通常可将 17 分钟降至 7–10 分钟。

---

### 阶段 B（Dockerfile 与 Buildx 缓存优化，1–2 天）

1) 使用更小的运行镜像
- 由 `eclipse-temurin:17-jre` 改为 `eclipse-temurin:17-jre-alpine`（或 `distroless/java17`）。
- 体积：常见从 300MB+ 降至 100MB 左右；上传耗时下降 30–60%。

2) BuildKit 缓存挂载与并行编译
- 构建阶段：
  ```Dockerfile
  # ========== 构建阶段 ==========
  FROM maven:3.9.8-eclipse-temurin-17 AS builder
  WORKDIR /build
  COPY pom.xml .
  RUN --mount=type=cache,target=/root/.m2 mvn -B -q -DskipTests dependency:go-offline
  COPY src ./src
  RUN --mount=type=cache,target=/root/.m2 mvn -B -T 1C -DskipTests package --no-transfer-progress

  # ========== 运行阶段（alpine） ==========
  FROM eclipse-temurin:17-jre-alpine
  WORKDIR /app
  COPY --from=builder /build/target/*.jar /app/app.jar
  ENV JAVA_TOOL_OPTIONS "-Duser.timezone=Asia/Shanghai"
  EXPOSE 8520
  ENTRYPOINT ["sh", "-c", "java $JAVA_TOOL_OPTIONS -jar /app/app.jar"]
  ```
- 说明：
  - `--mount=type=cache,target=/root/.m2` 让依赖缓存由 BuildKit 管理，增量构建更快。
  - `-T 1C` 并行编译；`--no-transfer-progress` 降低 IO 输出。

> 预期收益：在阶段 A 基础上再降 1–3 分钟。

---

### 阶段 C（自托管 Runner：国内构建与推送，显著提速）

1) 准备机器（建议）
- 4C/8G/40G SSD 起步；国内网络，带宽 10–20MB/s+。

2) 安装环境
- Docker Engine + buildx 插件。
- 可选：本地镜像加速、Zstd 压缩支持。

3) 注册 Runner
- 仓库 → Settings → Actions → Runners → New self-hosted runner（Linux）。
- 选择标签（例如：`self-hosted`, `linux`, `cn`）。
- `./svc.sh install && ./svc.sh start` 作为 systemd 服务长期运行。

4) 工作流改造
- `runs-on: [self-hosted, linux, cn]`（仅切换执行节点，其他不变即可工作）。
- 建议改用“注册表缓存”（跨 Runner 可用）：
  ```yaml
  - uses: docker/build-push-action@v6
    with:
      context: .
      push: true
      platforms: linux/amd64
      tags: ${{ secrets.CN_IMAGE }}:dev-${{ github.sha }}
      cache-from: type=registry,ref=${{ secrets.CN_IMAGE }}:buildcache
      cache-to: type=registry,ref=${{ secrets.CN_IMAGE }}:buildcache,mode=max,compression=zstd
  ```
- 说明：`buildcache` 镜像用于存放层缓存；配合自托管 Runner 的“本地磁盘缓存”，增量构建速度显著。

> 预期收益：dev build‑and‑push 3–6 分钟（视机器与带宽）。

---

## 3. 验收与对比指标

- 构建时长对比：
  - A 阶段：17m → 7–10m
  - A+B 阶段：7–10m → 5–8m
  - A+B+C 阶段：5–8m → 3–6m
- 缓存命中：观察 buildx 日志中的 `CACHED` 命中率；首次构建慢属正常。
- 上传时长：对比 `docker/build-push-action` 步骤内“推层耗时”。

---

## 4. 风险与回滚

- 改为只推国内仓库：确保部署脚本 `IMAGE` 引用的国内镜像名（当前已支持），不行随时恢复 GHCR 推送。
- jre‑alpine 基础镜像：先在 dev 验证运行时兼容性，不合适再回退到 `eclipse-temurin:17-jre`。
- 自托管 Runner：启用 Runner Group 仅授权本仓库使用；任意时刻将 `runs-on` 改回 `ubuntu-latest` 托管 Runner 即可回滚。

---

## 5. 操作清单（建议顺序）

1) dev 工作流
- 删除 GHCR 登录与推送；仅保留国内仓库推送。
- 单次 build-push 写入 `:dev-<sha>`（可选：移除 `dev-latest`）。
- 删除冗余“Build with Maven”步骤。
- 保持 `cache-from/to: type=gha`；platforms: linux/amd64。

2) Dockerfile 优化
- 切换运行阶段为 `17-jre-alpine`；引入 `--mount=type=cache,target=/root/.m2` 与 `-T 1C`。

3) 自托管 Runner（可选/推荐）
- 部署 Runner（标签：`self-hosted, linux, cn`），改 `runs-on`。
- 将 buildx 缓存改为 `type=registry` 的 `buildcache` 镜像。

---

## 6. 常见问答（FAQ）

- Q：dev 只推国内仓库，release 也只推国内可以吗？
  - A：可以。若外部协作或备份需要 GHCR，再在 release 时双推。否则统一国内即可。

- Q：去掉 `dev-latest` 是否影响回滚？
  - A：不影响。回滚靠不可变 `dev-<sha>`/`vX.Y.Z` 更可靠；`latest` 只是一个移动标签。

- Q：自托管 Runner 安全性如何保障？
  - A：放在专用子网/安全组；Runner Group 限定仓库；最小权限；限制出网域名；定期升级 runner 版本。

---

## 7. 参考变更点（YAML 片段）

- 单仓库、单次构建：
```yaml
- name: Login to CN Registry
  uses: docker/login-action@v3
  with:
    registry: ${{ secrets.CN_REGISTRY }}
    username: ${{ secrets.CN_USERNAME }}
    password: ${{ secrets.CN_PASSWORD }}

- name: Build and push image (CN)
  uses: docker/build-push-action@v6
  with:
    context: .
    push: true
    platforms: linux/amd64
    tags: |
      ${{ secrets.CN_IMAGE }}:dev-${{ github.sha }}
    cache-from: type=gha
    cache-to: type=gha,mode=max
```

- 自托管 Runner + 注册表缓存：
```yaml
runs-on: [self-hosted, linux, cn]

- uses: docker/build-push-action@v6
  with:
    context: .
    push: true
    platforms: linux/amd64
    tags: ${{ secrets.CN_IMAGE }}:dev-${{ github.sha }}
    cache-from: type=registry,ref=${{ secrets.CN_IMAGE }}:buildcache
    cache-to: type=registry,ref=${{ secrets.CN_IMAGE }}:buildcache,mode=max,compression=zstd
```

---

## 8. 结语

本方案优先从“减少重复工作 + 缩小上传体积 + 提高缓存命中”三方面提速；如需进一步缩短时长，引入自托管 Runner 能显著降低国内推送时间。建议按阶段推进，逐项验证效果与稳定性。

