#!/usr/bin/env bash
set -euo pipefail

PREVIOUS_TAG="${1:?Previous image tag required}"

echo "Rolling back to: $PREVIOUS_TAG"

export IMAGE_TAG="$PREVIOUS_TAG"

docker compose -f docker-compose.prod.yml pull backend
docker compose -f docker-compose.prod.yml up -d backend

./scripts/healthcheck.sh "http://localhost:8080/actuator/health"

echo "Rollback successful"