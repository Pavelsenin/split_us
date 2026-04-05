#!/usr/bin/env sh

set -eu

if [ "$#" -lt 2 ] || [ "$#" -gt 3 ]; then
    echo "Usage: $0 <backup-file> <target-db> [--drop-existing]" >&2
    exit 1
fi

BACKUP_FILE=$1
TARGET_DB=$2
DROP_EXISTING=${3:-}
SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
ROOT_DIR=$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)
COMPOSE_FILE=${COMPOSE_FILE:-"$ROOT_DIR/docker-compose.yml"}
POSTGRES_SERVICE=${POSTGRES_SERVICE:-postgres}
POSTGRES_USER=${POSTGRES_USER:-splitus}
TMP_RESTORE_FILE="/tmp/splitus-restore.dump"

compose() {
    if command -v docker-compose >/dev/null 2>&1; then
        docker-compose -f "$COMPOSE_FILE" "$@"
        return
    fi
    docker compose -f "$COMPOSE_FILE" "$@"
}

psql_exec() {
    compose exec -T "$POSTGRES_SERVICE" \
        psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d postgres -c "$1"
}

if [ ! -f "$BACKUP_FILE" ]; then
    echo "Backup file not found: $BACKUP_FILE" >&2
    exit 1
fi

case "$TARGET_DB" in
    *[!A-Za-z0-9_]*|"")
        echo "Target database name must contain only letters, digits and underscore." >&2
        exit 1
        ;;
esac

DB_EXISTS=$(compose exec -T "$POSTGRES_SERVICE" \
    psql -tA -U "$POSTGRES_USER" -d postgres \
    -c "SELECT 1 FROM pg_database WHERE datname = '$TARGET_DB';")

if [ "$DB_EXISTS" = "1" ] && [ "$DROP_EXISTING" != "--drop-existing" ]; then
    echo "Database $TARGET_DB already exists. Pass --drop-existing to recreate it." >&2
    exit 1
fi

if [ "$DB_EXISTS" = "1" ]; then
    psql_exec "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '$TARGET_DB' AND pid <> pg_backend_pid();"
    psql_exec "DROP DATABASE \"$TARGET_DB\";"
fi

psql_exec "CREATE DATABASE \"$TARGET_DB\";"

cat "$BACKUP_FILE" | compose exec -T "$POSTGRES_SERVICE" sh -ec \
    "cat > '$TMP_RESTORE_FILE'; pg_restore -U '$POSTGRES_USER' -d '$TARGET_DB' --clean --if-exists '$TMP_RESTORE_FILE'; rm -f '$TMP_RESTORE_FILE'"

echo "Restore completed into database: $TARGET_DB"
