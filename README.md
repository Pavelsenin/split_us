# Split Us

Репозиторий MVP-системы для ведения общего чека в Telegram и расчета взаиморасчетов.

## Структура

- `backend/` — Java backend, Telegram webhook, внутренний API и server-rendered админ-интерфейс.
- `docs/architecture/` — архитектурный baseline, схема хранения, Telegram-сценарии и spike по алгоритму расчета.
- `openapi/` — draft внутреннего OpenAPI-контракта.
- `docker-compose.yml` — локальное dev-окружение для PostgreSQL и Grafana.

## Быстрый старт

1. Скопировать `.env.example` в `.env` и при необходимости скорректировать значения.
2. Поднять инфраструктуру: `docker compose up -d`.
3. Запустить backend: `mvn -f backend/pom.xml spring-boot:run`.

## Статус

Сделан стартовый baseline по этапам 0-1 плана: архитектурные решения, каркас backend, baseline-миграция, draft OpenAPI и spike точного расчета.

