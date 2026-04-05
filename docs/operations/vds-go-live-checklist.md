# Чеклист Вывода В Production На VDS

## Назначение

Этот документ описывает последовательный checklist для вывода Split Us в production на VDS: от домена и Telegram-бота до первого smoke-test после деплоя.

Документ соответствует текущей инфраструктуре репозитория:

- backend, PostgreSQL, Prometheus, Grafana и Nginx поднимаются через `docker compose`
- HTTPS терминируется на Nginx
- сертификаты подготавливаются вне compose-стека и монтируются в `infra/nginx/certs`
- backend принимает Telegram webhook по `https://APP_DOMAIN/api/telegram/webhook/<alias>`

## Результат На Выходе

После прохождения чеклиста должно быть готово:

- VDS с Docker runtime
- DNS для backend и Grafana
- production `.env`
- TLS-сертификаты для Nginx
- поднятый compose stack
- зарегистрированный Telegram webhook
- доступные `/admin` и Grafana
- выполненный smoke-test

## 1. Подготовить Входные Данные

- [ ] Есть VDS с публичным IPv4-адресом.
- [ ] Есть домен для приложения, например `splitus.example.com`.
- [ ] Есть домен для Grafana, например `grafana.splitus.example.com`.
- [ ] Есть SSH-ключ для входа на сервер.
- [ ] Есть Telegram-аккаунт для работы с BotFather.
- [ ] Подготовлены секреты:
- [ ] `POSTGRES_PASSWORD`
- [ ] `TELEGRAM_WEBHOOK_SECRET`
- [ ] `INTERNAL_SERVICE_TOKEN`
- [ ] пароль администратора
- [ ] пароль Grafana

Рекомендуемый формат секретов:

- длина от 24 символов
- случайные буквы, цифры и спецсимволы
- отдельное значение для каждого секрета

## 2. Зарегистрировать Telegram-Бота

### 2.1 Создать Бота Через BotFather

- [ ] Открыть `@BotFather`.
- [ ] Выполнить `/newbot`.
- [ ] Указать display name, например `Split Us Bot`.
- [ ] Указать username, который заканчивается на `bot`, например `splitus_prod_bot`.
- [ ] Сохранить выданный `bot token` в секретное хранилище.

Что используется в проекте:

- `TELEGRAM_BOT_USERNAME` = username бота без `@`
- `bot token` не хранится в `.env` текущего приложения, но нужен для `setWebhook` и `getWebhookInfo`

### 2.2 Настроить Базовый Профиль Бота

- [ ] По желанию задать `/setdescription`.
- [ ] По желанию задать `/setabouttext`.
- [ ] По желанию задать `/setuserpic`.
- [ ] Выполнить `/setjoingroups` и убедиться, что бот можно добавлять в группы.

## 3. Подготовить DNS

- [ ] Создать `A`-запись для `APP_DOMAIN` на IP VDS.
- [ ] Создать `A`-запись для `GRAFANA_DOMAIN` на IP VDS.
- [ ] Дождаться применения DNS.

Проверка:

```bash
dig +short splitus.example.com
dig +short grafana.splitus.example.com
```

## 4. Подготовить VDS

Ниже предполагается Ubuntu 24.04 LTS или близкая система.

### 4.1 Базовая Инициализация

- [ ] Подключиться по SSH.
- [ ] Обновить пакеты.
- [ ] Настроить time zone.
- [ ] Создать пользователя деплоя, например `splitus`.
- [ ] Добавить SSH-ключ пользователю.
- [ ] При необходимости отключить password login по SSH.

Пример:

```bash
apt update && apt upgrade -y
timedatectl set-timezone Europe/Moscow
adduser splitus
usermod -aG sudo splitus
```

### 4.2 Открыть Только Нужные Порты

- [ ] Разрешить `22/tcp`.
- [ ] Разрешить `80/tcp`.
- [ ] Разрешить `443/tcp`.
- [ ] Не публиковать PostgreSQL и Prometheus наружу сверх текущего compose-контура.

Пример:

```bash
ufw allow OpenSSH
ufw allow 80/tcp
ufw allow 443/tcp
ufw enable
```

## 5. Установить Docker Runtime

- [ ] Установить Docker Engine.
- [ ] Установить Docker Compose plugin.
- [ ] Установить Git.
- [ ] Добавить пользователя `splitus` в группу `docker`.

Проверка:

```bash
docker --version
docker compose version
git --version
```

## 6. Подготовить Рабочую Директорию

- [ ] Создать каталог приложения, например `/opt/split_us`.
- [ ] Склонировать репозиторий.

Пример:

```bash
sudo mkdir -p /opt/split_us
sudo chown splitus:splitus /opt/split_us
cd /opt/split_us
git clone <REPO_URL> .
```

## 7. Подготовить TLS-Сертификаты Для Nginx

### 7.1 Выбрать Источник Сертификатов

- [ ] Используется wildcard/SAN сертификат, уже выпущенный вне проекта.

или

- [ ] Сертификаты выпускаются через внешний `certbot`, панель хостинга или корпоративный PKI.

### 7.2 Подготовить Файлы

Нужно получить:

- [ ] сертификат и ключ для `APP_DOMAIN`
- [ ] сертификат и ключ для `GRAFANA_DOMAIN`

Если используется один wildcard или SAN сертификат:

- [ ] допускается использовать один и тот же `crt/key` для обоих доменов

### 7.3 Разложить Файлы В Репозитории На Сервере

- [ ] Создать каталог `infra/nginx/certs/`.
- [ ] Скопировать сертификаты в этот каталог.

Минимальный вариант с дефолтными именами:

- [ ] `infra/nginx/certs/app.crt`
- [ ] `infra/nginx/certs/app.key`
- [ ] `infra/nginx/certs/grafana.crt`
- [ ] `infra/nginx/certs/grafana.key`

## 8. Подготовить Production `.env`

- [ ] Скопировать [`.env.example`](/D:/Dev/projects/codex/split_us/.env.example) в `.env`.
- [ ] Заполнить production-значения.

Пример:

```dotenv
POSTGRES_DB=splitus
POSTGRES_USER=splitus
POSTGRES_PASSWORD=CHANGE_ME_STRONG_DB_PASSWORD

APP_PORT=8080
APP_DOMAIN=splitus.example.com
GRAFANA_DOMAIN=grafana.splitus.example.com

DB_HOST=localhost
DB_PORT=5432
DB_NAME=splitus
DB_USER=splitus
DB_PASSWORD=CHANGE_ME_STRONG_DB_PASSWORD

GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=CHANGE_ME_STRONG_GRAFANA_PASSWORD

NGINX_APP_CERTIFICATE=app.crt
NGINX_APP_CERTIFICATE_KEY=app.key
NGINX_GRAFANA_CERTIFICATE=grafana.crt
NGINX_GRAFANA_CERTIFICATE_KEY=grafana.key

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

- `NGINX_*` переменные задают filenames внутри `infra/nginx/certs/`
- если используется один wildcard сертификат, можно указать одинаковые filenames для app и Grafana
- `ADMIN_BOOTSTRAP_PASSWORD_HASH` оставляется пустым, если используется обычный пароль

## 9. Проверить Конфигурацию До Запуска

- [ ] В `.env` нет `change-me`.
- [ ] Домены совпадают с DNS.
- [ ] `TELEGRAM_BOT_USERNAME` совпадает с реальным username бота.
- [ ] Задан только один из `ADMIN_BOOTSTRAP_PASSWORD` или `ADMIN_BOOTSTRAP_PASSWORD_HASH`.
- [ ] Все certificate files реально лежат в `infra/nginx/certs/`.
- [ ] Выполнить:

```bash
docker compose config
```

## 10. Поднять Стек

- [ ] Выполнить:

```bash
docker compose up -d --build
```

- [ ] Проверить:

```bash
docker compose ps
docker compose logs backend --tail=200
docker compose logs nginx --tail=200
```

Что должно быть в порядке:

- backend стартовал и применил Flyway migrations
- postgres healthy
- prometheus и grafana поднялись
- nginx стартовал без ошибок по certificate files и server blocks

## 11. Зарегистрировать Telegram Webhook

Подготовить переменные:

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

Проверить статус:

```bash
curl "https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/getWebhookInfo"
```

- [ ] `url` совпадает с production webhook URL.
- [ ] `last_error_message` отсутствует.

## 12. Выполнить Инфраструктурный Smoke-Test

### 12.1 Backend

- [ ] Проверить:

```bash
curl https://splitus.example.com/api/internal/health/live
curl https://splitus.example.com/api/internal/health/ready
```

### 12.2 Admin

- [ ] Открыть `https://APP_DOMAIN/admin/login`.
- [ ] Войти под `ADMIN_BOOTSTRAP_LOGIN` / `ADMIN_BOOTSTRAP_PASSWORD`.
- [ ] Убедиться, что открывается `/admin`.

### 12.3 Grafana

- [ ] Открыть `https://GRAFANA_DOMAIN/`.
- [ ] Войти под `GRAFANA_ADMIN_USER` / `GRAFANA_ADMIN_PASSWORD`.
- [ ] Убедиться, что datasource Prometheus provisioned.
- [ ] Убедиться, что dashboard `SplitUs Overview` доступен.

## 13. Выполнить Telegram Smoke-Test

- [ ] Выполнить `/new_check Smoke Test`.
- [ ] Получить join link.
- [ ] Проверить `/start join_<token>` вторым пользователем.
- [ ] Проверить `/add_guest <token> <name>`.
- [ ] Проверить `/add_expense <token> <amount_minor> <участники> | <комментарий>`.
- [ ] Проверить `/settle <token>`.

Минимальный сценарий:

1. создать чек
2. добавить одного guest
3. добавить один расход
4. получить settlement без ошибок

## 14. Настроить Backup И Проверку Restore

- [ ] Выполнить первый backup:

```bash
./scripts/backup-db.sh
```

- [ ] Выполнить verification restore:

```bash
./scripts/verify-latest-backup.sh
```

- [ ] Настроить ежедневный backup.
- [ ] Настроить weekly verification restore.

Подробности уже описаны в [backup-restore.md](/D:/Dev/projects/codex/split_us/docs/operations/backup-restore.md).

## 15. Финальный Go-Live Check

- [ ] `docker compose ps` показывает running/healthy состояние сервисов.
- [ ] `getWebhookInfo` не содержит ошибок.
- [ ] Admin login работает.
- [ ] Grafana доступна по HTTPS.
- [ ] Первый backup создан.
- [ ] Verification restore выполнен успешно.
- [ ] Telegram smoke-test пройден.
- [ ] Актуальный production `.env` хранится вне репозитория.

## Короткий Порядок Без Деталей

1. Зарегистрировать бота в BotFather.
2. Подготовить DNS.
3. Подготовить VDS и Docker.
4. Выпустить или получить TLS-сертификаты.
5. Развернуть репозиторий.
6. Заполнить `.env`.
7. Поднять `docker compose`.
8. Зарегистрировать webhook через Telegram Bot API.
9. Проверить backend, admin и Grafana.
10. Прогнать Telegram smoke-test и backup verification.
