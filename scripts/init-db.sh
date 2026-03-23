#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-stockpulse}"
DB_USER="${DB_USER:-stockpulse}"

echo "=== Initializing StockPulse Database ==="

echo "Applying V1__init.sql..."
PGPASSWORD="${DB_PASSWORD:-stockpulse}" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" \
    -f "$PROJECT_DIR/database/migration/V1__init.sql"

echo "Applying V2__initial_data.sql..."
PGPASSWORD="${DB_PASSWORD:-stockpulse}" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" \
    -f "$PROJECT_DIR/database/migration/V2__initial_data.sql"

echo "=== Database initialization complete ==="
