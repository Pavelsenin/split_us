# Статус Выполнения Плана

## Назначение

Этот документ фиксирует фактическое состояние репозитория относительно [implementation_plan.md](/D:/Dev/projects/codex/split_us/implementation_plan.md) и отделяет завершённые работы от функциональных пробелов и шагов, которые ещё требуют ручной проверки в production-like окружении.

Статус актуален для состояния репозитория на `2026-04-05`.

## Сводка По Этапам

| Этап | Статус | Комментарий |
| --- | --- | --- |
| 0. Проектирование | Done | Архитектурный baseline, доменная модель, Telegram-сценарии, spike по settlement и draft OpenAPI есть в репозитории. |
| 1. Базовый каркас | Done | Spring Boot backend, миграции, webhook endpoint, health endpoints, docker compose и env-конфигурация собраны. |
| 2. Домен и internal API | Done | `check`, `participant`, `expense`, `expense_share`, `participant_merge`, service-token auth, ошибки и internal API реализованы и покрыты тестами. |
| 3. Telegram core | Partial | Работают `new_check`, `start`, `add_guest`, merge, лимиты и проверка `username`, но нет автоматического создания чата, pin стартового сообщения и rename guest. |
| 4. Expense flow | Partial | Работает структурированный command-flow, edited-message sync и clarification, но нет menu/form flow, free-text parsing и реакции на удаление исходного Telegram-сообщения. |
| 5. Settlement engine | Partial | Exact settlement, guarded execution и метрики есть, но не реализована delayed publication для долгого расчёта. |
| 6. Admin panel | Partial | Есть Spring Security login, поиск, detail view и delete flow, но нет закрытия/деактивации Telegram-чата и встроенных ссылок на monitoring. |
| 7. Ops / observability / backup | Partial | Метрики, Grafana, alerting, HTTPS proxy и backup scripts оформлены, но контрольный restore и deploy stack не проверены в этом окружении. |
| 8. Тестирование и приёмка | Partial | Unit, scenario и web-acceptance покрытие есть, user docs и deployment docs есть, но OpenAPI всё ещё помечен как draft и full acceptance run в production-like среде ещё не зафиксирован. |

## Что Уже Закрыто

- Базовый Telegram lifecycle: создание чека, join по `deep link`, добавление гостя, merge guest -> registered.
- Expense lifecycle: create, list, update, delete, edited-message resync, `REQUIRES_CLARIFICATION`, возврат в `VALID` после исправления.
- Точный детерминированный settlement с защитой от параллельного запуска и изменения данных во время расчёта.
- Internal REST API с `X-Service-Token`, машиночитаемыми ошибками и OpenAPI-описанием.
- Admin web UI с Spring Security auth, поиском чеков, detail view и destructive delete через подтверждение.
- Observability контур: `/actuator/prometheus`, Prometheus scrape, Grafana provisioning, settlement alert rules, структурные логи.
- Backup/restore контур: скрипты, retention 7 дней и runbook.
- Документация для пользователя, operations и release-checklist.

## Функциональные Пробелы Относительно Плана

- Не реализовано автоматическое создание Telegram-чата там, где это возможно; текущая реализация опирается на `deep link` fallback.
- Не реализованы публикация и pin стартового сообщения в групповом чате.
- Не реализовано переименование guest-участника.
- Не реализованы menu/form flow и разбор свободного текстового сообщения для расходов; сейчас поддерживается только структурированная команда.
- Не реализовано автоматическое удаление или деактивация расхода при удалении связанного Telegram-сообщения.
- Не реализована delayed publication результата settlement для долгого расчёта.
- В admin-панели нет операции закрытия/деактивации связанного Telegram-чата.
- В admin-панели нет прямых ссылок или embedded-view для monitoring.
- OpenAPI остаётся в статусе draft и ещё не доведён до финального release-контракта.

## Ручная Проверка Ещё Нужна

- `docker compose up -d --build` для полного deploy stack не прогнан в текущем окружении, потому что `docker` здесь недоступен.
- Backup scripts и verification restore описаны и добавлены, но сами shell-скрипты не выполнялись в этом Windows-окружении без POSIX `sh`.
- Grafana datasource/dashboard provisioning и Prometheus alert loading оформлены как runnable-артефакты, но требуют живой проверки в production-like запуске.
- Release checklist из [acceptance-checklist.md](/D:/Dev/projects/codex/split_us/docs/release/acceptance-checklist.md) ещё не отмечен как выполненный end-to-end против поднятого стека.

## Вывод По Готовности

Текущее состояние репозитория можно считать сильным MVP candidate: бизнес-ядро, Telegram-flow, settlement, admin, monitoring и backup-контур уже собраны и покрыты тестами.

При этом план ещё нельзя считать закрытым полностью. Для полного соответствия ему нужно:

1. Либо добрать перечисленные функциональные пробелы.
2. Либо явно пересогласовать scope MVP и зафиксировать, какие пункты считаются допустимыми упрощениями.
3. После этого выполнить ручной acceptance-run на production-like окружении и закрыть checklist.
