# Backup And Restore

## Purpose

This runbook closes the MVP operational requirement for daily PostgreSQL backups, 7-day retention and control restore verification.

## Scripts

- `scripts/backup-db.sh` creates a PostgreSQL custom-format dump through the `postgres` service from `docker-compose.yml`
- `scripts/restore-backup.sh` restores a chosen dump into a target database
- `scripts/verify-latest-backup.sh` restores the latest dump into a temporary database, checks core tables and drops the temporary database

## Environment

- `COMPOSE_FILE`: path to `docker-compose.yml`
- `POSTGRES_SERVICE`: compose service name, defaults to `postgres`
- `POSTGRES_DB`: source database for backup, defaults to `splitus`
- `POSTGRES_USER`: PostgreSQL user, defaults to `splitus`
- `BACKUP_DIR`: host directory for dump files, defaults to `./var/backups/postgres`
- `BACKUP_RETENTION_DAYS`: retention window in days, defaults to `7`

## Daily Backup

Run:

```bash
./scripts/backup-db.sh
```

Result:

- a new `*.dump` file appears in `BACKUP_DIR`
- a matching `*.sha256` checksum is created when `sha256sum` or `shasum` is available
- dumps and checksums older than `BACKUP_RETENTION_DAYS` are removed

## Restore

Restore a dump into a dedicated database:

```bash
./scripts/restore-backup.sh ./var/backups/postgres/splitus_splitus_20260405T080000Z.dump splitus_restore_check
```

If the target database already exists and should be recreated:

```bash
./scripts/restore-backup.sh ./var/backups/postgres/splitus_splitus_20260405T080000Z.dump splitus_restore_check --drop-existing
```

The script refuses unsafe database names and does not drop an existing database unless `--drop-existing` is passed explicitly.

## Verification Restore

Control restore for the latest backup:

```bash
./scripts/verify-latest-backup.sh
```

The script:

1. finds the newest `*.dump`
2. restores it into a temporary database
3. reads row counts from `check_book`, `participant` and `expense`
4. drops the temporary database even if verification fails partway through

This verification should be executed before production go-live and then regularly during operations.

## Scheduler Example

Example cron entry for a daily backup at `03:15` server time:

```cron
15 3 * * * cd /opt/split_us && ./scripts/backup-db.sh >> /var/log/splitus-backup.log 2>&1
```

Example weekly control restore on Sunday at `04:00`:

```cron
0 4 * * 0 cd /opt/split_us && ./scripts/verify-latest-backup.sh >> /var/log/splitus-backup-verify.log 2>&1
```

## Operational Notes

- keep backup files on a host-mounted volume outside the PostgreSQL data directory
- keep application and scheduler logs for at least 7 days together with backup artifacts
- use a dedicated restore database name and never restore directly over the production database
- if restore verification fails, treat the newest backup as untrusted until a successful verification is completed
