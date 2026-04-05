#!/usr/bin/env sh

set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
ROOT_DIR=$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)
COMPOSE_FILE=${COMPOSE_FILE:-"$ROOT_DIR/docker-compose.yml"}
POSTGRES_SERVICE=${POSTGRES_SERVICE:-postgres}
POSTGRES_USER=${POSTGRES_USER:-splitus}
BACKUP_DIR=${BACKUP_DIR:-"$ROOT_DIR/var/backups/postgres"}
RESTORE_DB="splitus_restore_check_$(date -u +"%Y%m%d%H%M%S")"

compose() {
    if command -v docker-compose >/dev/null 2>&1; then
        docker-compose -f "$COMPOSE_FILE" "$@"
        return
    fi
    docker compose -f "$COMPOSE_FILE" "$@"
}

cleanup() {
    compose exec -T "$POSTGRES_SERVICE" \
        psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d postgres \
        -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '$RESTORE_DB' AND pid <> pg_backend_pid();" >/dev/null 2>&1 || true
    compose exec -T "$POSTGRES_SERVICE" \
        psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d postgres \
        -c "DROP DATABASE \"$RESTORE_DB\";" >/dev/null 2>&1 || true
}

trap cleanup EXIT INT TERM

LATEST_BACKUP=$(find "$BACKUP_DIR" -maxdepth 1 -type f -name '*.dump' | sort | tail -n 1)

if [ -z "$LATEST_BACKUP" ]; then
    echo "No backup files found in $BACKUP_DIR" >&2
    exit 1
fi

"$SCRIPT_DIR/restore-backup.sh" "$LATEST_BACKUP" "$RESTORE_DB"

CHECKS_COUNT=$(compose exec -T "$POSTGRES_SERVICE" \
    psql -tA -U "$POSTGRES_USER" -d "$RESTORE_DB" -c "SELECT count(*) FROM check_book;")
PARTICIPANTS_COUNT=$(compose exec -T "$POSTGRES_SERVICE" \
    psql -tA -U "$POSTGRES_USER" -d "$RESTORE_DB" -c "SELECT count(*) FROM participant;")
EXPENSES_COUNT=$(compose exec -T "$POSTGRES_SERVICE" \
    psql -tA -U "$POSTGRES_USER" -d "$RESTORE_DB" -c "SELECT count(*) FROM expense;")

compose exec -T "$POSTGRES_SERVICE" \
    psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d postgres \
    -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '$RESTORE_DB' AND pid <> pg_backend_pid();"
compose exec -T "$POSTGRES_SERVICE" \
    psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d postgres \
    -c "DROP DATABASE \"$RESTORE_DB\";"

trap - EXIT INT TERM

echo "Verified backup: $LATEST_BACKUP"
echo "Restored rows: check_book=$CHECKS_COUNT participant=$PARTICIPANTS_COUNT expense=$EXPENSES_COUNT"
