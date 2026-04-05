# Split Us

MVP-система для ведения общего чека в Telegram и расчёта взаиморасчётов с минимальным числом переводов.

## Что В Репозитории

- `backend/` — Spring Boot backend, Telegram webhook, internal API и server-rendered admin
- `openapi/` — draft internal OpenAPI-контракта
- `infra/` — deployable stack для Prometheus, Grafana и reverse proxy
- `scripts/` — backup, restore и verification restore
- `docs/` — архитектура, user guides, operations и release-документация

## Текущее Состояние MVP

Сейчас в репозитории уже есть:

- Telegram-flow для `new_check`, `start`, `add_guest`, `add_expense`, `list_expenses`, `update_expense`, `delete_expense`, `settle`
- internal API с service-token защитой
- exact settlement с guard-ами от параллельного запуска и изменения данных во время расчёта
- admin-панель с login, read-only просмотром и удалением чека
- observability, Prometheus endpoint, Grafana dashboard provisioning и alert rules
- backup/restore scripts и runbook
- unit, scenario и web-acceptance tests

## Быстрый Старт

1. Скопировать `.env.example` в `.env`.
2. Заполнить минимум:
   - `POSTGRES_*`
   - `TELEGRAM_WEBHOOK_SECRET`
   - `TELEGRAM_BOT_USERNAME`
   - `INTERNAL_SERVICE_TOKEN`
   - `ADMIN_BOOTSTRAP_LOGIN`
   - один из `ADMIN_BOOTSTRAP_PASSWORD` или `ADMIN_BOOTSTRAP_PASSWORD_HASH`
3. Для production-like запуска указать:
   - `APP_DOMAIN`
   - `GRAFANA_DOMAIN`
   - `ACME_EMAIL`
4. Поднять стек:

```bash
docker compose up -d --build
```

5. Для локального backend-only прогона можно использовать:

```bash
mvn -f backend/pom.xml test
mvn -f backend/pom.xml spring-boot:run
```

## Основные Точки Входа

- backend через reverse proxy: `https://APP_DOMAIN/`
- admin: `https://APP_DOMAIN/admin`
- Grafana: `https://GRAFANA_DOMAIN/`
- Prometheus scrape endpoint внутри приложения: `/actuator/prometheus`

## Документация

- архитектура: `docs/architecture/`
- пользовательские инструкции: `docs/user/`
- operations: `docs/operations/`
- статус выполнения плана: `docs/release/implementation-status.md`
- приёмка релиза: `docs/release/acceptance-checklist.md`
- обзор всех документов: `docs/index.md`

## Проверка Качества

Основной локальный прогон:

```bash
mvn -f backend/pom.xml test
```

На текущем состоянии репозитория тестовый набор включает unit, scenario и web-acceptance coverage.
