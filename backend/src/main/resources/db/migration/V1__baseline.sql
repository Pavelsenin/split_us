CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE app_user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    telegram_user_id BIGINT NOT NULL UNIQUE,
    telegram_username VARCHAR(64),
    registered_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE check_book (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    owner_user_id UUID NOT NULL REFERENCES app_user(id),
    telegram_chat_id BIGINT UNIQUE,
    currency_code CHAR(3) NOT NULL DEFAULT 'RUB',
    chat_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE participant (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    check_id UUID NOT NULL REFERENCES check_book(id) ON DELETE CASCADE,
    participant_type VARCHAR(16) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    linked_user_id UUID REFERENCES app_user(id),
    merged_into_participant_id UUID REFERENCES participant(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT participant_type_check CHECK (participant_type IN ('REGISTERED', 'GUEST'))
);

CREATE UNIQUE INDEX ux_participant_check_name
    ON participant(check_id, display_name);

CREATE UNIQUE INDEX ux_participant_check_user
    ON participant(check_id, linked_user_id)
    WHERE linked_user_id IS NOT NULL AND merged_into_participant_id IS NULL;

CREATE TABLE participant_merge (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    check_id UUID NOT NULL REFERENCES check_book(id) ON DELETE CASCADE,
    source_participant_id UUID NOT NULL REFERENCES participant(id),
    target_participant_id UUID NOT NULL REFERENCES participant(id),
    performed_by_participant_id UUID NOT NULL REFERENCES participant(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE expense (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    check_id UUID NOT NULL REFERENCES check_book(id) ON DELETE CASCADE,
    amount_minor BIGINT NOT NULL,
    currency_code CHAR(3) NOT NULL DEFAULT 'RUB',
    payer_participant_id UUID NOT NULL REFERENCES participant(id),
    comment VARCHAR(500),
    source_message_text TEXT,
    telegram_chat_id BIGINT,
    telegram_message_id BIGINT,
    status VARCHAR(32) NOT NULL,
    created_by_participant_id UUID NOT NULL REFERENCES participant(id),
    updated_by_participant_id UUID NOT NULL REFERENCES participant(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT expense_status_check CHECK (status IN ('VALID', 'INVALID', 'REQUIRES_CLARIFICATION')),
    CONSTRAINT expense_amount_positive CHECK (amount_minor > 0)
);

CREATE INDEX ix_expense_check_status
    ON expense(check_id, status);

CREATE INDEX ix_expense_check_created_at
    ON expense(check_id, created_at);

CREATE UNIQUE INDEX ux_expense_telegram_message
    ON expense(telegram_chat_id, telegram_message_id)
    WHERE telegram_chat_id IS NOT NULL AND telegram_message_id IS NOT NULL;

CREATE TABLE expense_share (
    expense_id UUID NOT NULL REFERENCES expense(id) ON DELETE CASCADE,
    participant_id UUID NOT NULL REFERENCES participant(id),
    share_minor BIGINT NOT NULL,
    PRIMARY KEY (expense_id, participant_id),
    CONSTRAINT expense_share_non_negative CHECK (share_minor >= 0)
);

CREATE TABLE admin_user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    login VARCHAR(128) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

