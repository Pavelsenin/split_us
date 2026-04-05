# MVP Acceptance Checklist

## Purpose

This checklist packages the current MVP into a final acceptance pass against section `18` of the requirements.

## Telegram Core

- [ ] `/new_check` creates a check and returns a join link
- [ ] `/start join_<token>` adds a registered participant
- [ ] `/add_guest <token> <name>` adds a guest with a unique name
- [ ] a Telegram user without `username` is rejected with a clear error

## Expense Flow

- [ ] `/add_expense` creates a valid expense
- [ ] `/list_expenses` returns created expenses
- [ ] `/update_expense` updates only the author’s expense
- [ ] `/delete_expense` deletes only the author’s expense
- [ ] invalid edited expense messages move the expense to `REQUIRES_CLARIFICATION`
- [ ] corrected edited expense messages return the expense to `VALID`

## Settlement

- [ ] `/settle <token>` returns balances and transfers
- [ ] settlement ignores non-`VALID` expenses
- [ ] concurrent settlement requests return a conflict instead of running twice
- [ ] settlement returns a conflict if data changes during calculation

## Admin

- [ ] `/admin/login` authenticates with bootstrap admin credentials
- [ ] `/admin` shows search results for checks
- [ ] the check detail page shows participants, expenses and settlement snapshot
- [ ] delete requires exact title confirmation

## Operations

- [ ] `/actuator/prometheus` is reachable inside the deploy stack
- [ ] Prometheus scrapes the backend successfully
- [ ] Grafana starts with the provisioned datasource and dashboard
- [ ] the settlement p95 alert rule is loaded
- [ ] daily backup script creates a dump
- [ ] restore script restores a dump into a separate database
- [ ] verification restore completes successfully

## Release Gate

The MVP is ready for acceptance when:

1. `mvn test` is green
2. the deploy stack starts successfully with `docker compose up -d --build`
3. the checklist above is executed at least once against a production-like environment
