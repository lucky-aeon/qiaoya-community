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

# Health check
HEALTH_PATH=${HEALTH_PATH:-/actuator/health}
HEALTH_TIMEOUT=${HEALTH_TIMEOUT:-120}

# Switch method: manual | nginx
UPSTREAM_SWITCH=${UPSTREAM_SWITCH:-manual}
NGINX_CONF=${NGINX_CONF:-/etc/nginx/conf.d/qiaoya.conf}

# Optional limits and logging
CONTAINER_LIMITS=${CONTAINER_LIMITS:---memory=1g --cpus=1.5 --pids-limit=512}
LOG_OPTS=${LOG_OPTS:---log-opt max-size=50m --log-opt max-file=3}

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

# Optional CN registry login (private)
if [[ -n "${CN_REGISTRY:-}" && -n "${CN_USERNAME:-}" && -n "${CN_PASSWORD:-}" ]]; then
  echo "[bluegreen] Logging in CN registry $CN_REGISTRY as $CN_USERNAME"
  echo "$CN_PASSWORD" | docker login "$CN_REGISTRY" -u "$CN_USERNAME" --password-stdin || true
fi

echo "[bluegreen] Pulling image..."
docker pull "$IMAGE"

# Start green fresh
if docker ps -a --format '{{.Names}}' | grep -w "$GREEN_NAME" >/dev/null 2>&1; then
  echo "[bluegreen] Removing existing green..."
  docker rm -f "$GREEN_NAME" || true
fi

echo "[bluegreen] Starting green container..."
docker run -d --name "$GREEN_NAME" --restart unless-stopped \
  --env-file "$ENV_FILE" \
  -e SPRING_PROFILES_ACTIVE="$PROFILE" \
  -e JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS" \
  -p "${GREEN_PORT}:8520" \
  $CONTAINER_LIMITS $LOG_OPTS \
  "$IMAGE"

# Health check green
echo "[bluegreen] Waiting health $HEALTH_PATH on ${GREEN_PORT} (timeout ${HEALTH_TIMEOUT}s)..."
deadline=$((SECONDS + HEALTH_TIMEOUT))
ok=0
while (( SECONDS < deadline )); do
  # Try to fetch and detect UP; tolerate jq absence
  resp=$(curl -fsS "http://127.0.0.1:${GREEN_PORT}${HEALTH_PATH}" || true)
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
  echo "[bluegreen] Health check failed on green." >&2
  docker logs --tail=200 "$GREEN_NAME" || true
  exit 1
fi
echo "[bluegreen] Green is healthy."

if [[ "$UPSTREAM_SWITCH" == "nginx" ]]; then
  echo "[bluegreen] Switching Nginx upstream to GREEN ($GREEN_PORT) via $NGINX_CONF"
  if [[ ! -f "$NGINX_CONF" ]]; then
    echo "[bluegreen] Nginx conf not found: $NGINX_CONF" >&2
    exit 1
  fi
  ts=$(date +%F_%H%M%S)
  cp -a "$NGINX_CONF" "${NGINX_CONF}.bak.$ts"
  # Use 'down' to disable BLUE and enable GREEN (Nginx does not support weight=0)
  # 1) Remove any existing 'down' tokens on both server lines
  sed -E -i \
    -e "/127\.0\.0\.1:${BLUE_PORT}/ s/\bdown\b//g" \
    -e "/127\.0\.0\.1:${GREEN_PORT}/ s/\bdown\b//g" \
    "$NGINX_CONF"
  # 2) Mark BLUE as down so GREEN takes traffic
  sed -E -i \
    -e "/127\.0\.0\.1:${BLUE_PORT}/ s/;/ down;/" \
    "$NGINX_CONF"
  # Validate & reload
  if nginx -t; then
    nginx -s reload || systemctl reload nginx
  else
    echo "[bluegreen] nginx -t failed, restoring backup" >&2
    mv -f "${NGINX_CONF}.bak.$ts" "$NGINX_CONF"
    exit 1
  fi
  echo "[bluegreen] Switched traffic to GREEN via Nginx."
else
  echo "[bluegreen] Manual switch required. Update proxy to point 100% to ${GREEN_PORT} and reload."
fi

if [[ "${RETIRE_OLD:-}" == "1" ]]; then
  echo "[bluegreen] Retiring BLUE container ($BLUE_NAME)"
  docker rm -f "$BLUE_NAME" || true
else
  echo "[bluegreen] Keeping BLUE container for quick rollback (name=$BLUE_NAME, port=$BLUE_PORT)."
fi

echo "[bluegreen] Done. GREEN: $GREEN_NAME ($IMAGE) on $GREEN_PORT."
