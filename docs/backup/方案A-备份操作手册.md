# 方案A 备份操作手册（宿主机执行）

适用范围：本项目 PostgreSQL 数据库，采用逻辑备份（pg_dump 自定义格式 -Fc，内部压缩 -Z 9），支持本地与阿里云 OSS 存储，提供 JSON 报表供平台查看。

关联文档：`docs/backup/方案A-定时备份与平台可观测.md`

## 1. 目标与原则

- 目标：稳定产出可恢复的备份，兼顾本地落盘与 OSS 远端；提供可观测报表，便于平台与运维巡检
- 原则：最小依赖、最小权限、可回滚、可演练、可验证完整性

## 2. 前置条件

- 已安装 PostgreSQL 客户端：`pg_dump/pg_restore/pg_dumpall`
- 已安装上传工具：`ossutil64`（推荐）或 `rclone`
- 网络：宿主机能连接 PostgreSQL 与 OSS 端点
- 账号：
  - 数据库侧：建议使用备份专用账号，至少具备目标库对象的 `SELECT`、模式的 `USAGE`、序列的 `USAGE/SELECT` 权限；若需导出全局对象（角色/权限），需超管或等效权限
  - OSS 侧：RAM 子账号，最小权限访问目标桶（`PutObject/GetObject/DeleteObject`）

## 3. 目录结构

- 备份脚本：`ops/backup/db_backup.sh`
- 示例配置：`ops/backup/.env.backup.example`
- 默认数据目录：`/data/db-backups/data`
- 默认报表目录：`/data/db-backups/reports`
- 默认日志目录：`/var/log/db-backup`

## 4. 配置项总表（env）

- PG_HOST：PostgreSQL 主机地址（示例：`127.0.0.1`）
- PG_PORT：PostgreSQL 端口（默认 `5432`）
- PG_DATABASE：需备份的数据库名（示例：`community`）
- PG_USER：备份账号（示例：`postgres` 或备份专用账号）
- PGPASSWORD：数据库密码（不写入文件，建议由 cron/systemd 注入，或使用 `~/.pgpass`）
- BACKUP_MODE：存储模式，`local|oss|both`（默认 `both`）
- BACKUP_BASE_DIR：本地备份根目录（默认 `/data/db-backups`）
- BACKUP_LOG_DIR：日志目录（默认 `/var/log/db-backup`）
- RETENTION_DAYS：本地保留天数（默认 `7`）
- INCLUDE_GLOBALS：是否导出全局对象 `pg_dumpall --globals-only`（默认 `true`）
- PG_DUMP_EXTRA_OPTS：pg_dump 额外参数（示例：`--exclude-table-data=large_audit_table`）
- BACKUP_UPLOAD_TOOL：上传工具，`ossutil|rclone`（默认 `ossutil`）
- OSS_BUCKET：OSS 桶名（示例：`your-oss-bucket`）
- OSS_PREFIX：上传路径前缀（示例：`postgres/backups`）
- OSS_ENDPOINT：OSS 区域端点（示例：`oss-cn-hangzhou.aliyuncs.com`）
- OSS_ACCESS_KEY_ID：RAM 子账号的 AK
- OSS_ACCESS_KEY_SECRET：RAM 子账号的 SK
- RCLONE_REMOTE：rclone 远端名（默认 `oss`）
- RCLONE_PATH：rclone 目标路径（示例：`postgres/backups`）
- BACKUP_ENV_FILE：脚本读取的 env 文件路径（示例：`/etc/db-backup.env`）

建议：复制 `ops/backup/.env.backup.example` 到 `/etc/db-backup.env`，按需修改；将该文件权限设为仅 root/备份用户可读（0600）。

## 5. 安装与部署步骤

1) 安装工具
   - PostgreSQL 客户端：通过包管理器安装（包含 `pg_dump/pg_restore/pg_dumpall`）
   - 上传工具：`ossutil64` 放入 `PATH`；或安装 `rclone` 并执行 `rclone config` 配置 OSS 远端

2) 准备目录
   - `sudo mkdir -p /data/db-backups/data /data/db-backups/reports /var/log/db-backup`
   - `sudo chown -R <backup_user>:<group> /data/db-backups /var/log/db-backup`

3) 准备配置
   - `cp ops/backup/.env.backup.example /etc/db-backup.env`
   - 编辑 `/etc/db-backup.env` 填写数据库与 OSS 等参数
   - 保护权限：`chmod 600 /etc/db-backup.env`

4) 首次执行（手动）
   - `chmod +x ops/backup/db_backup.sh`
   - `BACKUP_ENV_FILE=/etc/db-backup.env PGPASSWORD=****** bash ops/backup/db_backup.sh`

5) 结果验证
   - 本地产物：`/data/db-backups/data/*.dump`、`*.dump.sha256`、`*_globals.sql`
   - 报表：`/data/db-backups/reports/*.json`（status=SUCCESS 且 sizeBytes>0）
   - 日志：`/var/log/db-backup/backup_YYYYMMDD.log`
   - 远端校验：
     - ossutil：`ossutil64 ls oss://<bucket>/<prefix>/ -e <endpoint> -i <ak> -k <sk>`
     - rclone：`rclone ls <remote>:<path>`

## 6. 定时执行配置

方式A：cron（示例：每天 02:00 执行）

```
SHELL=/bin/bash
PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
PGPASSWORD=your_password
BACKUP_ENV_FILE=/etc/db-backup.env
0 2 * * * /bin/bash /path/to/repo/ops/backup/db_backup.sh >> /var/log/db-backup/cron.log 2>&1
```

方式B：systemd timer（推荐）

`/etc/systemd/system/db-backup.service`
```
[Unit]
Description=PostgreSQL logical backup (scheme A)

[Service]
Type=oneshot
Environment=BACKUP_ENV_FILE=/etc/db-backup.env
Environment=PGPASSWORD=your_password
ExecStart=/bin/bash /path/to/repo/ops/backup/db_backup.sh
User=<backup_user>
Group=<group>
```

`/etc/systemd/system/db-backup.timer`
```
[Unit]
Description=Run db-backup daily at 02:00

[Timer]
OnCalendar=*-*-* 02:00:00
Persistent=true

[Install]
WantedBy=timers.target
```

启用：`systemctl daemon-reload && systemctl enable --now db-backup.timer`

## 7. 日常巡检与平台对接

- 巡检要点
  - 最近一次报表 JSON：`status=SUCCESS`，`durationSeconds` 合理，`sizeBytes` 非 0
  - 日志无 ERROR，OSS 目录存在对应对象
  - 本地保留策略正常执行（过期备份被清理）
- 平台读取 JSON 报表目录 `/data/db-backups/reports`，解析并分页展示（字段与示例见关联文档）

平台 API（已内置）：

- 最新一次：`GET /api/admin/backup/latest`
- 分页列表：`GET /api/admin/backup/list?pageNum=1&pageSize=10&status=SUCCESS`
  - `status` 可选：`SUCCESS` 或 `FAILED`

## 8. 恢复操作（简版）

- 校验：`sha256sum -c <dump>.sha256`
- 创建库：`createdb -h <host> -p <port> -U <user> <db>`
- 恢复：`pg_restore -h <host> -p <port> -U <user> -d <db> -c <dump>.dump`
- 并行恢复（可选）：`pg_restore -j 4 ...`
- 恢复全局对象（需要时）：`psql -h <host> -p <port> -U <user> -f <dump>_globals.sql postgres`

详细步骤与注意事项参考关联文档“恢复指引”。

## 9. 故障处理

- 缺少命令：脚本报 `Missing command: ...`，按提示安装相应工具（pg_dump/ossutil64/rclone/sha256sum）
- 数据库认证失败：检查 `PG_HOST/PORT/USER/PGPASSWORD` 或 `.pgpass`；确认备份用户权限
- 无法连接数据库：确认网络与防火墙，尝试 `psql -h ... -U ... -d ... -c 'select 1'`
- 本地权限/空间：确认 `BACKUP_BASE_DIR/BACKUP_LOG_DIR` 存在且可写，磁盘空间充足
- OSS 上传失败：检查 `OSS_*`/`rclone` 配置、网络连通、端点地域；可先用 `BACKUP_MODE=local` 保本地落盘
- 报表状态 FAILED：查看 `/var/log/db-backup/backup_*.log` 中的 ERROR 具体原因，修复后重试

## 10. 安全与合规

- 不将 AK/SK、数据库密码写入仓库；配置文件设为 0600 权限
- 优先使用 `.pgpass` 管理数据库凭据；定期轮换 RAM 密钥
- 可在 OSS 设置生命周期与服务器端加密（SSE/KMS）；也可在本地上传前做 `gpg/age` 加密

## 11. 性能与容量建议

- 压缩等级：`-Z 9` 压缩率高但 CPU 开销较大；CPU 紧张时可调至 `-Z 6`
- 备份窗口：选择业务低峰期（如 02:00）；避免与大批量作业重叠
- 大库恢复：使用 `pg_restore -j N` 并行恢复（目录或自定义格式支持并行恢复）

## 12. 验收清单（上线必做）

- [ ] 手动跑通一次备份，报表 `status=SUCCESS`
- [ ] 验证远端 OSS 存在对应对象且大小合理
- [ ] 执行一次恢复演练至测试库，校验核心表与业务用例
- [ ] 配置并验证定时任务（cron/systemd timer）
- [ ] 平台能读取并展示报表（最近一次/列表）

## 13. 变更与回滚

- 变更记录：更新配置项需在变更单中记录（影响窗口、责任人、回退方案）
- 快速回滚：将 `BACKUP_MODE` 临时改为 `local`，先确保本地备份产出；网络/OSS 故障后再恢复 `both`
