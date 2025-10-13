#!/usr/bin/env bash
set -euo pipefail

# Blue-Green deployment script for Qiaoya backend (Docker + Nginx optional)
# Usage (manual run on server):
#   IMAGE=registry.cn-xx.aliyuncs.com/ns/qiaoya:v1.2.3 \
#   PROFILE=prod ENV_FILE=/etc/qiaoya.backend.prod.env \
#   UPSTREAM_SWITCH=nginx NGINX_CONF=/etc/nginx/conf.d/qiaoya.conf \
#   /www/project/qiaoya/deploy-qiaoya-bluegreen.sh
#
# This script will:
#  1) pull image  2) start green on port 8521  3) health check
#  4) (optional) switch Nginx upstream to green  5) keep blue for rollback
# Set RETIRE_OLD=1 to stop/remove blue after cutover.

# Required
IMAGE=${IMAGE:-}

# Optional
PROFILE=${PROFILE:-prod}
ENV_FILE=${ENV_FILE:-/etc/qiaoya.backend.prod.env}
JAVA_TOOL_OPTIONS=${JAVA_TOOL_OPTIONS:-}

# Naming and ports
BASE_NAME=${CONTAINER_BASE_NAME:-qiaoya-community-backend}
BLUE_NAME=${BLUE_NAME:-${BASE_NAME}-blue}
GREEN_NAME=${GREEN_NAME:-${BASE_NAME}-green}
BLUE_PORT=${BLUE_PORT:-8520}
GREEN_PORT=${GREEN_PORT:-8521}

# Health check (hardcoded to public health endpoint)
HEALTH_PATH=${HEALTH_PATH:-/api/public/health}
HEALTH_TIMEOUT=${HEALTH_TIMEOUT:-120}

# Switch method: manual | nginx
UPSTREAM_SWITCH=${UPSTREAM_SWITCH:-manual}
NGINX_CONF=${NGINX_CONF:-/etc/nginx/conf.d/qiaoya.conf}

# Target side for new release: blue | green | auto
# auto: detect current active from Nginx (down flag) and choose the idle one
TARGET_SIDE=${TARGET_SIDE:-auto}

# Optional limits and logging
CONTAINER_LIMITS=${CONTAINER_LIMITS:---memory=1g --cpus=1.5 --pids-limit=512}
# Default to json-file with 30MB rolling buffer (10m x 3) so Promtail can read and disk risk is controlled
LOG_OPTS=${LOG_OPTS:---log-driver json-file --log-opt max-size=10m --log-opt max-file=3}

# Ensure log stack (Loki/Promtail/Grafana) is up, unless explicitly disabled
# WITH_LOGS=0 to skip; LOG_STACK_COMPOSE to override compose path; COMPOSE_BIN to force binary
WITH_LOGS=${WITH_LOGS:-1}
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
# 默认优先使用：脚本同级目录下的 loki/docker-compose.loki.yml（便于服务器仅拷部署资产）
REPO_ROOT=$(cd "$SCRIPT_DIR/../.." && pwd)
_default_compose="$SCRIPT_DIR/loki/docker-compose.loki.yml"
if [[ ! -f "$_default_compose" ]]; then
  _default_compose="$REPO_ROOT/deploy/observability/loki/docker-compose.loki.yml"
fi
LOG_STACK_COMPOSE=${LOG_STACK_COMPOSE:-$_default_compose}
if [[ "$WITH_LOGS" == "1" ]]; then
  if [[ -f "$LOG_STACK_COMPOSE" ]]; then
    echo "[bluegreen] Ensuring log stack is up via $LOG_STACK_COMPOSE"
    if docker compose version >/dev/null 2>&1; then
      docker compose -f "$LOG_STACK_COMPOSE" up -d || true
    elif command -v docker-compose >/dev/null 2>&1; then
      docker-compose -f "$LOG_STACK_COMPOSE" up -d || true
    else
      echo "[bluegreen] docker compose not available; skipping log stack bring-up" >&2
    fi
  else
    echo "[bluegreen] Log stack compose not found: $LOG_STACK_COMPOSE (set WITH_LOGS=0 to skip)" >&2
  fi
fi

if [[ -z "$IMAGE" ]]; then
  echo "[bluegreen] IMAGE is required" >&2
  exit 1
fi

if [[ ! -f "$ENV_FILE" ]]; then
  echo "[bluegreen] ENV_FILE not found: $ENV_FILE" >&2
  exit 1
fi

echo "[bluegreen] IMAGE=$IMAGE"
echo "[bluegreen] PROFILE=$PROFILE"
echo "[bluegreen] ENV_FILE=$ENV_FILE"
echo "[bluegreen] BLUE_NAME=$BLUE_NAME (port $BLUE_PORT)"
echo "[bluegreen] GREEN_NAME=$GREEN_NAME (port $GREEN_PORT)"
echo "[bluegreen] SWITCH=$UPSTREAM_SWITCH"
echo "[bluegreen] TARGET_SIDE=$TARGET_SIDE"

# Optional CN registry login (private)
if [[ -n "${CN_REGISTRY:-}" && -n "${CN_USERNAME:-}" && -n "${CN_PASSWORD:-}" ]]; then
  echo "[bluegreen] Logging in CN registry $CN_REGISTRY as $CN_USERNAME"
  echo "$CN_PASSWORD" | docker login "$CN_REGISTRY" -u "$CN_USERNAME" --password-stdin || true
fi

echo "[bluegreen] Pulling image..."
docker pull "$IMAGE"

# Decide target side (auto picks the idle one from Nginx upstream)
active_side="unknown"
if [[ "$UPSTREAM_SWITCH" == "nginx" && -f "$NGINX_CONF" ]]; then
  blue_line=$(grep -E "127\\.0\\.0\\.1:${BLUE_PORT}[^;]*;" "$NGINX_CONF" || true)
  green_line=$(grep -E "127\\.0\\.0\\.1:${GREEN_PORT}[^;]*;" "$NGINX_CONF" || true)
  if echo "$blue_line" | grep -q '\bdown\b' && ! echo "$green_line" | grep -q '\bdown\b'; then
    active_side="green"
  elif echo "$green_line" | grep -q '\bdown\b' && ! echo "$blue_line" | grep -q '\bdown\b'; then
    active_side="blue"
  fi
fi

if [[ "$TARGET_SIDE" == "auto" ]]; then
  if [[ "$active_side" == "green" ]]; then
    TARGET_SIDE="blue"
  elif [[ "$active_side" == "blue" ]]; then
    TARGET_SIDE="green"
  else
    TARGET_SIDE="green"
  fi
fi

if [[ "$TARGET_SIDE" == "blue" ]]; then
  TARGET_NAME="$BLUE_NAME"; TARGET_PORT="$BLUE_PORT"; OPP_NAME="$GREEN_NAME"; OPP_PORT="$GREEN_PORT"
else
  TARGET_NAME="$GREEN_NAME"; TARGET_PORT="$GREEN_PORT"; OPP_NAME="$BLUE_NAME"; OPP_PORT="$BLUE_PORT"
fi

# Start target fresh
if docker ps -a --format '{{.Names}}' | grep -w "$TARGET_NAME" >/dev/null 2>&1; then
  echo "[bluegreen] Removing existing $TARGET_NAME..."
  docker rm -f "$TARGET_NAME" || true
fi

echo "[bluegreen] Starting $TARGET_NAME on port $TARGET_PORT..."
docker run -d --name "$TARGET_NAME" --restart unless-stopped \
  --env-file "$ENV_FILE" \
  -e SPRING_PROFILES_ACTIVE="$PROFILE" \
  -e JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS" \
  -p "${TARGET_PORT}:8520" \
  $CONTAINER_LIMITS $LOG_OPTS \
  "$IMAGE"

# Health check target
echo "[bluegreen] Waiting health $HEALTH_PATH on ${TARGET_PORT} (timeout ${HEALTH_TIMEOUT}s)..."
deadline=$((SECONDS + HEALTH_TIMEOUT))
ok=0
while (( SECONDS < deadline )); do
  # Try to fetch and detect UP; tolerate jq absence
  resp=$(curl -fsS "http://127.0.0.1:${TARGET_PORT}${HEALTH_PATH}" || true)
  code=$?
  if [[ $code -eq 0 ]]; then
    if command -v jq >/dev/null 2>&1; then
      status=$(echo "$resp" | jq -r '.data.status // .status // empty')
      if [[ "$status" == "UP" ]]; then ok=1; break; fi
    else
      # fallback: any 200 response is acceptable
      ok=1; break
    fi
  fi
  sleep 2
done

if [[ "$ok" != "1" ]]; then
  echo "[bluegreen] Health check failed on $TARGET_NAME." >&2
  docker logs --tail=200 "$TARGET_NAME" || true
  exit 1
fi
echo "[bluegreen] $TARGET_NAME is healthy."

if [[ "$UPSTREAM_SWITCH" == "nginx" ]]; then
  echo "[bluegreen] Switching Nginx upstream to $TARGET_SIDE ($TARGET_PORT) via $NGINX_CONF"
  if [[ ! -f "$NGINX_CONF" ]]; then
    echo "[bluegreen] Nginx conf not found: $NGINX_CONF" >&2
    exit 1
  fi
  ts=$(date +%F_%H%M%S)
  cp -a "$NGINX_CONF" "${NGINX_CONF}.bak.$ts"
  # Use 'down' to disable the opposite side and enable target side
  # 1) Remove any existing 'down' tokens on both server lines
  sed -E -i \
    -e "/127\.0\.0\.1:${BLUE_PORT}/ s/\bdown\b//g" \
    -e "/127\.0\.0\.1:${GREEN_PORT}/ s/\bdown\b//g" \
    "$NGINX_CONF"
  # 2) Disable opposite side
  if [[ "$TARGET_SIDE" == "blue" ]]; then
    sed -E -i -e "/127\.0\.0\.1:${GREEN_PORT}/ s/;/ down;/" "$NGINX_CONF"
  else
    sed -E -i -e "/127\.0\.0\.1:${BLUE_PORT}/ s/;/ down;/" "$NGINX_CONF"
  fi
  # Validate & reload
  if nginx -t; then
    nginx -s reload || systemctl reload nginx
  else
    echo "[bluegreen] nginx -t failed, restoring backup" >&2
    mv -f "${NGINX_CONF}.bak.$ts" "$NGINX_CONF"
    exit 1
  fi
  echo "[bluegreen] Switched traffic to $TARGET_SIDE via Nginx."
else
  echo "[bluegreen] Manual switch required. Update proxy to point 100% to ${TARGET_PORT} and reload."
fi

if [[ "${RETIRE_OLD:-}" == "1" ]]; then
  echo "[bluegreen] Retiring opposite container ($OPP_NAME)"
  docker rm -f "$OPP_NAME" || true
else
  echo "[bluegreen] Keeping opposite container for quick rollback (name=$OPP_NAME, port=$OPP_PORT)."
fi

# Clean up images: keep only the most recent N for this repository
# Default N: prod=2 (override via KEEP_N_IMAGES). For other profiles, default to 1.
IMAGE_NO_DIGEST="${IMAGE%%@*}"
_after_slash="${IMAGE_NO_DIGEST##*/}"
if [[ "${_after_slash}" == *:* ]]; then
  IMAGE_REPO="${IMAGE_NO_DIGEST%:*}"
else
  IMAGE_REPO="${IMAGE_NO_DIGEST}"
fi

if [[ -n "${KEEP_N_IMAGES:-}" ]]; then
  KEEP_N="${KEEP_N_IMAGES}"
else
  case "${PROFILE}" in
    prod|production) KEEP_N=2 ;;
    *) KEEP_N=1 ;;
  esac
fi

echo "[bluegreen] Keeping last ${KEEP_N} images for repo: ${IMAGE_REPO}"

KEEP_SCRIPT="$(dirname "$0")/../../ops/docker/keep-last-n-images.sh"
if [[ -x "$KEEP_SCRIPT" ]]; then
  PRUNE_DANGLING=1 BUILDER_PRUNE_HOURS="${BUILDER_PRUNE_HOURS:-168}" \
    "$KEEP_SCRIPT" "$IMAGE_REPO" "$KEEP_N" || true
else
  # Fallback inline cleanup (safe: won't remove images in use)
  mapfile -t _tags < <(docker images "$IMAGE_REPO" --format '{{.Repository}}:{{.Tag}}' | grep -v '<none>' || true)
  if (( ${#_tags[@]} > KEEP_N )); then
    for img in "${_tags[@]:${KEEP_N}}"; do
      docker rmi "$img" || true
    done
  fi
  docker image prune -f >/dev/null 2>&1 || true
  if [[ -n "${BUILDER_PRUNE_HOURS:-}" ]] && [[ "${BUILDER_PRUNE_HOURS}" =~ ^[0-9]+$ ]]; then
    docker builder prune -af --filter "until=${BUILDER_PRUNE_HOURS}h" >/dev/null 2>&1 || true
  fi
fi

echo "[bluegreen] Done. Target: $TARGET_NAME ($IMAGE) on $TARGET_PORT."
