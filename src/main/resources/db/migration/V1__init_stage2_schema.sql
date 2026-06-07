CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE game (
    id UUID PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE', 'ARCHIVED')),
    entry_path VARCHAR(255) NOT NULL,
    starts_at TIMESTAMPTZ,
    ends_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE visitor_account (
    id UUID PRIMARY KEY,
    game_id UUID NOT NULL REFERENCES game(id),
    email_normalized VARCHAR(320) NOT NULL UNIQUE,
    email_hash CHAR(64) NOT NULL,
    email_verified_at TIMESTAMPTZ,
    status VARCHAR(32) NOT NULL CHECK (status IN ('PENDING_VERIFICATION', 'ACTIVE', 'DISABLED')),
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE email_verification (
    id UUID PRIMARY KEY,
    game_id UUID NOT NULL REFERENCES game(id),
    visitor_account_id UUID REFERENCES visitor_account(id),
    email_normalized VARCHAR(320) NOT NULL,
    otp_hash VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL CHECK (status IN ('PENDING', 'VERIFIED', 'EXPIRED', 'FAILED', 'REVOKED')),
    requested_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    verified_at TIMESTAMPTZ,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    client_ip VARCHAR(64),
    user_agent VARCHAR(512),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_email_verification_lookup
    ON email_verification (email_normalized, status, expires_at DESC);

CREATE TABLE visitor_session (
    id UUID PRIMARY KEY,
    game_id UUID NOT NULL REFERENCES game(id),
    visitor_account_id UUID NOT NULL REFERENCES visitor_account(id),
    token_hash CHAR(64) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL CHECK (status IN ('ACTIVE', 'EXPIRED', 'REVOKED', 'LOGGED_OUT')),
    issued_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    last_seen_at TIMESTAMPTZ,
    client_ip VARCHAR(64),
    user_agent VARCHAR(512),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_visitor_session_lookup
    ON visitor_session (visitor_account_id, status, expires_at DESC);

CREATE TABLE quest (
    id UUID PRIMARY KEY,
    game_id UUID NOT NULL REFERENCES game(id),
    code VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    sort_order INTEGER NOT NULL,
    status VARCHAR(32) NOT NULL CHECK (status IN ('DRAFT', 'ACTIVE', 'DISABLED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_quest_game_code UNIQUE (game_id, code),
    CONSTRAINT uq_quest_game_sort_order UNIQUE (game_id, sort_order)
);

CREATE TABLE location (
    id UUID PRIMARY KEY,
    quest_id UUID NOT NULL REFERENCES quest(id),
    name VARCHAR(255) NOT NULL,
    latitude NUMERIC(9, 6) NOT NULL,
    longitude NUMERIC(9, 6) NOT NULL,
    radius_meters INTEGER NOT NULL,
    max_accuracy_meters INTEGER NOT NULL,
    hint_text TEXT,
    status VARCHAR(32) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE')),
    sort_order INTEGER NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_location_quest_sort_order UNIQUE (quest_id, sort_order)
);

CREATE TABLE quest_progress (
    id UUID PRIMARY KEY,
    game_id UUID NOT NULL REFERENCES game(id),
    visitor_account_id UUID NOT NULL REFERENCES visitor_account(id),
    quest_id UUID NOT NULL REFERENCES quest(id),
    status VARCHAR(32) NOT NULL CHECK (status IN ('NOT_STARTED', 'STARTED', 'LOCATION_VERIFIED', 'AI_RIDDLE_STARTED', 'COMPLETED')),
    started_at TIMESTAMPTZ,
    location_verified_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    last_hint_level INTEGER NOT NULL DEFAULT 0,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    last_ai_conversation_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_quest_progress_visitor_quest UNIQUE (visitor_account_id, quest_id)
);

CREATE TABLE ai_riddle_config (
    id UUID PRIMARY KEY,
    quest_id UUID NOT NULL REFERENCES quest(id),
    riddle_prompt TEXT NOT NULL,
    answer_criteria TEXT NOT NULL,
    spoiler_policy TEXT NOT NULL,
    completion_policy TEXT NOT NULL,
    status VARCHAR(32) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_ai_riddle_config_quest UNIQUE (quest_id)
);

CREATE TABLE ai_riddle_conversation (
    id UUID PRIMARY KEY,
    game_id UUID NOT NULL REFERENCES game(id),
    visitor_account_id UUID NOT NULL REFERENCES visitor_account(id),
    quest_id UUID NOT NULL REFERENCES quest(id),
    status VARCHAR(32) NOT NULL CHECK (status IN ('ACTIVE', 'COMPLETED', 'CLOSED', 'ABANDONED')),
    started_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    closed_at TIMESTAMPTZ,
    last_message_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uq_ai_riddle_conversation_active
    ON ai_riddle_conversation (visitor_account_id, quest_id)
    WHERE status = 'ACTIVE';

CREATE TABLE ai_riddle_message (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL REFERENCES ai_riddle_conversation(id) ON DELETE CASCADE,
    role VARCHAR(32) NOT NULL CHECK (role IN ('VISITOR', 'ASSISTANT', 'SYSTEM')),
    content TEXT NOT NULL,
    ai_model VARCHAR(128),
    is_answer_correct BOOLEAN,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ai_riddle_message_conversation_created_at
    ON ai_riddle_message (conversation_id, created_at);

CREATE TABLE merchant (
    id UUID PRIMARY KEY,
    game_id UUID NOT NULL REFERENCES game(id),
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    address VARCHAR(255),
    contact_name VARCHAR(255),
    contact_phone VARCHAR(64),
    status VARCHAR(32) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_merchant_game_code UNIQUE (game_id, code)
);

CREATE TABLE coupon_campaign (
    id UUID PRIMARY KEY,
    game_id UUID NOT NULL REFERENCES game(id),
    quest_id UUID NOT NULL REFERENCES quest(id),
    merchant_id UUID NOT NULL REFERENCES merchant(id),
    code VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(32) NOT NULL CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE', 'EXPIRED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_coupon_campaign_game_code UNIQUE (game_id, code),
    CONSTRAINT uq_coupon_campaign_quest UNIQUE (quest_id)
);

CREATE TABLE coupon (
    id UUID PRIMARY KEY,
    game_id UUID NOT NULL REFERENCES game(id),
    visitor_account_id UUID NOT NULL REFERENCES visitor_account(id),
    quest_id UUID NOT NULL REFERENCES quest(id),
    coupon_campaign_id UUID NOT NULL REFERENCES coupon_campaign(id),
    status VARCHAR(32) NOT NULL CHECK (status IN ('ISSUED', 'CONSUMED', 'EXPIRED', 'REVOKED')),
    issued_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ,
    display_code VARCHAR(32) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_coupon_visitor_campaign UNIQUE (visitor_account_id, coupon_campaign_id)
);

CREATE TABLE coupon_usage (
    id UUID PRIMARY KEY,
    coupon_id UUID NOT NULL REFERENCES coupon(id),
    visitor_account_id UUID NOT NULL REFERENCES visitor_account(id),
    used_at TIMESTAMPTZ NOT NULL,
    client_confirmed_at TIMESTAMPTZ,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_coupon_usage_coupon UNIQUE (coupon_id)
);

CREATE TABLE knowledge_document (
    id UUID PRIMARY KEY,
    game_id UUID NOT NULL REFERENCES game(id),
    quest_id UUID REFERENCES quest(id),
    location_id UUID REFERENCES location(id),
    title VARCHAR(255) NOT NULL,
    source VARCHAR(255) NOT NULL,
    spoiler_level VARCHAR(64) NOT NULL,
    version INTEGER NOT NULL,
    embedding_status VARCHAR(32) NOT NULL CHECK (embedding_status IN ('PENDING', 'INDEXED', 'FAILED', 'ARCHIVED')),
    indexed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE quest_progress
    ADD CONSTRAINT fk_quest_progress_last_ai_conversation
    FOREIGN KEY (last_ai_conversation_id) REFERENCES ai_riddle_conversation(id);
