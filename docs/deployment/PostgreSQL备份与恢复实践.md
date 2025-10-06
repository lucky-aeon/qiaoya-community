# PostgreSQL 备份与恢复实践指南（生产可落地）

> 目标：为敲鸭社区后端在自管或云环境下提供一套主流、可执行、可演练的 PostgreSQL 备份与恢复方案，覆盖逻辑备份、物理备份 + WAL 归档（PITR）、以及 pgBackRest 等业界主流工具。本文以 Linux + PostgreSQL 14–16 为基准。

## 1. 方案综述与选型

- 逻辑备份（pg_dump/pg_dumpall）
  - 特性：按库/表导出为 SQL、自定义（-Fc）或目录（-Fd）格式；跨版本/跨平台友好；便于按对象粒度恢复
  - 适用：小中型库（<100GB）、单库/单表迁移、回滚特定对象
  - 局限：不可增量；大库恢复耗时长；不含 WAL，无法 PITR
- 物理备份（pg_basebackup）+ WAL 归档（PITR）
  - 特性：拷贝数据目录，配合 WAL 支持任意时间点恢复（Point-In-Time Recovery）
  - 适用：生产库、中大规模数据、低 RTO/RPO 诉求
  - 局限：主版本需一致；管理与存储开销较大
- 专业工具（pgBackRest/Barman/WAL-G）
  - 特性：在物理备份基础上提供全/差/增量、压缩/加密、保留策略、对象存储（S3/OSS）、并行、校验、一键 PITR
  - 业界主流：pgBackRest（Crunchy/EDB 生态广泛使用）、Barman（2ndQuadrant/EDB）、WAL-G（云原生）
- 存储快照（LVM/ZFS/云盘）
  - 特性：极快；需确保一致性（协调 PG 产生备份一致点 + 冻结 I/O）；常与 WAL 归档组合

选型建议（经验法则）：
- <100GB 且可容忍较长恢复：每日 `pg_dump -Fc` + `pg_dumpall -g`，保留 7–30 天
- ≥100GB 或生产低 RTO/RPO：pgBackRest（或 Barman/WAL-G）+ WAL 归档：周全量、日差/增量，落盘/对象存储，定期演练
- 云 RDS：启用厂商自动快照 + PITR；关键对象另做 `pg_dump` 以备快速回滚与审计

---

## 2. 逻辑备份方案（pg_dump / pg_dumpall）

### 2.1 快速命令

- 单库（自定义格式，推荐并行恢复）
  ```bash
  PGPASSWORD='PWD' pg_dump -h 127.0.0.1 -p 5432 -U postgres \\
    -d mydb -Fc -f /backup/mydb_$(date +%F).dump
  ```
- 单库（目录格式，可并行导出）
  ```bash
  PGPASSWORD='PWD' pg_dump -h 127.0.0.1 -U postgres -d mydb \\
    -Fd -j 4 -f /backup/mydb_dir_$(date +%F)
  ```
- 全局对象（角色/权限/表空间等）
  ```bash
  PGPASSWORD='PWD' pg_dumpall -h 127.0.0.1 -U postgres -g \\
    > /backup/globals_$(date +%F).sql
  ```

提示：
- 使用 `~/.pgpass` 管理凭据，避免明文密码（见附录 A）
- 大表可用 `-t` 指定或 `--exclude-table-data` 排除数据
- 目录格式 `-Fd` 配合 `-j` 明显加速备份与恢复

### 2.2 恢复

- 先恢复全局对象（若需要角色/权限）
  ```bash
  psql -h 127.0.0.1 -U postgres -d postgres -f /backup/globals_2025-10-05.sql
  ```
- 从自定义格式 `.dump`
  ```bash
  # 在 postgres 库中创建目标库并恢复
  pg_restore -h 127.0.0.1 -U postgres -d postgres -C -j 4 /backup/mydb_2025-10-05.dump
  ```
- 从目录格式 `-Fd`
  ```bash
  pg_restore -h 127.0.0.1 -U postgres -d postgres -C -j 4 /backup/mydb_dir_2025-10-05/
  ```

可选：
- 恢复前用 `pg_restore --list` 检查内容，用 `--use-list` 精细控制
- 使用 `--single-transaction` 提高一致性（注意长事务内存占用）

### 2.3 备份脚本（示例，可直接使用）

```bash
#!/usr/bin/env bash
# 文件：/opt/qiaoya/scripts/backup_pg_logical.sh
set -Eeuo pipefail

# === 可按需修改的参数 ===
PG_HOST="127.0.0.1"; PG_PORT="5432"; PG_USER="postgres"
DB_NAME="qiaoya_community"        # 目标库
BACKUP_DIR="/backup/pg/logic"     # 备份目录（需提前创建）
KEEP_DAYS=14                       # 保留天数
JOBS=4                             # 并行度（用于 -Fd 或恢复）

# === 环境准备 ===
mkdir -p "${BACKUP_DIR}"; umask 077
TS=$(date +%F_%H%M%S)

# 1) 全局对象
pg_dumpall -h "$PG_HOST" -p "$PG_PORT" -U "$PG_USER" -g \\
  > "${BACKUP_DIR}/globals_${TS}.sql"
sha256sum "${BACKUP_DIR}/globals_${TS}.sql" > "${BACKUP_DIR}/globals_${TS}.sql.sha256"

# 2) 目录格式的库备份（并行导出）
pg_dump -h "$PG_HOST" -p "$PG_PORT" -U "$PG_USER" -d "$DB_NAME" \\
  -Fd -j "$JOBS" -f "${BACKUP_DIR}/${DB_NAME}_${TS}.dir"
# 生成校验清单（简单方式：目录打包再校验；也可记录文件列表）
(cd "$BACKUP_DIR" && tar -cf "${DB_NAME}_${TS}.dir.tar" "${DB_NAME}_${TS}.dir")
sha256sum "${BACKUP_DIR}/${DB_NAME}_${TS}.dir.tar" > "${BACKUP_DIR}/${DB_NAME}_${TS}.dir.tar.sha256"
rm -f "${BACKUP_DIR}/${DB_NAME}_${TS}.dir.tar"  # 仅保留校验文件与目录

# 3) 清理过期备份
find "$BACKUP_DIR" -type f -mtime +"$KEEP_DAYS" -delete
find "$BACKUP_DIR" -type d -name "${DB_NAME}_*.dir" -mtime +"$KEEP_DAYS" -exec rm -rf {} +

echo "[OK] logical backup finished at $TS"
```

配套 crontab（例）：
```cron
# 每日 02:30 执行逻辑备份（使用 ~/.pgpass 管理凭据）
30 2 * * * /opt/qiaoya/scripts/backup_pg_logical.sh >> /var/log/pg_backup.log 2>&1
```

---

## 3. 物理备份 + WAL 归档（支持 PITR）

### 3.1 前置配置（postgresql.conf）

```conf
wal_level = replica
archive_mode = on
archive_command = 'test ! -f /backup/pg/wal/%f && cp %p /backup/pg/wal/%f'
# 建议：确保 /backup 独立磁盘/对象存储挂载，并设置合理权限
```

可选（复制/HA 环境常见）：
```conf
max_wal_senders = 10
max_replication_slots = 10
hot_standby = on
```

创建归档目录：
```bash
sudo install -d -m 700 /backup/pg/wal
```

### 3.2 基础备份（pg_basebackup）

```bash
# 需要具有 REPLICATION 权限的用户（如 replicator）
pg_basebackup -h 127.0.0.1 -p 5432 -U replicator \\
  -D /backup/pg/base/$(date +%F_%H%M) -Ft -X stream -z -P
# PG13+ 可用
pg_verifybackup -m /backup/pg/base/$(date +%F_%H%M)/backup_manifest
```

说明：
- `-Ft` 生成 tar 包，`-X stream` 同步打包 WAL，`-z` 压缩，`-P` 显示进度
- 建议将基础备份与 WAL 归档一并推送到可靠存储（异机/对象存储）

### 3.3 恢复到某个时间点（PITR 流程）

1) 停库并清空数据目录（危险操作，谨慎）
```bash
pg_ctl -D "$PGDATA" stop -m fast
rm -rf "$PGDATA"/*
```
2) 解包基础备份到 `$PGDATA`，还原 `postgresql.conf/pg_hba.conf`
3) 设置恢复参数（postgresql.conf）：
```conf
restore_command = 'cp /backup/pg/wal/%f %p'
# 指定目标时间（或目标 LSN/还原到最近）
recovery_target_time = '2025-10-05 10:46:00'
# 回放到目标后自动提升为可写
recovery_target_action = promote
```
4) 创建 `recovery.signal`（空文件）并启动
```bash
: > "$PGDATA/recovery.signal"
pg_ctl -D "$PGDATA" start
```
5) 观察日志与 `pg_last_wal_replay_lsn()`，确认回放到目标点

---

## 4. 使用 pgBackRest（主流推荐）

### 4.1 最小化配置（本地仓库）

`/etc/pgbackrest/pgbackrest.conf`
```ini
[global]
repo1-path=/var/lib/pgbackrest
repo1-retention-full=7
compress-type=zstd
process-max=4

[main]
pg1-path=/var/lib/postgresql/15/main
```
初始化与备份：
```bash
pgbackrest --stanza=main stanza-create
pgbackrest --stanza=main --type=full backup
pgbackrest --stanza=main --type=incr backup
```
恢复 / PITR：
```bash
# 恢复到最新
pgbackrest --stanza=main restore
# 恢复到指定时间
pgbackrest --stanza=main --type=time "--target=2025-10-05 10:46:00" restore
```

### 4.2 S3/OSS 对象存储（示例）

```ini
[global]
repo1-type=s3
repo1-path=/pgbackrest
repo1-s3-bucket=your-bucket
repo1-s3-endpoint=s3.amazonaws.com
repo1-s3-region=ap-southeast-1
repo1-s3-key=AKIA...
repo1-s3-key-secret=SECRET...
repo1-retention-full=7
compress-type=zstd
process-max=8
```

定时：
```cron
# 每日 03:00 增量，每周日 03:00 全量（示例）
0 3 * * 1-6 pgbackrest --stanza=main --type=incr backup
0 3 * * 0   pgbackrest --stanza=main --type=full backup
```

---

## 5. 其它工具与方案简述

- Barman：与 pgBackRest 类似的企业级备份工具，易与物理流复制集成
- WAL-G：擅长云原生与对象存储，轻量灵活
- 存储快照：LVM/ZFS/云盘（EBS/CBS 等），需结合 `pg_backup_start()/pg_backup_stop()`（PG15+；旧版 `pg_start_backup()/pg_stop_backup()`）确保快照一致性，并保留 WAL 以支持 PITR

---

## 6. 监控、校验与安全

- 3-2-1 原则：至少 3 份副本、2 种介质、1 份异地
- 校验：
  - `pg_verifybackup` 校验基础备份
  - 对逻辑备份生成并定期校验 SHA256
  - 定期在沙箱环境做恢复演练（抽样/全量）
- 监控：
  - 归档滞后（WAL backlog）、失败率、容量阈值、最近一次成功备份时间
  - 恢复演练耗时（RTO）与可恢复点（RPO）
- 安全：
  - `.pgpass`/密钥管理、最小权限、备份静态加密（SSE/KMS/自管密钥）
  - 对象存储访问策略最小化、密钥轮转

---

## 7. 恢复演练（SOP）

1) 在新服务器/沙箱创建同版本 PG 实例
2) 准备最新基础备份 + WAL（或选定时间窗）
3) 按 3.3 或 4.1 的流程恢复到目标时间点
4) 运行应用最小冒烟（连接、DDL、核心查询）
5) 校验关键数据量与摘要（如行数、哈希）
6) 记录 RTO/RPO、容量、问题清单，必要时优化参数与流程

---

## 8. 常见问题（FAQ）

- 逻辑备份恢复时提示缺少角色/权限？
  - 先执行 `pg_dumpall -g` 生成的全局对象 SQL 再恢复库/表
- 物理恢复卡住或回放缓慢？
  - 核对 `restore_command` 路径/权限；观察 WAL 目录与服务日志；避免 I/O 限速
- 版本兼容性？
  - 逻辑备份可跨版本（建议用目标版本的 `pg_dump/pg_restore`）；物理备份需同主版本
- 归档目录爆满？
  - 检查归档失败重试、清理策略与对象存储上传；监控 WAL 生成速率

---

## 附录 A：~/.pgpass 示例（权限 0600）

```
# hostname:port:database:username:password
127.0.0.1:5432:*:postgres:YOUR_SECURE_PASSWORD
```

## 附录 B：变量与目录约定

- 备份根目录：`/backup/pg/{logic,wal,base}`（按需调整为独立磁盘/对象存储挂载点）
- 数据目录：`$PGDATA`（如 `/var/lib/postgresql/15/main`）
- 备份用户：`postgres` 或具备最小所需权限的用户（`REPLICATION` 用于物理备份）

---

若需，我可以：
- 生成适配你服务器环境的脚本（含 systemd/cron、告警与校验）
- 接入对象存储（S3/OSS）与密钥管理
- 编写恢复演练 Playbook 并每月例行验证
