# 日志系统选型与落地（Loki/Promtail/Grafana）

> 场景：单台 2C4G 服务器，Docker 部署，当前内存使用约 72%，后端为 Spring Boot 3.2 + Logback，DDD 分层（仅在基础设施层改造）。

---

## 1. 背景与目标

- 背景
  - 现有环境为单机 Docker 部署，机器规格 2 vCPU / 4GB RAM，内存余量约 1GB
  - 需要集中化查询应用日志、支持简单分析与告警，尽量不侵入业务代码和 DDD 分层
  - 预算与资源有限，不适合运维重量级 ELK（Elasticsearch + Logstash + Kibana）
- 目标
  - 统一输出结构化（JSON）日志，采集容器 stdout
  - 采集→存储→可视化全链路最小可用：Promtail → Loki → Grafana
  - 可按天保留（默认 7 天），内存/磁盘可控，可水平演进

结论：选择 Loki Stack（Promtail + Loki + Grafana），在 2C4G 上资源占用显著低于 ELK，满足检索、聚合与基础告警需求。

---

## 2. 术语与名词解释（按使用顺序）

- stdout/stderr：容器标准输出/标准错误。Docker 默认用 `json-file` 驱动把 stdout 写入宿主机日志文件
- Docker json-file：Docker 的默认日志驱动，在宿主机目录 `/var/lib/docker/containers/<container-id>/<id>-json.log` 持久化日志并轮转
- 结构化日志（Structured Logging）：使用 JSON 等统一字段格式输出日志，便于检索与聚合（如 `level`,`message`,`service`,`env`）
- Promtail：Loki 的日志采集代理，从 Docker 日志文件读取，解析并打标签（labels），发送到 Loki
- Labels（标签）：Loki 中用于索引/过滤的键值，如 `job`,`app`,`container`，决定查询粒度与成本
- Stream（日志流）：具有相同标签集合的一组日志记录
- Positions file：Promtail 的断点文件，记录已读取到日志文件的偏移量，重启后从断点续传
- Pipeline stages：Promtail 的处理流水线，可进行 `docker` 解码、`json` 解析、字段提取与脱敏
- LogQL：Loki 的查询语言，基于标签过滤和日志内容匹配，支持聚合函数（如 `count_over_time`）
- Loki：面向日志的存储与查询系统，索引成本低、资源友好；本方案使用 `boltdb-shipper + filesystem`（单机）
- Retention（保留期）：日志保存时间，超期后由 Loki 压缩器（Compactor）清理
- Grafana：可视化与告警平台，配置 Loki 数据源后可检索日志、做图表与告警

---

## 3. 选型对比与结论

- ELK：功能强、全文检索/DSL 丰富，但单 ES 节点通常需要 ≥2–4GB 内存堆，叠加 Kibana/Logstash 无法在 2C4G 稳定运行
- Loki Stack：资源占用小（Loki 200–400MB、Promtail 30–80MB、Grafana 150–250MB），部署简单，满足大多数查询/聚合与告警

结论：在当前资源下采用 Loki Stack。未来若数据量显著增长或需要强全文与 SIEM，可迁移至 Elastic Cloud 或分布式 Loki（或对象存储后端）。

---

## 4. 目标架构与数据流

```
Spring Boot(Logback JSON stdout)
      │
      ▼
Docker json-file(日志文件, 可轮转)
      │  (Promtail 通过 positions.yaml 断点续传)
      ▼
Promtail(解析/打标签/脱敏) ─────► Loki(存储/索引/保留期)
                                          │
                                          ▼
                                     Grafana(检索/面板/告警)
```

容器重启与日志：已发往 Loki 的日志不受重启影响；未送达的日志仍在宿主机日志文件里，Promtail 会从 positions 续传。保留期由 Loki 控制（与容器重启无关）。

---

## 5. 部署步骤（Docker Compose 示例）

> 以下为可直接落地的一套最小化配置。建议将配置文件放在 `deploy/observability/loki/` 目录（自行创建）。

### 5.1 docker-compose.loki.yml（含 Grafana Provisioning 挂载）

```yaml
version: "3.8"
services:
  loki:
    image: grafana/loki:2.9.2
    command: ["-config.file=/etc/loki/loki-config.yml"]
    ports: ["3100:3100"]
    volumes:
      - ./loki-config.yml:/etc/loki/loki-config.yml:ro
      - loki-data:/loki
    deploy:
      resources:
        limits:
          memory: 512M

  promtail:
    image: grafana/promtail:2.9.2
    command: ["-config.file=/etc/promtail/promtail-config.yml"]
    volumes:
      - ./promtail-config.yml:/etc/promtail/promtail-config.yml:ro
      - /var/lib/docker/containers:/var/lib/docker/containers:ro
      - /var/run/docker.sock:/var/run/docker.sock:ro
      - promtail-positions:/tmp
    depends_on: [loki]
    deploy:
      resources:
        limits:
          memory: 64M

  grafana:
    image: grafana/grafana:10.4.2
    ports: ["5601:3000"]
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=change_me
      - LOKI_URL=http://loki:3100
    volumes:
      - grafana-data:/var/lib/grafana
      - ./grafana/provisioning:/etc/grafana/provisioning:ro
      - ./grafana/dashboards:/var/lib/grafana/dashboards:ro
    depends_on: [loki]
    deploy:
      resources:
        limits:
          memory: 256M

volumes:
  loki-data:
  promtail-positions:
  grafana-data:
```

> 端口说明：Loki `3100`，Grafana 通过宿主 `5601` 访问（避免与 Kibana 端口混淆，可自行调整）。

### 5.2 Loki 配置（loki-config.yml）

```yaml
server:
  http_listen_port: 3100

common:
  compactor_address: http://localhost:3100

storage_config:
  boltdb_shipper:
    active_index_directory: /loki/index
    cache_location: /loki/boltdb-cache
  filesystem:
    directory: /loki/chunks

schema_config:
  configs:
    - from: 2023-01-01
      store: boltdb-shipper
      object_store: filesystem
      schema: v13
      index:
        prefix: index_
        period: 24h

compactor:
  working_directory: /loki/compactor
  retention_enabled: true

limits_config:
  # 全局保留期，示例 7 天
  retention_period: 168h
  ingestion_rate_mb: 8
  ingestion_burst_size_mb: 16

chunk_store_config:
  max_look_back_period: 168h

table_manager:
  retention_deletes_enabled: true
  retention_period: 168h
```

### 5.3 Promtail 配置（promtail-config.yml）

```yaml
server:
  http_listen_port: 9080
  grpc_listen_port: 0

positions:
  filename: /tmp/positions.yaml  # 映射到卷以保证断点续传

clients:
  - url: http://loki:3100/loki/api/v1/push

scrape_configs:
  - job_name: docker-logs
    docker_sd_configs:
      - host: unix:///var/run/docker.sock
        refresh_interval: 30s
    pipeline_stages:
      - docker: {}
      - json:
          expressions:
            level: level
            logger: logger
            thread: thread
            service: service
            env: env
            traceId: traceId
      - labels:
          level:
          service:
          env:
    relabel_configs:
      # 读取 json-file 的实际日志路径
      - source_labels: ['__meta_docker_container_id']
        target_label: '__path__'
        replacement: '/var/lib/docker/containers/$1/$1-json.log'
      # 常用标签
      - source_labels: ['__meta_docker_container_name']
        target_label: 'container'
      - source_labels: ['__meta_docker_container_label_com_docker_compose_service']
        target_label: 'compose_service'
      - source_labels: ['__meta_docker_container_label_com_docker_compose_project']
        target_label: 'compose_project'
      - target_label: 'job'
        replacement: 'docker'
```

### 5.4 Grafana Provisioning（自动创建数据源/仪表盘/告警）

目录与文件（已在本仓库提供模板）：

- `deploy/observability/loki/grafana/provisioning/datasources/loki.yaml`
  - 自动创建 Loki 数据源（UID `loki`，URL 由环境变量 `LOKI_URL` 提供）
- `deploy/observability/loki/grafana/provisioning/dashboards/dashboards.yaml`
  - 自动加载 `deploy/observability/loki/grafana/dashboards/**.json` 面板
- `deploy/observability/loki/grafana/provisioning/alerting/log-error-rate.yaml`
  - 示例告警：5 分钟内 ERROR>0 告警

> 提示：如需更多数据源/告警，仿照上述文件新增即可；变更后重启 Grafana 生效。

### 5.5 应用侧日志（结构化 JSON 到 stdout）

1) 依赖（`pom.xml`）

```xml
<dependency>
  <groupId>net.logstash.logback</groupId>
  <artifactId>logstash-logback-encoder</artifactId>
  <version>7.4</version>
  <scope>runtime</scope>
  <!-- 仅运行时需要，不影响编译 -->
</dependency>
```

2) `src/main/resources/logback-spring.xml`（示例片段）

```xml
<configuration>
  <springProperty name="APP_NAME" source="spring.application.name" defaultValue="qiaoya-community"/>
  <springProperty name="ENV" source="spring.profiles.active" defaultValue="dev"/>

  <appender name="JSON_CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder class="net.logstash.logback.encoder.LogstashEncoder">
      <customFields>{"service":"${APP_NAME}","env":"${ENV}"}</customFields>
    </encoder>
  </appender>

  <root level="INFO">
    <appender-ref ref="JSON_CONSOLE"/>
  </root>
</configuration>
```

3) Docker 日志轮转（业务容器）

```yaml
logging:
  driver: "json-file"
  options:
    max-size: "10m"
    max-file: "3"
```

---

## 6. 使用与运维

### 6.1 启动

1) 在 `deploy/observability/loki/` 保存上面的三个文件：
   - `docker-compose.loki.yml`
   - `loki-config.yml`
   - `promtail-config.yml`

2) 启动服务

```bash
cd deploy/observability/loki
docker compose -f docker-compose.loki.yml up -d
```

3) 访问 Grafana：`http://<服务器IP>:5601`，默认账户 `admin / change_me`（首次登录请立即修改）

4) 数据源将由 Provisioning 自动创建；如需手动验证，可在“管理 → 数据源”查看 `Loki` 是否存在

### 6.2 常用 LogQL 查询

- 最近 5 分钟 ERROR 日志计数（按服务）

```logql
sum by (service) (count_over_time({level="ERROR"}[5m]))
```

- 按服务筛选并包含关键字

```logql
{service="qiaoya-community"} |= "Exception"
```

- JSON 字段过滤（level 来自 JSON 解析）

```logql
{service="qiaoya-community", level="WARN"}
```

### 6.3 告警建议

- 错误率告警：基于 `count_over_time({level="ERROR"}[5m])` 超阈值触发
- 无日志告警：某服务在近 10 分钟无日志，则可能实例异常
- 关键字告警：匹配特定异常模式（如数据库连接失败）

### 6.4 资源与保留期

- 参考上限：Loki 512MB、Promtail 64MB、Grafana 256MB；实际根据量级微调
- 保留期默认 7 天（`limits_config.retention_period`），可改为 `720h`（30 天）
- 磁盘占用与标签相关：标签越多、越高基数，索引越大——控制标签数量与基数

### 6.5 安全与合规

- Grafana 开启强密码，限制公网暴露（建议仅内网访问或反向代理鉴权）
- 避免在日志中打印密码、Token、身份证/手机号等敏感信息；必要时在 Promtail `pipeline_stages` 做脱敏

---

## 7. FAQ（基于常见问题）

1) 容器重启日志会丢吗？
   - 已写入 Loki 的日志按保留期保存；未送达部分仍在宿主机日志文件中，Promtail 依据 positions 断点续传

2) 保留多久？如何修改？
   - 由 `loki-config.yml` 中的 `limits_config.retention_period` 决定（如 `168h`=7 天）

3) Docker 本地日志是否会撑满磁盘？
   - 在业务容器设置日志轮转（`max-size`/`max-file`），建议 10–50MB 级别缓冲

4) 查询慢或结果不全？
   - 降低高基数标签（如 `container` 在大量短生命周期容器时），缩小时间范围，避免使用正则的大范围模糊

5) 需要全文检索或复杂清洗怎么办？
   - 可接入 Grafana Loki 的 `query-frontend`、或迁移到 Elastic Cloud；也可在应用侧增加更丰富的结构化字段

---

## 8. 与 DDD 架构的契合

- 改动仅涉及基础设施层与资源文件：
  - 应用端：Logback 输出 JSON 至 stdout（不改变 Application/Domain 职责）
  - 观测侧：Promtail/Loki/Grafana 在部署层接入
- 不在 Domain 层做参数格式校验/日志采集逻辑，保持分层职责纯粹

---

## 9. 演进与备选

- 规模扩大：Loki 迁移对象存储（S3/OSS）+ 分布式部署；或转 Elastic Cloud/SaaS（Datadog/New Relic）
- 指标与链路：结合 Prometheus/Tempo，Grafana 实现指标/日志/链路三栈联动；或使用 OpenTelemetry Collector 汇聚

---

## 10. 快速清单（Checklist）

- [ ] 业务日志改为 JSON stdout（logstash-encoder）
- [ ] 给业务容器启用 Docker 日志轮转
- [ ] 部署 Promtail/Loki/Grafana（Compose）
- [ ] 配置 Loki 保留期（默认 7 天）与数据卷
- [ ] Grafana 添加 Loki 数据源与基础告警
- [ ] 敏感信息检查与脱敏

---

如需，我可以把上述 `docker-compose.loki.yml / loki-config.yml / promtail-config.yml` 以模板形式补充到 `deploy/observability/loki/` 目录，或按你的实际端口和安全策略调整配置。

---

## 11. 生产部署（蓝绿）集成与命令

本项目生产采用蓝绿发布（两个容器：blue/green，只有一个接流量）。日志栈与业务解耦，统一采集两边容器输出到一套 Loki，不会产生两份存储。按下面步骤部署即可。

### 11.1 确保日志栈运行（脚本已内置）

从现在开始，蓝绿发布脚本会在执行前自动“确保日志栈已启动”（幂等）：
- 默认开启（`WITH_LOGS=1`）；设置 `WITH_LOGS=0` 可跳过
- 默认 compose 搜索顺序：
  1) 脚本同级目录的 `loki/docker-compose.loki.yml`
  2) 仓库结构下的 `deploy/observability/loki/docker-compose.loki.yml`
  - 可用 `LOG_STACK_COMPOSE` 显式覆盖

手动健康检查（排障用）：
- `curl http://127.0.0.1:3100/ready` → `ready`（Loki）
- `curl http://127.0.0.1:9080/targets`（Promtail 目标页）

### 11.2 蓝绿发布（含 30MB 日志轮转）

仍然使用仓库自带蓝绿脚本 `deploy/scripts/deploy-qiaoya-bluegreen.sh`；脚本默认：
- 自动确保日志栈运行（可通过 `WITH_LOGS=0` 关闭）
- 为业务容器启用 `json-file` 驱动与 30MB 轮转（`10m × 3`）

最简发布命令：

```bash
REPO=/www/project/qiaoya-community-backend
IMAGE=<你的生产镜像>
ENV_FILE=/etc/qiaoya.backend.env   # 按 deploy/env/qiaoya.backend.prod.env.example 复制并填好

IMAGE=$IMAGE PROFILE=prod ENV_FILE=$ENV_FILE \
  $REPO/deploy/scripts/deploy-qiaoya-bluegreen.sh
```

覆盖项（可选）：
- `LOG_OPTS` 可自定义轮转策略，例如 `LOG_OPTS="--log-driver json-file --log-opt max-size=50m --log-opt max-file=5"`
- `WITH_LOGS=0` 跳过日志栈校验；`LOG_STACK_COMPOSE=/path/to/compose.yml` 指定自定义路径

脚本会：拉镜像 → 在 8521（或 8520）起“新一侧”容器 → 健康检查 `/api/public/health` →（可选）通过 Nginx 切流 → 保留旧容器以便快速回滚。日志由 Promtail 自动发现并采集。

### 11.3 生产查看日志（Grafana）

- 访问：`http://<服务器IP>:5601`，首次登录 `admin / change_me`，请立刻改密
- Explore 查询示例：
  - 全部：`{service="qiaoya-community"}`
  - 只看 green：`{service="qiaoya-community", container="qiaoya-community-backend-green"}`
  - 只看 blue：`{service="qiaoya-community", container="qiaoya-community-backend-blue"}`
  - 错误趋势：`sum by (container)(count_over_time({service="qiaoya-community", level="ERROR"}[5m]))`

说明：蓝绿期间备用容器也会产生启动/健康日志，均进入同一套 Loki，仅以 `container` 标签区分；不影响存储成本与查询。

### 11.4 常见问题（生产）

- Explore 无数据：
  - `docker logs <容器名> | head` 是否为 JSON（logback 已配置）
  - `curl http://127.0.0.1:9080/targets` 是否出现 blue/green 目标
  - `curl http://127.0.0.1:3100/ready` 是否 `ready`
- Grafana 没有 Loki 数据源：
  - 重启 grafana：`docker compose -f $REPO/deploy/observability/loki/docker-compose.loki.yml restart grafana`
  - 查看日志应有 `Provisioned datasource Loki`
  - 调整保留期：编辑 `$REPO/deploy/observability/loki/loki-config.yml` 的 `limits_config.retention_period`，然后 `restart loki`

---

## 12. 指标接入（JVM + HTTP请求，最小实现）

本节在不显著增加资源的前提下，新增 Prometheus 抓取应用指标（JVM/HTTP）。

### 12.1 应用侧（已集成）
- 依赖：`spring-boot-starter-actuator`、`micrometer-registry-prometheus`（已加入 pom.xml）
- 配置：`application.yml` 已暴露 `/actuator/prometheus`，并将 `service` 作为全局标签

### 12.2 指标栈（已与日志栈合并）
- 在 `docker-compose.loki.yml` 中新增了 `prometheus` 服务，内存上限 256MB，开放 9090 端口
- Prometheus 配置：`deploy/observability/loki/prometheus/prometheus.yml`
  - 抓取目标：`host.docker.internal:8520`（容器通过 host-gateway 访问宿主机端口）
- Grafana 数据源：已通过 Provisioning 自动创建 `Prometheus` 数据源（指向 `http://prometheus:9090`）

启动/更新：
```bash
# 确保日志+指标栈全部 up（脚本也会自动执行）
docker compose -f $REPO/deploy/observability/loki/docker-compose.loki.yml up -d
```

Grafana 查看：
- Explore 选择数据源 `Prometheus`
- 常用查询
  - HTTP 请求量：`sum(rate(http_server_requests_seconds_count[5m])) by (uri)`
  - HTTP 时延 P95：`histogram_quantile(0.95, sum by (le, uri)(rate(http_server_requests_seconds_bucket[5m])))`
  - JVM 堆使用：`jvm_memory_used_bytes{area="heap"}` 与 `jvm_memory_max_bytes{area="heap"}`

注意：若 Prometheus 无法抓取到应用，请确认应用端口 8520 对宿主机可达，且 Prometheus 服务包含 `extra_hosts: host.docker.internal:host-gateway`。
