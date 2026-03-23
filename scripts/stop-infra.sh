#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "=== Stopping StockPulse Infrastructure ==="
docker compose -f "$PROJECT_DIR/docker/docker-compose.yml" down
echo "=== Infrastructure stopped ==="
