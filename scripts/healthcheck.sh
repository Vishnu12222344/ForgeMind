#!/usr/bin/env bash
set -euo pipefail

URL="${1:-https://forgemind-l98k.onrender.com/actuator/health}"
MAX_ATTEMPTS=30
SLEEP_SECONDS=10

echo "Checking health endpoint: $URL"

for ((i=1; i<=MAX_ATTEMPTS; i++)); do
  STATUS_CODE=$(curl -s -o /tmp/health.json -w "%{http_code}" "$URL" || true)

  if [[ "$STATUS_CODE" == "200" ]]; then
    echo "Health check passed on attempt $i"
    cat /tmp/health.json
    exit 0
  fi

  echo "Attempt $i/$MAX_ATTEMPTS failed with status: $STATUS_CODE"
  sleep "$SLEEP_SECONDS"
done

echo "Health check failed after $MAX_ATTEMPTS attempts"
exit 1