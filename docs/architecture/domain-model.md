# Domain Model

## Правила моделирования

- Деньги хранятся в копейках (`BIGINT`).
- Базовая валюта MVP фиксирована как `RUB`.
- Название таблицы `check` не используется, так как это зарезервированное слово SQL; применяется `check_book`.

## Таблицы

### `app_user`

- `id` — UUID пользователя.
- `telegram_user_id` — уникальный id пользователя Telegram.
- `telegram_username` — актуальный username.
- `registered_at` — дата регистрации.
- `updated_at` — дата обновления.

Ограничения:

- `telegram_user_id` уникален.
- `telegram_username` допускает `NULL`, но пользователь без username не может стать зарегистрированным участником чека.

### `check_book`

- `id` — UUID чека.
- `title` — название чека.
- `owner_user_id` — владелец.
- `telegram_chat_id` — id чата.
- `currency_code` — всегда `RUB`.
- `chat_active` — признак активности чата.
- `created_at` — дата создания.

Ограничения:

- `owner_user_id` ссылается на `app_user`.
- `telegram_chat_id` уникален, если известен.

### `participant`

- `id` — UUID участника.
- `check_id` — чек.
- `participant_type` — `REGISTERED` или `GUEST`.
- `display_name` — отображаемое имя.
- `linked_user_id` — ссылка на `app_user` для зарегистрированного участника.
- `merged_into_participant_id` — целевой участник, если запись деактивирована после merge.
- `created_at` — дата создания.

Ограничения:

- имя уникально в пределах чека;
- один зарегистрированный пользователь может иметь только одну активную participant-запись в пределах чека.

### `participant_merge`

- `id` — UUID события.
- `check_id` — чек.
- `source_participant_id` — объединяемый guest.
- `target_participant_id` — итоговый registered participant.
- `performed_by_participant_id` — кто инициировал merge.
- `created_at` — дата события.

### `expense`

- `id` — UUID расхода.
- `check_id` — чек.
- `amount_minor` — сумма в копейках.
- `currency_code` — `RUB`.
- `payer_participant_id` — плательщик.
- `comment` — краткий комментарий.
- `source_message_text` — исходный Telegram-текст.
- `telegram_chat_id` — связанный чат.
- `telegram_message_id` — исходное сообщение.
- `status` — `VALID`, `INVALID`, `REQUIRES_CLARIFICATION`.
- `created_by_participant_id` — автор.
- `updated_by_participant_id` — кто последним менял.
- `created_at` — дата создания.
- `updated_at` — дата обновления.

### `expense_share`

- `expense_id` — расход.
- `participant_id` — участник.
- `share_minor` — доля в копейках.

Ограничения:

- составной первичный ключ `expense_id + participant_id`.

### `admin_user`

- `id` — UUID администратора.
- `login` — уникальный логин.
- `password_hash` — `bcrypt`-хеш.
- `created_at` — дата создания.
- `updated_at` — дата обновления.

## Индексы

- `participant(check_id, display_name)` — уникальный.
- `participant(check_id, linked_user_id)` — уникальный для активных зарегистрированных участников.
- `expense(check_id, status)`.
- `expense(check_id, created_at)`.
- `expense(telegram_chat_id, telegram_message_id)` — для синхронизации изменений и удалений сообщений.

