#!/usr/bin/env bash

set -euo pipefail

# Postgres logical backup with local/OSS storage and JSON report
# - Supports: local, oss, both
# - Tools: pg_dump/pg_dumpall, sha256sum, ossutil64 (or rclone)

# Usage:
#   Export envs OR provide an env file path via BACKUP_ENV_FILE
#   Then run: ./db_backup.sh

#############################
# Load configuration
#############################

if [[ -n "${BACKUP_ENV_FILE:-}" && -f "$BACKUP_ENV_FILE" ]]; then
  # shellcheck disable=SC1090
  source "$BACKUP_ENV_FILE"
fi

# Required PG
: "${PG_HOST:?PG_HOST is required}"
: "${PG_PORT:?PG_PORT is required}"
: "${PG_DATABASE:?PG_DATABASE is required}"
: "${PG_USER:?PG_USER is required}"
# PGPASSWORD should be provided in env (no prompt). Optional if .pgpass configured

# Backup mode: local | oss | both
BACKUP_MODE=${BACKUP_MODE:-both}

# Base directories
BACKUP_BASE_DIR=${BACKUP_BASE_DIR:-/data/db-backups}
BACKUP_DATA_DIR="$BACKUP_BASE_DIR/data"
BACKUP_REPORT_DIR="$BACKUP_BASE_DIR/reports"
BACKUP_LOG_DIR=${BACKUP_LOG_DIR:-/var/log/db-backup}
RETENTION_DAYS=${RETENTION_DAYS:-7}

# Upload settings
BACKUP_UPLOAD_TOOL=${BACKUP_UPLOAD_TOOL:-ossutil} # ossutil | rclone

# OSS settings (for ossutil)
OSS_BUCKET=${OSS_BUCKET:-}
OSS_PREFIX=${OSS_PREFIX:-postgres/backups}
OSS_ENDPOINT=${OSS_ENDPOINT:-}
OSS_ACCESS_KEY_ID=${OSS_ACCESS_KEY_ID:-}
OSS_ACCESS_KEY_SECRET=${OSS_ACCESS_KEY_SECRET:-}

# Rclone settings (if used)
RCLONE_REMOTE=${RCLONE_REMOTE:-oss}
RCLONE_PATH=${RCLONE_PATH:-postgres/backups}

# Optional: compress globals
INCLUDE_GLOBALS=${INCLUDE_GLOBALS:-true} # pg_dumpall --globals-only

# Optional: additional pg_dump options (e.g., --exclude-table-data)
PG_DUMP_EXTRA_OPTS=${PG_DUMP_EXTRA_OPTS:-}

mkdir -p "$BACKUP_DATA_DIR" "$BACKUP_REPORT_DIR" "$BACKUP_LOG_DIR"

LOG_FILE="$BACKUP_LOG_DIR/backup_$(date +%Y%m%d).log"
exec 3>>"$LOG_FILE"
log() {
  local level="$1"; shift
  local msg="$*"
  printf '%s [%s] %s\n' "$(date '+%Y-%m-%d %H:%M:%S')" "$level" "$msg" | tee >(sed 's/.*/&/' >&3)
}

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || { log ERROR "Missing command: $1"; exit 1; }
}

require_cmd pg_dump
require_cmd pg_restore
require_cmd pg_dumpall
require_cmd sha256sum || require_cmd shasum

if [[ "$BACKUP_MODE" =~ oss|both ]]; then
  if [[ "$BACKUP_UPLOAD_TOOL" == "ossutil" ]]; then
    require_cmd ossutil64 || require_cmd ossutil
    : "${OSS_BUCKET:?OSS_BUCKET is required for OSS upload}"
    : "${OSS_ENDPOINT:?OSS_ENDPOINT is required for OSS upload}"
    : "${OSS_ACCESS_KEY_ID:?OSS_ACCESS_KEY_ID is required for OSS upload}"
    : "${OSS_ACCESS_KEY_SECRET:?OSS_ACCESS_KEY_SECRET is required for OSS upload}"
  else
    require_cmd rclone
  fi
fi

START_TS=$(date -u +%Y-%m-%dT%H:%M:%SZ)
TS_TAG=$(date -u +%Y%m%dT%H%M%SZ)
HOST_TAG=$(hostname -s || echo host)

BACKUP_NAME="${PG_DATABASE}_${TS_TAG}_${HOST_TAG}"
BACKUP_FILE="$BACKUP_DATA_DIR/${BACKUP_NAME}.dump"
GLOBAL_FILE="$BACKUP_DATA_DIR/${BACKUP_NAME}_globals.sql"
CHECKSUM_FILE="$BACKUP_FILE.sha256"

STATUS="SUCCESS"
ERROR_MSG=""
LOCAL_SIZE=0
UPLOAD_URLS=()
TOOL_USED="$BACKUP_UPLOAD_TOOL"

log INFO "Starting backup: db=$PG_DATABASE mode=$BACKUP_MODE tool=$BACKUP_UPLOAD_TOOL"

BEGIN_EPOCH=$(date +%s)

set +e

# 1) Dump database (custom format, internal compression -Z 9)
pg_dump \
  -h "$PG_HOST" -p "$PG_PORT" -U "$PG_USER" -d "$PG_DATABASE" \
  -Fc -Z 9 $PG_DUMP_EXTRA_OPTS \
  -f "$BACKUP_FILE"
rc_dump=$?

if [[ $rc_dump -ne 0 ]]; then
  STATUS="FAILED"; ERROR_MSG="pg_dump failed with code $rc_dump"
else
  log INFO "pg_dump completed: $BACKUP_FILE"
fi

# 2) Dump globals (roles/privileges)
if [[ "${INCLUDE_GLOBALS}" == "true" && "$STATUS" == "SUCCESS" ]]; then
  pg_dumpall -h "$PG_HOST" -p "$PG_PORT" -U "$PG_USER" --globals-only > "$GLOBAL_FILE"
  rc_globals=$?
  if [[ $rc_globals -ne 0 ]]; then
    STATUS="FAILED"; ERROR_MSG="pg_dumpall(globals) failed with code $rc_globals"
  else
    log INFO "pg_dumpall globals completed: $GLOBAL_FILE"
  fi
fi

# 3) Checksum
if [[ "$STATUS" == "SUCCESS" ]]; then
  if command -v sha256sum >/dev/null 2>&1; then
    sha256sum "$BACKUP_FILE" > "$CHECKSUM_FILE"
  else
    shasum -a 256 "$BACKUP_FILE" > "$CHECKSUM_FILE"
  fi
  log INFO "Checksum generated: $CHECKSUM_FILE"

  # Size
  if [[ -f "$BACKUP_FILE" ]]; then
    LOCAL_SIZE=$(stat -f%z "$BACKUP_FILE" 2>/dev/null || stat -c%s "$BACKUP_FILE" 2>/dev/null || echo 0)
  fi
fi

# 4) Upload to OSS (if required)
if [[ "$STATUS" == "SUCCESS" && "$BACKUP_MODE" =~ oss|both ]]; then
  if [[ "$BACKUP_UPLOAD_TOOL" == "ossutil" ]]; then
    # Try ossutil64 first
    OSSUTIL_BIN=$(command -v ossutil64 || command -v ossutil)
    # upload main dump
    "$OSSUTIL_BIN" cp "$BACKUP_FILE" "oss://${OSS_BUCKET}/${OSS_PREFIX}/" \
      -e "$OSS_ENDPOINT" -i "$OSS_ACCESS_KEY_ID" -k "$OSS_ACCESS_KEY_SECRET" --update
    rc1=$?
    # upload globals (if present)
    rc2=0
    if [[ -f "$GLOBAL_FILE" ]]; then
      "$OSSUTIL_BIN" cp "$GLOBAL_FILE" "oss://${OSS_BUCKET}/${OSS_PREFIX}/" \
        -e "$OSS_ENDPOINT" -i "$OSS_ACCESS_KEY_ID" -k "$OSS_ACCESS_KEY_SECRET" --update
      rc2=$?
    fi
    if [[ $rc1 -ne 0 || $rc2 -ne 0 ]]; then
      STATUS="FAILED"; ERROR_MSG="ossutil upload failed";
    else
      UPLOAD_URLS+=("oss://${OSS_BUCKET}/${OSS_PREFIX}/$(basename "$BACKUP_FILE")")
      [[ -f "$GLOBAL_FILE" ]] && UPLOAD_URLS+=("oss://${OSS_BUCKET}/${OSS_PREFIX}/$(basename "$GLOBAL_FILE")")
      log INFO "Uploaded to OSS: ${UPLOAD_URLS[*]}"
    fi
  else
    # rclone
    require_cmd rclone
    rclone copy "$BACKUP_FILE" "${RCLONE_REMOTE}:${RCLONE_PATH}/"
    rc1=$?
    rc2=0
    if [[ -f "$GLOBAL_FILE" ]]; then
      rclone copy "$GLOBAL_FILE" "${RCLONE_REMOTE}:${RCLONE_PATH}/"
      rc2=$?
    fi
    if [[ $rc1 -ne 0 || $rc2 -ne 0 ]]; then
      STATUS="FAILED"; ERROR_MSG="rclone upload failed"
    else
      UPLOAD_URLS+=("${RCLONE_REMOTE}:${RCLONE_PATH}/$(basename "$BACKUP_FILE")")
      [[ -f "$GLOBAL_FILE" ]] && UPLOAD_URLS+=("${RCLONE_REMOTE}:${RCLONE_PATH}/$(basename "$GLOBAL_FILE")")
      log INFO "Uploaded via rclone: ${UPLOAD_URLS[*]}"
    fi
  fi
fi

# 5) Retention clean (local)
if [[ "$STATUS" == "SUCCESS" && "$BACKUP_MODE" =~ local|both ]]; then
  find "$BACKUP_DATA_DIR" -type f -name "*.dump" -mtime +"$RETENTION_DAYS" -print -delete | tee >(sed 's/.*/&/' >&3) || true
  find "$BACKUP_DATA_DIR" -type f -name "*.sql" -mtime +"$RETENTION_DAYS" -print -delete | tee >(sed 's/.*/&/' >&3) || true
  find "$BACKUP_DATA_DIR" -type f -name "*.sha256" -mtime +"$RETENTION_DAYS" -print -delete | tee >(sed 's/.*/&/' >&3) || true
fi

END_EPOCH=$(date +%s)
DURATION_SEC=$((END_EPOCH - BEGIN_EPOCH))
END_TS=$(date -u +%Y-%m-%dT%H:%M:%SZ)

# 6) Write JSON report
CHECKSUM=$(awk '{print $1}' "$CHECKSUM_FILE" 2>/dev/null || echo "")
REPORT_FILE="$BACKUP_REPORT_DIR/${BACKUP_NAME}.json"

cat > "$REPORT_FILE" <<EOF
{
  "database": "${PG_DATABASE}",
  "host": "${PG_HOST}",
  "port": "${PG_PORT}",
  "startedAt": "${START_TS}",
  "finishedAt": "${END_TS}",
  "durationSeconds": ${DURATION_SEC},
  "mode": "${BACKUP_MODE}",
  "tool": "${TOOL_USED}",
  "status": "${STATUS}",
  "errorMessage": "${ERROR_MSG}",
  "local": {
    "file": "${BACKUP_FILE}",
    "globalsFile": "${GLOBAL_FILE}",
    "checksumSha256": "${CHECKSUM}",
    "sizeBytes": ${LOCAL_SIZE}
  },
  "remote": {
    "urls": [$(printf '"%s",' "${UPLOAD_URLS[@]}" | sed 's/,$//')]
  }
}
EOF

if [[ "$STATUS" == "SUCCESS" ]]; then
  log INFO "Backup finished successfully in ${DURATION_SEC}s (report: $REPORT_FILE)"
  exit 0
else
  log ERROR "Backup failed: ${ERROR_MSG} (report: $REPORT_FILE)"
  exit 1
fi
