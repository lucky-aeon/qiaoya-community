# 在现有工作流中插入冒烟测试并分离部署（操作清单）

作者：后端
最后更新：2025-09-27
状态：操作指南（按步骤修改 YAML）
适用范围：`.github/workflows/dev-deploy.yml`、`.github/workflows/release-deploy.yml`

关联文档：
- 冒烟用例与脚本示例：docs/deployment/CI-容器级冒烟测试-复用步骤与示例.md
- 发布可靠性与回滚策略：docs/deployment/发布可靠性与回滚策略-蓝绿-金丝雀-候选容器预检.md

---

## 目标

- 将“部署步骤”从 `build-and-push` Job 拆分出来，形成三级链路：
  - `build-and-push` → `smoke-test` → `deploy`
- `smoke-test` 在 CI Runner 启 Postgres/Redis 和应用容器，跑健康与公开接口冒烟；通过后才允许部署。
- 镜像标签：dev 使用不可变 `dev-<sha>`；release 使用版本 `vX.Y.Z`。

---

## 一、dev 工作流（`.github/workflows/dev-deploy.yml`）

当前现状：`build-and-push` Job 内部直接执行“Deploy via SSH (dev)”步骤（参考：`.github/workflows/dev-deploy.yml`）。

改造步骤：

1) 在 `build-and-push` Job 末尾“部署步骤”之前，新增“导出镜像标签给下游”的步骤与 Job 输出：

```yaml
jobs:
  build-and-push:
    runs-on: ubuntu-latest
    # ... 省略已有 steps（构建与推送到 GHCR/CN）
    outputs:
      image: ${{ steps.out-image.outputs.image }}
    steps:
      # ... 你的构建与推送步骤

      - name: Compute image for downstream
        id: out-image
        run: |
          if [ -n "${{ env.CN_IMAGE }}" ]; then
            echo "image=${{ env.CN_IMAGE }}:${{ env.TAG_SHA }}" >> "$GITHUB_OUTPUT"
          else
            echo "image=${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ env.TAG_SHA }}" >> "$GITHUB_OUTPUT"
          fi
```

说明：
- dev 已经同时推送了 `dev-latest` 和 `dev-<sha>`；下游统一使用不可变的 `dev-<sha>`（`TAG_SHA`）。
- 若配置了国内仓库，将优先使用 `CN_IMAGE:dev-<sha>`；否则使用 GHCR `ghcr.io/<repo>:dev-<sha>`。

2) 新增 `smoke-test` Job，依赖 `build-and-push`，并登录镜像仓库→起数据库服务→运行应用容器→健康+冒烟：

```yaml
  smoke-test:
    name: Smoke Test (Container)
    runs-on: ubuntu-latest
    needs: build-and-push

    services:
      postgres:
        image: postgres:16-alpine
        env:
            POSTGRES_PASSWORD: postgres
        ports:
          - 5432:5432
        options: >-
          --health-cmd="pg_isready -U postgres" --health-interval=5s --health-timeout=5s --health-retries=20
      redis:
        image: redis:7-alpine
        ports:
          - 6379:6379
        options: >-
          --health-cmd="redis-cli ping || exit 1" --health-interval=5s --health-timeout=5s --health-retries=20

    steps:
      - uses: actions/checkout@v4

      # 登录仓库（按需选择 GHCR 或国内仓库）
      - name: Login to GHCR
        if: ${{ env.CN_REGISTRY == '' }}
        uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Login to CN Registry
        if: ${{ env.CN_REGISTRY != '' }}
        uses: docker/login-action@v3
        with:
          registry: ${{ env.CN_REGISTRY }}
          username: ${{ secrets.CN_USERNAME }}
          password: ${{ secrets.CN_PASSWORD }}

      - name: Prepare DB and tools
        run: |
          sudo apt-get update && sudo apt-get install -y postgresql-client curl jq
          for i in $(seq 1 60); do
            pg_isready -h 127.0.0.1 -p 5432 -U postgres && break || sleep 2
          done
          PGPASSWORD=postgres psql -h 127.0.0.1 -U postgres -p 5432 -c "CREATE DATABASE qiaoya_community;" || true

      - name: Run app container
        env:
          IMAGE: ${{ needs.build-and-push.outputs.image }}
        run: |
          docker run -d --name app -p 18520:8520 \
            -e SPRING_PROFILES_ACTIVE=dev \
            -e DB_HOST=127.0.0.1 -e DB_PORT=5432 -e DB_USERNAME=postgres -e DB_PASSWORD=postgres \
            -e REDIS_HOST=127.0.0.1 -e REDIS_PORT=6379 -e REDIS_DATABASE=0 \
            "$IMAGE"

      - name: Wait health up
        run: |
          for i in $(seq 1 120); do
            status=$(curl -fsS http://127.0.0.1:18520/actuator/health | jq -r .status || echo "")
            [ "$status" = "UP" ] && exit 0
            sleep 2
          done
          echo "Health timeout" >&2
          docker logs --tail=200 app || true
          exit 1

      - name: Public endpoints smoke
        run: |
          set -e
          curl -fsS http://127.0.0.1:18520/api/public/stats/users | jq . >/dev/null
          curl -fsS http://127.0.0.1:18520/api/public/testimonials | jq . >/dev/null
          curl -fsS http://127.0.0.1:18520/api/public/subscription-plans | jq . >/dev/null

      - name: Cleanup
        if: always()
        run: docker rm -f app || true
```

3) 新增独立 `deploy` Job，依赖 `smoke-test`（并可同时依赖 `build-and-push` 以使用输出的镜像标签），直接沿用原来的 SSH 步骤：

```yaml
  deploy:
    name: Deploy (dev)
    runs-on: ubuntu-latest
    needs: [ build-and-push, smoke-test ]
    steps:
      - name: Deploy via SSH (dev)
        uses: appleboy/ssh-action@v1.0.3
        env:
          GHCR_USERNAME: ${{ secrets.GHCR_USERNAME }}
          GHCR_TOKEN: ${{ secrets.GHCR_TOKEN }}
          SCRIPT_PATH: ${{ secrets.DEPLOY_SCRIPT_PATH }}
          IMAGE: ${{ needs.build-and-push.outputs.image }}  # 使用 dev-<sha>
          PROFILE: dev
          PORT: 8520
          CONTAINER_NAME: qiaoya-community-backend
          CN_REGISTRY: ${{ secrets.CN_REGISTRY }}
          CN_USERNAME: ${{ secrets.CN_USERNAME }}
          CN_PASSWORD: ${{ secrets.CN_PASSWORD }}
        with:
          host: ${{ secrets.DEV_HOST }}
          username: ${{ secrets.SSH_USERNAME }}
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          script_stop: true
          envs: GHCR_USERNAME,GHCR_TOKEN,SCRIPT_PATH,IMAGE,PROFILE,PORT,CONTAINER_NAME,CN_REGISTRY,CN_USERNAME,CN_PASSWORD
          script: |
            set -e
            SCRIPT_PATH="${SCRIPT_PATH:-/www/project/qiaoya/deploy-qiaoya.sh}"
            if [ -n "${CN_REGISTRY}" ] && [ -n "${CN_USERNAME}" ] && [ -n "${CN_PASSWORD}" ]; then
              echo "${CN_PASSWORD}" | docker login "${CN_REGISTRY}" -u "${CN_USERNAME}" --password-stdin
            fi
            chmod +x "$SCRIPT_PATH" || true
            "$SCRIPT_PATH"
```

完成上述三步后：删除 `build-and-push` Job 中原有的内联“Deploy via SSH (dev)”步骤，避免重复部署。

---

## 二、release 工作流（`.github/workflows/release-deploy.yml`）

思路相同，但镜像标签使用 `${{ env.TAG }}`（例如 `v1.2.3`）。

1) 在 `build-and-push` Job 末尾导出镜像给下游：

```yaml
  build-and-push:
    outputs:
      image: ${{ steps.out-image.outputs.image }}
    steps:
      # ... 你的构建与推送步骤
      - name: Compute image for downstream
        id: out-image
        run: |
          if [ -n "${{ env.CN_IMAGE }}" ]; then
            echo "image=${{ env.CN_IMAGE }}:${{ env.TAG }}" >> "$GITHUB_OUTPUT"
          else
            echo "image=${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ env.TAG }}" >> "$GITHUB_OUTPUT"
          fi
```

2) 新增 `smoke-test` Job（与 dev 基本一致，`SPRING_PROFILES_ACTIVE` 可用 `prod` 或 `dev` 取决于你想验证的配置。若使用外部依赖，请在 CI 中禁用邮件等外部调用）：

```yaml
  smoke-test:
    needs: build-and-push
    # ... 拷贝 dev 的 smoke-test，唯一变化：
    # - Run app container: 设置 -e SPRING_PROFILES_ACTIVE=prod （如需）
```

3) 新增 `deploy` Job（生产）：

```yaml
  deploy:
    needs: [ build-and-push, smoke-test ]
    steps:
      - name: Deploy via SSH (prod)
        uses: appleboy/ssh-action@v1.0.3
        env:
          IMAGE: ${{ needs.build-and-push.outputs.image }}  # 使用 vX.Y.Z
          PROFILE: prod
          # ... 其余与原始步骤一致
```

---

## 三、注意事项

- 登录仓库：`smoke-test` 是新 Job，需重新 `docker/login-action`（GHCR 用 `GITHUB_TOKEN`；国内仓库用 `CN_USERNAME`/`CN_PASSWORD`）。
- 数据库准备：在 `smoke-test` 中用 `psql` 创建数据库 `qiaoya_community`，Flyway 会自动迁移表结构。
- 超时与日志：首启包含 JIT/Flyway 可能较慢，健康检查请给 60–120 秒；失败时 dump 日志帮助排障。
- 变量传递：通过 Job outputs 传递镜像标签，避免在多处重复计算；部署使用与冒烟一致的不可变标签。
- 最小变更：尽量只新增 `outputs/smoke-test/deploy`，保持其他步骤不动；确认删除原 build Job 内的部署步骤。

---

## 四、改造完成后的依赖链

- dev：`build-and-push (输出 image=dev-<sha>)` → `smoke-test (使用 image)` → `deploy (使用 image)`
- release：`build-and-push (输出 image=vX.Y.Z)` → `smoke-test (使用 image)` → `deploy (使用 image)`

