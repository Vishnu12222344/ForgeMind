#!/usr/bin/env bash
set -euo pipefail

IMAGE="${1:?Docker image required}"
COMPOSE_FILE="${2:-docker-compose.prod.yml}"

echo "Deploying image: $IMAGE"

export IMAGE_TAG="$IMAGE"

docker compose -f "$COMPOSE_FILE" pull backend
docker compose -f "$COMPOSE_FILE" up -d backend

echo "Waiting for health..."
./scripts/healthcheck.sh "http://localhost:8080/actuator/health"

echo "Deployment successful"