#!/usr/bin/env sh

set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
ROOT_DIR=$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)
COMPOSE_FILE=${COMPOSE_FILE:-"$ROOT_DIR/docker-compose.yml"}
POSTGRES_SERVICE=${POSTGRES_SERVICE:-postgres}
POSTGRES_DB=${POSTGRES_DB:-splitus}
POSTGRES_USER=${POSTGRES_USER:-splitus}
BACKUP_DIR=${BACKUP_DIR:-"$ROOT_DIR/var/backups/postgres"}
BACKUP_RETENTION_DAYS=${BACKUP_RETENTION_DAYS:-7}
TIMESTAMP=$(date -u +"%Y%m%dT%H%M%SZ")
DUMP_BASENAME="splitus_${POSTGRES_DB}_${TIMESTAMP}.dump"
DUMP_FILE="$BACKUP_DIR/$DUMP_BASENAME"
TMP_DUMP_FILE="$DUMP_FILE.tmp"
CHECKSUM_FILE="$DUMP_FILE.sha256"

compose() {
    if command -v docker-compose >/dev/null 2>&1; then
        docker-compose -f "$COMPOSE_FILE" "$@"
        return
    fi
    docker compose -f "$COMPOSE_FILE" "$@"
}

cleanup() {
    rm -f "$TMP_DUMP_FILE"
}

trap cleanup EXIT INT TERM

mkdir -p "$BACKUP_DIR"

compose exec -T "$POSTGRES_SERVICE" \
    pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Fc > "$TMP_DUMP_FILE"

mv "$TMP_DUMP_FILE" "$DUMP_FILE"

if command -v sha256sum >/dev/null 2>&1; then
    (
        cd "$BACKUP_DIR"
        sha256sum "$DUMP_BASENAME" > "$(basename "$CHECKSUM_FILE")"
    )
elif command -v shasum >/dev/null 2>&1; then
    (
        cd "$BACKUP_DIR"
        shasum -a 256 "$DUMP_BASENAME" > "$(basename "$CHECKSUM_FILE")"
    )
fi

find "$BACKUP_DIR" -type f \( -name '*.dump' -o -name '*.sha256' \) -mtime +"$BACKUP_RETENTION_DAYS" -delete

echo "Backup created: $DUMP_FILE"
