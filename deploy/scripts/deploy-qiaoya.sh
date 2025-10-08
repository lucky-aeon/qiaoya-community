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

# Clean up images: keep only the most recent N for this repository
# Default N: dev=1, prod=2 (override via KEEP_N_IMAGES)
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

echo "[deploy] Keeping last ${KEEP_N} images for repo: ${IMAGE_REPO}"

KEEP_SCRIPT="$(dirname "$0")/../../ops/docker/keep-last-n-images.sh"
if [[ -x "$KEEP_SCRIPT" ]]; then
  PRUNE_DANGLING=1 BUILDER_PRUNE_HOURS="${BUILDER_PRUNE_HOURS:-168}" \
    "$KEEP_SCRIPT" "$IMAGE_REPO" "$KEEP_N" || true
else
  # Fallback inline cleanup
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

echo "[deploy] Done. Container '$CONTAINER_NAME' is running on port $PORT"
