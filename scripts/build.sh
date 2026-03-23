#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "=== Building StockPulse ==="
cd "$PROJECT_DIR"
./gradlew clean build -x test
echo "=== Build complete ==="
