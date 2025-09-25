#!/usr/bin/env bash
set -euo pipefail

# Required
IMAGE=${IMAGE:-}

# Optional with defaults
PROFILE=${PROFILE:-dev}
ENV_FILE=${ENV_FILE:-/www/project/qiaoya/qiaoya.backend.env}
CONTAINER_NAME=${CONTAINER_NAME:-qiaoya-community-backend}
PORT=${PORT:-8520}
JAVA_TOOL_OPTIONS=${JAVA_TOOL_OPTIONS:-}

if [[ -z "$IMAGE" ]]; then
  echo "[deploy] IMAGE is required (e.g. ghcr.io/owner/repo:dev-latest)" >&2
  exit 1
fi

echo "[deploy] IMAGE=$IMAGE"
echo "[deploy] PROFILE=$PROFILE"
echo "[deploy] ENV_FILE=$ENV_FILE"
echo "[deploy] CONTAINER_NAME=$CONTAINER_NAME"
echo "[deploy] PORT=$PORT"

# Optional GHCR login for private images
if [[ -n "${GHCR_USERNAME:-}" && -n "${GHCR_TOKEN:-}" ]]; then
  echo "[deploy] Logging in to GHCR as $GHCR_USERNAME"
  echo "$GHCR_TOKEN" | docker login ghcr.io -u "$GHCR_USERNAME" --password-stdin || true
else
  echo "[deploy] Skipping GHCR login (credentials not provided)"
fi

# Pull latest image
echo "[deploy] Pulling image..."
docker pull "$IMAGE"

# Stop and remove existing container if any
if docker ps -a --format '{{.Names}}' | grep -w "$CONTAINER_NAME" >/dev/null 2>&1; then
  echo "[deploy] Stopping existing container..."
  docker stop "$CONTAINER_NAME" || true
  echo "[deploy] Removing existing container..."
  docker rm "$CONTAINER_NAME" || true
fi

# Run new container
echo "[deploy] Starting container..."
docker run -d --name "$CONTAINER_NAME" --restart unless-stopped \
  --env-file "$ENV_FILE" \
  -e SPRING_PROFILES_ACTIVE="$PROFILE" \
  -e JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS" \
  -p "${PORT}:8520" \
  "$IMAGE"

# Clean up old images to save space
docker image prune -f >/dev/null 2>&1 || true

echo "[deploy] Done. Container '$CONTAINER_NAME' is running on port $PORT"
