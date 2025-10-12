# 方案A：PostgreSQL 逻辑备份（pg_dump）+ 本地/阿里云 OSS + 可观测

本文档给出一套在宿主机执行的数据库逻辑备份方案（方案A），并提供平台“查看备份情况”的落地做法。目标是：简单、稳定、可快速上线；同时支持本地与 OSS 双存储、保留策略、校验与恢复演练。

## 总览

- 备份方法：`pg_dump` 导出全量 + 自定义格式（`-Fc`）+ 内部压缩（`-Z 9`）
- 存储方式：`local | oss | both`（默认 both）
- 对象存储：优先使用 `ossutil64`，亦可选 `rclone`（需预先配置 OSS 后端）
- 完整性：生成 `sha256` 校验文件
- 可观测：生成 JSON 报表（平台可直接读取显示）
- 调度：宿主机 `cron` 或 `systemd timer`

目录与脚本：

- 备份脚本：`ops/backup/db_backup.sh`
- 示例配置：`ops/backup/.env.backup.example`
- 日志目录：默认 `/var/log/db-backup`
- 备份目录：默认 `/data/db-backups/data`
- 报表目录：默认 `/data/db-backups/reports`

## 术语解释（关键技术点）

- pg_dump（逻辑备份工具）
  - PostgreSQL 官方提供的“逻辑备份”工具，用一致性快照（MVCC）导出一个数据库的结构与数据，不阻塞写
  - “全量导出”指导出目标数据库的全部对象（模式、表、索引、数据等），不包含“全局对象”（角色/权限）
  - 常用格式选项：
    - `-F c` 自定义格式（custom，文件扩展名常用 `.dump`），支持随机访问、对象级筛选、并行恢复
    - `-F p` 纯文本 SQL（plain），恢复用 `psql`，不支持并行恢复
    - `-F d` 目录格式（directory），生成目录及多文件，支持并行恢复
  - 压缩选项：`-Z 0..9`（仅对 `-F c`/`-F d` 生效），0 表示不压缩，9 压缩率最高（CPU 开销更大）
  - 本方案使用 `-Fc -Z 9`，即“自定义格式 + 内部压缩”，无需再额外 gzip 压缩

- pg_restore（恢复工具）
  - 用于从 `pg_dump -F c` 或 `-F d` 生成的备份恢复到数据库（对 `-F p` 纯文本需用 `psql -f`）
  - 支持并行恢复（`-j N`）、清理再建（`-c/--clean`）、按对象/模式过滤等

- pg_dumpall（全实例/全局对象导出）
  - 导出整个实例的所有数据库或仅“全局对象”（角色、权限、表空间）
  - 本方案开启 `--globals-only` 生成一份 `*_globals.sql`，用于恢复角色/权限等全局信息

- SHA-256 校验（完整性验证）
  - 使用 `sha256sum`/`shasum -a 256` 计算备份文件的加密哈希，生成 `*.sha256` 校验文件
  - 用途：在传输或下载后校验备份文件是否被破坏或截断（验证完整性，不是加密）
  - 示例：`sha256sum -c <dump>.sha256`（返回 OK 代表校验通过）

## 环境准备（宿主机）

1) 安装 PostgreSQL 客户端工具（包含 `pg_dump/pg_restore/pg_dumpall`）

2) 安装上传工具（二选一）

- `ossutil64`（推荐）：
  - 下载地址（阿里云官方文档），将可执行文件放入 `PATH`
  - 使用 RAM 子账号最小权限（仅对备份桶授予读/写）
- `rclone`（可选）：
  - 安装 rclone；执行 `rclone config` 新建一个名为 `oss` 的 remote，类型选 OSS

3) 准备目录与权限（示例）

```bash
sudo mkdir -p /data/db-backups/data /data/db-backups/reports /var/log/db-backup
sudo chown -R postgres:postgres /data/db-backups /var/log/db-backup
```

## 配置与运行

1) 复制示例 env 文件并按需修改

```bash
cp ops/backup/.env.backup.example /etc/db-backup.env
# 编辑 /etc/db-backup.env，至少需填写 PG 连接、存储模式、OSS 参数
```

关键项说明：

- `PG_HOST/PG_PORT/PG_DATABASE/PG_USER`：数据库连接（备份从宿主机直连 PG）
- `PGPASSWORD`：建议由 cron 任务注入（避免写入文件）或使用 `.pgpass`
- `BACKUP_MODE`：`local | oss | both`
- `BACKUP_BASE_DIR/BACKUP_LOG_DIR/RETENTION_DAYS`：目录与保留策略
- `BACKUP_UPLOAD_TOOL`：`ossutil | rclone`
- `OSS_*` 或 `RCLONE_*`：对应上传工具配置

2) 手动试跑

```bash
chmod +x ops/backup/db_backup.sh
export BACKUP_ENV_FILE=/etc/db-backup.env
export PGPASSWORD=your_password
bash ops/backup/db_backup.sh
```

应产生：

- 备份文件：`/data/db-backups/data/<db>_<UTC时间>_<主机>.dump`
- 校验文件：`*.dump.sha256`
- 全局对象：`*_globals.sql`（角色/权限，可选）
- JSON 报表：`/data/db-backups/reports/<同名>.json`
- 日志：`/var/log/db-backup/backup_<日期>.log`

3) 定时任务（cron 示例：每天 02:00 执行）

```cron
SHELL=/bin/bash
PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
PGPASSWORD=your_password
BACKUP_ENV_FILE=/etc/db-backup.env
0 2 * * * /bin/bash /path/to/repo/ops/backup/db_backup.sh >> /var/log/db-backup/cron.log 2>&1
```

备注：更安全的做法是使用 `~/.pgpass` 文件替代 `PGPASSWORD`。

## JSON 报表结构（平台读取）

每次备份会生成一个 JSON 报表，包含关键元数据，便于平台展示：

```json
{
  "database": "community",
  "host": "127.0.0.1",
  "port": "5432",
  "startedAt": "2025-10-11T18:00:00Z",
  "finishedAt": "2025-10-11T18:05:12Z",
  "durationSeconds": 312,
  "mode": "both",
  "tool": "ossutil",
  "status": "SUCCESS",
  "errorMessage": "",
  "local": {
    "file": "/data/db-backups/data/community_20251011T180000Z_host.dump",
    "globalsFile": "/data/db-backups/data/community_20251011T180000Z_host_globals.sql",
    "checksumSha256": "...",
    "sizeBytes": 123456789
  },
  "remote": {
    "urls": ["oss://<bucket>/postgres/backups/community_20251011T180000Z_host.dump"]
  }
}
```

平台可以按 `reports` 目录做列表、读取与分页，或按文件名时间倒序展示最新一次结果。

## 恢复指引（方案A）

1) 取回备份：从本地或 OSS 下载 `.dump`（以及 `*_globals.sql`）

2) 校验完整性：

```bash
sha256sum -c <dump>.sha256
# 或 shasum -a 256 -c <dump>.sha256（macOS）
```

3) 创建空数据库并恢复数据：

```bash
createdb -h <host> -p <port> -U <user> <db>
pg_restore -h <host> -p <port> -U <user> -d <db> -c <dump>.dump
# 如需并行恢复：pg_restore -j 4 ...（注意：并行恢复对 -Fc 是支持的）
```

4) 恢复全局对象（需要时）：

```bash
psql -h <host> -p <port> -U <user> -f <dump>_globals.sql postgres
```

5) 验证：连接应用、检查关键表数量与业务用例。

## OSS 最佳实践

- 使用 RAM 子账号 + 最小权限（仅给备份桶授予 `PutObject/GetObject/DeleteObject`）
- 按前缀（如 `postgres/backups/`）设置生命周期：近 7 天标准存储、30 天后转低频、90 天后过期删除
- 可选加密：本地使用 `gpg/age`，或启用 OSS 服务器端加密（SSE/KMS）

## 平台“查看备份情况”的实现

这里提供两种落地方式，均遵守本项目 DDD 规范。

### 方式一：读取 JSON 报表（零数据库改动，快速上线）

思路：备份脚本生成的 JSON 即为“事实来源（source of truth）”。平台通过应用层读取报表目录、解析 JSON，并输出 DTO。

- Infrastructure：
  - `infrastructure/config/BackupP  roperties` 配置 `reportsDir`
  - `infrastructure/service/BackupReportReader` 负责扫描目录并解析 JSON
- Domain（ops 子域）：
  - `domain/ops/service/BackupDomainService` 组合读取逻辑（可按时间/状态过滤）
- Application：
  - `application/ops/dto/BackupJobDTO`
  - `application/ops/assembler/BackupAssembler`
  - `application/ops/service/BackupAppService` 提供查询接口（分页/筛选）

调用链：Application → Domain Service → Infrastructure（读取 JSON）→ DTO 返回。

示例 DTO 字段建议：

```java
public class BackupJobDTO {
    private String database;
    private String startedAt;  // ISO8601
    private String finishedAt; // ISO8601
    private Long durationSeconds;
    private String mode;       // local/oss/both
    private String status;     // SUCCESS/FAILED
    private Long sizeBytes;
    private String localFile;
    private List<String> remoteUrls;
}
```

优点：
- 无需新增表结构；依赖脚本即可出数
- 读取目录即可分页/筛选，改造成本低

注意：
- 确保应用有权限读取 `reportsDir`
- 报表文件命名已包含时间戳，可按文件名倒序

### 方式二：写入数据库表（更强的查询能力）

若需要更复杂的筛选、跨维度统计与审计，建议新增表并由脚本上报或应用消费 JSON 入库。

1) 数据结构（Flyway 迁移建议）

`src/main/resources/db/migration/Vxxx__create_db_backup_job.sql`

```sql
CREATE TABLE IF NOT EXISTS db_backup_job (
  id           VARCHAR(36) PRIMARY KEY,
  started_at   TIMESTAMPTZ NOT NULL,
  finished_at  TIMESTAMPTZ NOT NULL,
  duration_sec BIGINT NOT NULL,
  database     TEXT NOT NULL,
  mode         TEXT NOT NULL,   -- local/oss/both
  status       TEXT NOT NULL,   -- SUCCESS/FAILED
  size_bytes   BIGINT,
  local_file   TEXT,
  remote_urls  TEXT,            -- JSON 数组字符串或用 jsonb
  checksum     TEXT,
  error_msg    TEXT,
  create_time  TIMESTAMPTZ DEFAULT NOW(),
  update_time  TIMESTAMPTZ DEFAULT NOW(),
  deleted      BOOLEAN DEFAULT FALSE
);
```

2) 领域与应用（ops 子域示意）

- `domain/ops/entity/BackupJobEntity extends BaseEntity`
- `domain/ops/repository/BackupJobRepository extends BaseMapper<BackupJobEntity>`
- `domain/ops/service/BackupDomainService`
- `application/ops/dto/BackupJobDTO`
- `application/ops/assembler/BackupAssembler`
- `application/ops/service/BackupAppService`

3) 上报路径（两选一）

- A. 脚本直接写库：脚本在完成后用 `psql` 执行 `INSERT`（需最小权限账号）
- B. 应用消费 JSON：应用层定时扫描 `reportsDir`，将未入库的报表写入表，随后对外提供查询接口

## 监控与告警（建议）

- 失败重试：脚本失败时退出码非 0，运维侧可通过 `systemd` 或任务编排重试
- 告警渠道：失败时触发飞书/钉钉/邮件（在脚本尾部留 Hook 或由平台读取 JSON 报表后告警）
- 容量预警：监控本地磁盘与 OSS 存储用量；本地按 `RETENTION_DAYS` 清理

## 常见问题（FAQ）

- 大库恢复慢怎么办？
  - 方案A为逻辑备份，恢复需要重建索引，时间和数据量线性相关；可用 `pg_restore -j N` 并行加速
- 需要按时间点恢复（PITR）怎么办？
  - 需升级为方案B（WAL-G/pgBackRest），不在本文档范围
- 忘记备份角色/权限？
  - 开启 `INCLUDE_GLOBALS=true`，脚本会生成 `*_globals.sql`

## 变更记录

- 新增备份脚本：`ops/backup/db_backup.sh`
- 新增示例环境：`ops/backup/.env.backup.example`
- 本文档：方案A 落地与平台可观测实现指引
