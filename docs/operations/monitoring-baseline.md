# Monitoring Baseline

## What Is Exposed

- Prometheus scrape endpoint: `/actuator/prometheus`
- Prometheus scrape config: `infra/prometheus/prometheus.yml`
- Prometheus alert rules: `infra/prometheus/alerts.yml`
- Provisioned Grafana dashboard: `infra/grafana/dashboards/splitus-overview.json`
- Core custom metrics:
  - `splitus.settlement.requests.total`
  - `splitus.settlement.conflicts.total`
  - `splitus.settlement.duration`
  - `splitus.telegram.updates.total`
  - `splitus.telegram.commands.total`
  - `splitus.telegram.command.duration`
  - `splitus.admin.check.delete.total`

## Recommended Dashboard Panels

- Settlement request rate by outcome
- Settlement conflict rate split by `already_running` and `state_changed`
- Settlement p95 duration over 5m
- Telegram command throughput by command and outcome
- Telegram command p95 duration for `add_expense`, `update_expense`, `settle`
- Admin check delete rate by outcome

## Recommended Alert

- Trigger when settlement p95 duration is above 1 second for 5 minutes
- Prometheus expression example:

```promql
histogram_quantile(
  0.95,
  sum by (le) (
    rate(splitus_settlement_duration_seconds_bucket{outcome="success"}[5m])
  )
) > 1
```

## Logging Baseline

- `SettlementExecutionService` logs successful runs, lock conflicts and state-change conflicts
- `TelegramCommandService` logs command successes and validation failures with chat/user context
- `AdminCheckCommandService` logs confirmed check deletion and rejected confirmation attempts
