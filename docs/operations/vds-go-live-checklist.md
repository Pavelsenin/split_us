# Чеклист Вывода В Production На VDS

## Назначение

Этот документ описывает последовательный plan-of-action для вывода Split Us в production-like среду на VDS: от подготовки домена и регистрации Telegram-бота до первого smoke-test после деплоя.

Документ ориентирован на текущую реализацию репозитория:

- backend, PostgreSQL, Prometheus, Grafana и Caddy поднимаются через `docker compose`
- HTTPS терминируется на Caddy
- backend принимает Telegram webhook по `https://APP_DOMAIN/api/telegram/webhook/<alias>`
- приложение не хранит `TELEGRAM_BOT_TOKEN` в `.env`; токен нужен снаружи для регистрации webhook через Telegram Bot API

## Результат На Выходе

После прохождения чеклиста должно быть готово:

- VDS с настроенным Docker runtime
- DNS для backend и Grafana
- production `.env`
- поднятый compose stack
- зарегистрированный Telegram webhook
- доступный `/admin`
- доступная Grafana
- выполненный smoke-test

## 1. Подготовить Входные Данные

- [ ] Есть VDS с публичным IPv4-адресом.
- [ ] Есть домен или поддомен для приложения, например `splitus.example.com`.
- [ ] Есть отдельный домен или поддомен для Grafana, например `grafana.splitus.example.com`.
- [ ] Есть email для ACME/Let's Encrypt, например `ops@example.com`.
- [ ] Есть SSH-ключ для входа на сервер.
- [ ] Есть Telegram-аккаунт, с которого будет зарегистрирован бот через BotFather.
- [ ] Подготовлены секреты:
  - [ ] `POSTGRES_PASSWORD`
  - [ ] `TELEGRAM_WEBHOOK_SECRET`
  - [ ] `INTERNAL_SERVICE_TOKEN`
  - [ ] пароль администратора
  - [ ] пароль Grafana

Рекомендуемый формат секретов:

- длина не меньше 24-32 символов
- случайные буквы, цифры и спецсимволы
- без повторного использования между сервисами

## 2. Зарегистрировать Telegram-Бота

### 2.1 Создать Бота Через BotFather

- [ ] Открыть `@BotFather` в Telegram.
- [ ] Выполнить `/newbot`.
- [ ] Указать display name, например `Split Us Bot`.
- [ ] Указать username, который заканчивается на `bot`, например `splitus_prod_bot`.
- [ ] Сохранить выданный `bot token` в секретное хранилище.

Что куда пойдёт дальше:

- username бота попадёт в `TELEGRAM_BOT_USERNAME`
- token бота не кладётся в `.env` текущего приложения, но потребуется для вызовов `setWebhook` и `getWebhookInfo`

### 2.2 Настроить Базовый Профиль Бота

- [ ] По желанию задать `/setdescription`.
- [ ] По желанию задать `/setabouttext`.
- [ ] По желанию задать `/setuserpic`.

### 2.3 Проверить Групповые Настройки

- [ ] Выполнить `/setjoingroups` и убедиться, что бот может быть добавлен в групповые чаты.
- [ ] Не отключать privacy mode без отдельной причины.

Примечание: текущий MVP опирается на slash-команды и webhook updates `message` / `edited_message`. Для этого отдельное отключение privacy mode сейчас не требуется. Если позже появится free-text parsing, это решение нужно будет пересмотреть.

## 3. Подготовить DNS

- [ ] Создать `A`-запись для `APP_DOMAIN`, указывающую на IP VDS.
- [ ] Создать `A`-запись для `GRAFANA_DOMAIN`, указывающую на тот же IP VDS.
- [ ] Дождаться применения DNS.

Проверка с локальной машины:

```bash
dig +short splitus.example.com
dig +short grafana.splitus.example.com
```

Обе команды должны вернуть IP VDS.

## 4. Подготовить VDS

Ниже пример для Ubuntu 24.04 LTS. Если образ другой, команды могут немного отличаться.

### 4.1 Базовая Инициализация Сервера

- [ ] Подключиться по SSH под root или bootstrap-пользователем.
- [ ] Обновить пакеты.
- [ ] Настроить time zone сервера.
- [ ] Создать отдельного пользователя для деплоя, например `splitus`.
- [ ] Добавить SSH-ключ этому пользователю.
- [ ] Запретить password login по SSH, если это допустимо в окружении.

Пример:

```bash
apt update && apt upgrade -y
timedatectl set-timezone Europe/Moscow
adduser splitus
usermod -aG sudo splitus
mkdir -p /home/splitus/.ssh
cp /root/.ssh/authorized_keys /home/splitus/.ssh/authorized_keys
chown -R splitus:splitus /home/splitus/.ssh
chmod 700 /home/splitus/.ssh
chmod 600 /home/splitus/.ssh/authorized_keys
```

### 4.2 Открыть Только Нужные Порты

- [ ] Разрешить `22/tcp`.
- [ ] Разрешить `80/tcp`.
- [ ] Разрешить `443/tcp`.
- [ ] Не публиковать PostgreSQL, Prometheus и Grafana наружу напрямую сверх текущего compose-контура.

Пример с `ufw`:

```bash
ufw allow OpenSSH
ufw allow 80/tcp
ufw allow 443/tcp
ufw enable
ufw status
```

## 5. Установить Docker Runtime

- [ ] Установить Docker Engine.
- [ ] Установить Docker Compose plugin.
- [ ] Установить Git.
- [ ] Добавить пользователя `splitus` в группу `docker`.

После установки:

```bash
docker --version
docker compose version
git --version
```

## 6. Подготовить Рабочую Директорию

- [ ] Войти под пользователем `splitus`.
- [ ] Создать каталог приложения, например `/opt/split_us`.
- [ ] Склонировать репозиторий.

Пример:

```bash
sudo mkdir -p /opt/split_us
sudo chown splitus:splitus /opt/split_us
cd /opt/split_us
git clone <REPO_URL> .
```

## 7. Подготовить Production `.env`

- [ ] Скопировать [`.env.example`](/D:/Dev/projects/codex/split_us/.env.example) в `.env`.
- [ ] Заполнить production-значения.

Минимальный пример:

```dotenv
POSTGRES_DB=splitus
POSTGRES_USER=splitus
POSTGRES_PASSWORD=CHANGE_ME_STRONG_DB_PASSWORD

APP_PORT=8080
APP_DOMAIN=splitus.example.com
GRAFANA_DOMAIN=grafana.splitus.example.com
ACME_EMAIL=ops@example.com

DB_HOST=localhost
DB_PORT=5432
DB_NAME=splitus
DB_USER=splitus
DB_PASSWORD=CHANGE_ME_STRONG_DB_PASSWORD

GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=CHANGE_ME_STRONG_GRAFANA_PASSWORD

TELEGRAM_WEBHOOK_ALIAS=prod-bot
TELEGRAM_WEBHOOK_SECRET=CHANGE_ME_STRONG_TELEGRAM_SECRET
TELEGRAM_BOT_USERNAME=splitus_prod_bot

INTERNAL_SERVICE_TOKEN=CHANGE_ME_STRONG_INTERNAL_TOKEN
ADMIN_ENVIRONMENT_NAME=production
ADMIN_BOOTSTRAP_LOGIN=admin
ADMIN_BOOTSTRAP_PASSWORD=CHANGE_ME_STRONG_ADMIN_PASSWORD
ADMIN_BOOTSTRAP_PASSWORD_HASH=
```

Пояснения:

- `APP_DOMAIN` — боевой домен backend и admin.
- `GRAFANA_DOMAIN` — боевой домен Grafana.
- `TELEGRAM_WEBHOOK_ALIAS` — последний сегмент webhook path.
- `TELEGRAM_WEBHOOK_SECRET` — значение, которое Telegram будет присылать в `X-Telegram-Bot-Api-Secret-Token`.
- `TELEGRAM_BOT_USERNAME` — username бота без `@`.
- `INTERNAL_SERVICE_TOKEN` — токен для `/api/internal/**`.
- `ADMIN_BOOTSTRAP_PASSWORD_HASH` оставляется пустым, если используется обычный bootstrap password.

Примечание по `DB_*`: в compose-запуске backend получает `DB_HOST=postgres` и связанные значения прямо из [docker-compose.yml](/D:/Dev/projects/codex/split_us/docker-compose.yml). Значения `DB_*` в `.env` полезны в основном для вне-container запуска backend. Их можно оставить синхронными с PostgreSQL для удобства.

## 8. Проверить Конфигурацию До Запуска

- [ ] Убедиться, что в `.env` нет `change-me`.
- [ ] Убедиться, что `APP_DOMAIN` и `GRAFANA_DOMAIN` совпадают с DNS.
- [ ] Убедиться, что `TELEGRAM_BOT_USERNAME` совпадает с реальным username бота.
- [ ] Убедиться, что задан только один из `ADMIN_BOOTSTRAP_PASSWORD` или `ADMIN_BOOTSTRAP_PASSWORD_HASH`.
- [ ] Прогнать:

```bash
docker compose config
```

Ожидаемый результат: compose-конфигурация рендерится без ошибок.

## 9. Поднять Стек

- [ ] Выполнить первый production-like запуск:

```bash
docker compose up -d --build
```

- [ ] Проверить статусы контейнеров:

```bash
docker compose ps
```

- [ ] Проверить логи backend:

```bash
docker compose logs backend --tail=200
```

- [ ] Проверить логи Caddy:

```bash
docker compose logs caddy --tail=200
```

Что должно быть в порядке:

- backend стартовал и применил Flyway migrations
- postgres healthy
- prometheus и grafana поднялись
- caddy получил или пытается получить сертификаты для реальных доменов

## 10. Зарегистрировать Telegram Webhook

На сервере или локально подготовить переменные:

```bash
export TELEGRAM_BOT_TOKEN='<BOT_TOKEN_FROM_BOTFATHER>'
export APP_DOMAIN='splitus.example.com'
export TELEGRAM_WEBHOOK_ALIAS='prod-bot'
export TELEGRAM_WEBHOOK_SECRET='CHANGE_ME_STRONG_TELEGRAM_SECRET'
```

Зарегистрировать webhook:

```bash
curl -X POST "https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/setWebhook" \
  -F "url=https://${APP_DOMAIN}/api/telegram/webhook/${TELEGRAM_WEBHOOK_ALIAS}" \
  -F "secret_token=${TELEGRAM_WEBHOOK_SECRET}" \
  -F 'allowed_updates=["message","edited_message"]' \
  -F "drop_pending_updates=true"
```

- [ ] Ответ Telegram содержит `"ok": true`.

Проверить статус webhook:

```bash
curl "https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/getWebhookInfo"
```

- [ ] `url` совпадает с production webhook URL.
- [ ] Нет `last_error_message`.

## 11. Выполнить Инфраструктурный Smoke-Test

### 11.1 Проверить Backend

- [ ] Проверить health:

```bash
curl https://splitus.example.com/api/internal/health/live
curl https://splitus.example.com/api/internal/health/ready
```

Обе ручки должны вернуть `status=UP`.

### 11.2 Проверить Admin

- [ ] Открыть `https://APP_DOMAIN/admin/login`.
- [ ] Войти под `ADMIN_BOOTSTRAP_LOGIN` и `ADMIN_BOOTSTRAP_PASSWORD`.
- [ ] Убедиться, что открывается `/admin`.

### 11.3 Проверить Grafana

- [ ] Открыть `https://GRAFANA_DOMAIN/`.
- [ ] Войти под `GRAFANA_ADMIN_USER` / `GRAFANA_ADMIN_PASSWORD`.
- [ ] Убедиться, что datasource Prometheus уже provisioned.
- [ ] Убедиться, что dashboard `SplitUs Overview` доступен.

### 11.4 Проверить Prometheus

- [ ] Убедиться, что `/actuator/prometheus` отдаёт метрики из backend.
- [ ] Убедиться, что target backend в Prometheus имеет статус `UP`.

## 12. Выполнить Прикладной Smoke-Test В Telegram

- [ ] Открыть диалог с ботом.
- [ ] Выполнить `/new_check Smoke Test`.
- [ ] Получить `deep link` на join.
- [ ] Проверить `/start join_<token>` вторым пользователем или тестовым аккаунтом.
- [ ] Проверить `/add_guest <token> <name>`.
- [ ] Проверить `/add_expense <token> <amount_minor> <участники> | <комментарий>`.
- [ ] Проверить `/settle <token>`.

Минимальный целевой сценарий:

1. Создать чек.
2. Добавить одного guest.
3. Добавить один расход.
4. Получить settlement без ошибок.

## 13. Настроить Backup И Проверку Restore

- [ ] Убедиться, что существует каталог для backup-артефактов.
- [ ] Выполнить первый ручной backup:

```bash
./scripts/backup-db.sh
```

- [ ] Выполнить контрольный restore:

```bash
./scripts/verify-latest-backup.sh
```

- [ ] Настроить ежедневный backup.
- [ ] Настроить weekly verification restore.

Команды и примеры cron уже описаны в [backup-restore.md](/D:/Dev/projects/codex/split_us/docs/operations/backup-restore.md).

## 14. Финальный Go-Live Check

- [ ] `docker compose ps` показывает healthy или running состояние всех сервисов.
- [ ] `getWebhookInfo` не содержит ошибок.
- [ ] Admin login работает.
- [ ] Grafana доступна по HTTPS.
- [ ] Первый backup создан.
- [ ] Verification restore выполнен успешно.
- [ ] Smoke scenario в Telegram пройден.
- [ ] Актуальный production `.env` сохранён вне репозитория.

## 15. Что Сделать Сразу После Go-Live

- [ ] Сохранить bot token, `.env` и SSH-доступ в секретное хранилище команды.
- [ ] Зафиксировать ссылку на Grafana dashboard.
- [ ] Зафиксировать ссылку на admin login.
- [ ] Назначить ответственного за ежедневный мониторинг первых дней.
- [ ] Выполнить отдельный acceptance-run по [acceptance-checklist.md](/D:/Dev/projects/codex/split_us/docs/release/acceptance-checklist.md).

## Короткий Порядок Без Деталей

1. Зарегистрировать бота в BotFather.
2. Подготовить DNS.
3. Подготовить VDS и Docker.
4. Развернуть репозиторий.
5. Заполнить `.env`.
6. Поднять `docker compose`.
7. Зарегистрировать webhook через Telegram Bot API.
8. Проверить backend, admin и Grafana.
9. Прогнать Telegram smoke-test.
10. Настроить backup и verification restore.
