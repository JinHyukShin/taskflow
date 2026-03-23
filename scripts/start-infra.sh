#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "=== Starting StockPulse Infrastructure ==="
docker compose -f "$PROJECT_DIR/docker/docker-compose.yml" up -d postgres redis
echo "=== Infrastructure started ==="
echo "PostgreSQL: localhost:5432"
echo "Redis:      localhost:6379"
