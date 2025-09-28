# CI 容器级冒烟测试：复用步骤与示例（GitHub Actions）

作者：后端
最后更新：2025-09-27
状态：实践指引（可直接拷贝到工作流）
适用范围：构建镜像后，在 CI 中实际起容器做健康/公开接口冒烟，失败即阻断部署

关联文档：
- 发布可靠性与回滚策略：docs/deployment/发布可靠性与回滚策略-蓝绿-金丝雀-候选容器预检.md
- CI/CD 方案（GitHub Actions + SSH + Docker）：docs/deployment/后端CI-CD技术方案-GitHub-Actions-SSH-Docker.md

---

## 1. 设计要点

- 真实镜像验证：用构建出的镜像在 Runner 内起容器，校验健康与关键公开接口。
- 依赖容器：用 Actions `services` 启动 Postgres、Redis 供应用连接（Flyway 自动建表）。
- 最小化配置：邮箱/OSS 可留空（应用有默认值），仅需 DB/Redis 即可启动。
- 失败即断：任一检查失败（非 2xx / 超时）都应中断后续部署。

---

## 2. 直接在工作流中添加步骤（推荐）

说明：下方示例假设上一 Job 已构建并推送镜像，并通过 `IMAGE` 变量传入（如 `ghcr.io/<owner>/<repo>:dev-<sha>`）。

```yaml
jobs:
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

    env:
      IMAGE: ${{ needs.build-and-push.outputs.image }} # 例如 ghcr.io/owner/repo:dev-<sha>
      DB_HOST: 127.0.0.1
      DB_PORT: 5432
      DB_USERNAME: postgres
      DB_PASSWORD: postgres
      REDIS_HOST: 127.0.0.1
      REDIS_PORT: 6379
      REDIS_DATABASE: 0
      SPRING_PROFILE: dev

    steps:
      - uses: actions/checkout@v4

      - name: Wait Postgres ready and create database
        run: |
          for i in $(seq 1 60); do
            pg_isready -h 127.0.0.1 -p 5432 -U postgres && break || sleep 2
          done
          sudo apt-get update && sudo apt-get install -y postgresql-client curl jq
          PGPASSWORD=$DB_PASSWORD psql -h 127.0.0.1 -U $DB_USERNAME -p 5432 -c "CREATE DATABASE qiaoya_community;" || true

      - name: Run app container (detached)
        run: |
          docker run -d --name app -p 18520:8520 \
            -e SPRING_PROFILES_ACTIVE=$SPRING_PROFILE \
            -e DB_HOST=$DB_HOST -e DB_PORT=$DB_PORT -e DB_USERNAME=$DB_USERNAME -e DB_PASSWORD=$DB_PASSWORD \
            -e REDIS_HOST=$REDIS_HOST -e REDIS_PORT=$REDIS_PORT -e REDIS_DATABASE=$REDIS_DATABASE \
            "$IMAGE"

      - name: Wait health endpoint up
        run: |
          set -e
          for i in $(seq 1 120); do
            status=$(curl -fsS "http://127.0.0.1:18520/actuator/health" | jq -r .status || echo "")
            if [ "$status" = "UP" ]; then echo "Health UP"; exit 0; fi
            sleep 2
          done
          echo "Health check timeout" >&2
          docker logs --tail=200 app || true
          exit 1

      - name: Public endpoints smoke
        run: |
          set -e
          curl -fsS "http://127.0.0.1:18520/api/public/stats/users" | jq 
          curl -fsS "http://127.0.0.1:18520/api/public/testimonials" | jq 
          curl -fsS "http://127.0.0.1:18520/api/public/subscription-plans" | jq 

      - name: Dump logs on failure
        if: failure()
        run: docker logs --tail=500 app || true

      - name: Cleanup
        if: always()
        run: |
          docker rm -f app || true
```

对接部署：
- 在 `release` 或 `dev` 工作流中，让部署 Job 依赖 `smoke-test`（`needs: smoke-test`）。
- 只有当冒烟成功时才继续通过 SSH 执行部署脚本。

---

## 3. 作为可复用工作流（workflow_call）

可将冒烟步骤拆到单独工作流文件（例如 `.github/workflows/smoke.yml`），供 dev/release 调用：

```yaml
# .github/workflows/smoke.yml
name: Reusable Smoke Test
on:
  workflow_call:
    inputs:
      image:
        description: "Container image to test"
        required: true
        type: string
      profile:
        default: dev
        type: string
jobs:
  smoke:
    runs-on: ubuntu-latest
    # 省略：复用第 2 节相同 services/env/steps，IMAGE/SPRING_PROFILE 从 inputs 取
```

调用示例：
```yaml
jobs:
  build-and-push:
    # ... 构建镜像，输出 image 标签到 outputs

  smoke-test:
    uses: ./.github/workflows/smoke.yml
    needs: build-and-push
    with:
      image: ${{ needs.build-and-push.outputs.image }}
      profile: dev
```

---

## 4. 最小落地清单

- [ ] 在 dev/release 流程中插入 `smoke-test`，成功才部署
- [ ] Postgres/Redis 作为 Actions services 启动，并创建数据库 `qiaoya_community`
- [ ] 健康检查超时阈值 120s，失败时 dump 应用日志
- [ ] 公开接口冒烟通过（统计、评价、套餐）
- [ ] 清理容器，避免污染 Runner

