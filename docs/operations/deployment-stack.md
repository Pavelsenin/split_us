# Deployment Stack

## Purpose

This document describes the deployable infrastructure artifacts used by the current MVP delivery flow.

## Stack

- `backend` is built from `backend/Dockerfile`
- `postgres` stores operational data
- `prometheus` scrapes `backend:8080/actuator/prometheus`
- `grafana` starts with a provisioned Prometheus datasource and dashboard
- `nginx` terminates HTTPS and proxies the application and Grafana

## Compose Services

`docker-compose.yml` defines:

- `backend`
- `postgres`
- `prometheus`
- `grafana`
- `nginx`

Routing:

- `APP_DOMAIN` -> SplitUs backend and `/admin`
- `GRAFANA_DOMAIN` -> Grafana UI

## Required Environment

Add these values to `.env` before a production-like launch:

- `APP_DOMAIN`
- `GRAFANA_DOMAIN`
- `NGINX_APP_CERTIFICATE`
- `NGINX_APP_CERTIFICATE_KEY`
- `NGINX_GRAFANA_CERTIFICATE`
- `NGINX_GRAFANA_CERTIFICATE_KEY`
- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `TELEGRAM_WEBHOOK_SECRET`
- `TELEGRAM_BOT_USERNAME`
- `INTERNAL_SERVICE_TOKEN`
- `ADMIN_BOOTSTRAP_LOGIN`
- one of `ADMIN_BOOTSTRAP_PASSWORD` or `ADMIN_BOOTSTRAP_PASSWORD_HASH`
- `GRAFANA_ADMIN_USER`
- `GRAFANA_ADMIN_PASSWORD`

## Required Certificate Files

Nginx expects certificate files in:

- `infra/nginx/certs/`

Default filenames from `.env.example`:

- `app.crt`
- `app.key`
- `grafana.crt`
- `grafana.key`

If you use a wildcard or SAN certificate, both application and Grafana variables may point to the same certificate and key filenames.

## Bring-Up

Before launch:

1. place certificate files into `infra/nginx/certs/`
2. fill `.env`
3. run:

```bash
docker compose up -d --build
```

Expected endpoints:

- application: `https://APP_DOMAIN/`
- admin: `https://APP_DOMAIN/admin`
- Grafana: `https://GRAFANA_DOMAIN/`
- Prometheus inside compose: `http://prometheus:9090`

## Provisioned Monitoring

Provisioning files live in:

- `infra/prometheus/prometheus.yml`
- `infra/prometheus/alerts.yml`
- `infra/grafana/provisioning/datasources/prometheus.yml`
- `infra/grafana/provisioning/dashboards/dashboards.yml`
- `infra/grafana/dashboards/splitus-overview.json`

The default dashboard exposes:

- settlement request rate by outcome
- settlement conflict rate by reason
- settlement p95 duration
- telegram command throughput
- telegram update throughput
- admin delete activity

The baseline alert rule exposes:

- settlement p95 above 1 second for 5 minutes

## HTTPS Notes

- Nginx in this stack does not issue certificates automatically
- certificate provisioning is expected to happen outside the container stack
- Telegram webhook registration should point to `https://APP_DOMAIN/api/telegram/webhook/<alias>`
- PostgreSQL, Prometheus and Grafana host ports are bound to `127.0.0.1`, while public ingress is expected to go through `nginx`
