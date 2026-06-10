UPDATE knowledge_document
   SET content = '茶園入口附近有主要提示點，遊客需先完成定位，再透過 AI 猜謎取得通關線索。'
 WHERE id = '66666666-6666-6666-6666-666666666666';

INSERT INTO visitor_account (
    id,
    game_id,
    email_normalized,
    email_hash,
    email_verified_at,
    status,
    last_login_at,
    created_at,
    updated_at
) VALUES (
    '77777777-7777-7777-7777-777777777777',
    '11111111-1111-1111-1111-111111111111',
    'demo.visitor@example.com',
    'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa',
    CURRENT_TIMESTAMP,
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email_normalized) DO NOTHING;

INSERT INTO email_verification (
    id,
    game_id,
    visitor_account_id,
    email_normalized,
    otp_hash,
    status,
    requested_at,
    expires_at,
    verified_at,
    attempt_count,
    client_ip,
    user_agent,
    created_at
) VALUES (
    '88888888-8888-8888-8888-888888888888',
    '11111111-1111-1111-1111-111111111111',
    '77777777-7777-7777-7777-777777777777',
    'demo.visitor@example.com',
    'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb',
    'VERIFIED',
    CURRENT_TIMESTAMP - INTERVAL '20 minutes',
    CURRENT_TIMESTAMP - INTERVAL '10 minutes',
    CURRENT_TIMESTAMP - INTERVAL '18 minutes',
    1,
    '127.0.0.1',
    'stage8-demo',
    CURRENT_TIMESTAMP - INTERVAL '20 minutes'
)
ON CONFLICT DO NOTHING;

INSERT INTO visitor_session (
    id,
    game_id,
    visitor_account_id,
    token_hash,
    status,
    issued_at,
    expires_at,
    revoked_at,
    last_seen_at,
    client_ip,
    user_agent,
    created_at,
    updated_at
) VALUES (
    '99999999-9999-9999-9999-999999999999',
    '11111111-1111-1111-1111-111111111111',
    '77777777-7777-7777-7777-777777777777',
    'cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc',
    'ACTIVE',
    CURRENT_TIMESTAMP - INTERVAL '15 minutes',
    CURRENT_TIMESTAMP + INTERVAL '23 hours 45 minutes',
    NULL,
    CURRENT_TIMESTAMP,
    '127.0.0.1',
    'stage8-demo',
    CURRENT_TIMESTAMP - INTERVAL '15 minutes',
    CURRENT_TIMESTAMP
)
ON CONFLICT (token_hash) DO NOTHING;

INSERT INTO ai_riddle_config (
    id,
    quest_id,
    riddle_prompt,
    answer_criteria,
    spoiler_policy,
    completion_policy,
    status,
    created_at,
    updated_at
) VALUES (
    'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1',
    '22222222-2222-2222-2222-222222222222',
    '請根據茶園現地線索引導遊客回答品種相關問題。',
    '答案需明確提及茶葉品種或其特徵。',
    '只可提供漸進提示，不直接洩漏完整答案。',
    '當答案符合條件時即可完成任務。',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (quest_id) DO NOTHING;

INSERT INTO ai_riddle_conversation (
    id,
    game_id,
    visitor_account_id,
    quest_id,
    status,
    started_at,
    completed_at,
    closed_at,
    last_message_at,
    created_at,
    updated_at
) VALUES (
    'aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaa2',
    '11111111-1111-1111-1111-111111111111',
    '77777777-7777-7777-7777-777777777777',
    '22222222-2222-2222-2222-222222222222',
    'COMPLETED',
    CURRENT_TIMESTAMP - INTERVAL '12 minutes',
    CURRENT_TIMESTAMP - INTERVAL '9 minutes',
    CURRENT_TIMESTAMP - INTERVAL '9 minutes',
    CURRENT_TIMESTAMP - INTERVAL '9 minutes',
    CURRENT_TIMESTAMP - INTERVAL '12 minutes',
    CURRENT_TIMESTAMP - INTERVAL '9 minutes'
)
ON CONFLICT DO NOTHING;

INSERT INTO ai_riddle_message (
    id,
    conversation_id,
    role,
    content,
    ai_model,
    is_answer_correct,
    metadata,
    created_at
) VALUES
(
    'aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaa3',
    'aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaa2',
    'ASSISTANT',
    '請觀察茶園周遭資訊，試著描述這裡主要種植的茶葉特徵。',
    'demo-model',
    NULL,
    '{"stage":"prompt"}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '11 minutes'
),
(
    'aaaaaaa4-aaaa-aaaa-aaaa-aaaaaaaaaaa4',
    'aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaa2',
    'VISITOR',
    '這裡主要是烏龍茶，而且葉片厚實。',
    NULL,
    TRUE,
    '{"stage":"answer"}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '10 minutes'
)
ON CONFLICT DO NOTHING;

INSERT INTO quest_progress (
    id,
    game_id,
    visitor_account_id,
    quest_id,
    status,
    started_at,
    location_verified_at,
    completed_at,
    last_hint_level,
    attempt_count,
    last_ai_conversation_id,
    created_at,
    updated_at
) VALUES (
    'aaaaaaa5-aaaa-aaaa-aaaa-aaaaaaaaaaa5',
    '11111111-1111-1111-1111-111111111111',
    '77777777-7777-7777-7777-777777777777',
    '22222222-2222-2222-2222-222222222222',
    'COMPLETED',
    CURRENT_TIMESTAMP - INTERVAL '14 minutes',
    CURRENT_TIMESTAMP - INTERVAL '13 minutes',
    CURRENT_TIMESTAMP - INTERVAL '9 minutes',
    1,
    2,
    'aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaa2',
    CURRENT_TIMESTAMP - INTERVAL '14 minutes',
    CURRENT_TIMESTAMP - INTERVAL '9 minutes'
)
ON CONFLICT (visitor_account_id, quest_id) DO NOTHING;

INSERT INTO coupon (
    id,
    game_id,
    visitor_account_id,
    quest_id,
    coupon_campaign_id,
    status,
    issued_at,
    expires_at,
    consumed_at,
    display_code,
    created_at,
    updated_at
) VALUES (
    'aaaaaaa6-aaaa-aaaa-aaaa-aaaaaaaaaaa6',
    '11111111-1111-1111-1111-111111111111',
    '77777777-7777-7777-7777-777777777777',
    '22222222-2222-2222-2222-222222222222',
    '55555555-5555-5555-5555-555555555555',
    'CONSUMED',
    CURRENT_TIMESTAMP - INTERVAL '8 minutes',
    CURRENT_TIMESTAMP + INTERVAL '7 days',
    CURRENT_TIMESTAMP - INTERVAL '5 minutes',
    'DEMO-TEA-001',
    CURRENT_TIMESTAMP - INTERVAL '8 minutes',
    CURRENT_TIMESTAMP - INTERVAL '5 minutes'
)
ON CONFLICT (display_code) DO NOTHING;

INSERT INTO coupon_usage (
    id,
    coupon_id,
    visitor_account_id,
    used_at,
    client_confirmed_at,
    metadata,
    created_at
) VALUES (
    'aaaaaaa7-aaaa-aaaa-aaaa-aaaaaaaaaaa7',
    'aaaaaaa6-aaaa-aaaa-aaaa-aaaaaaaaaaa6',
    '77777777-7777-7777-7777-777777777777',
    CURRENT_TIMESTAMP - INTERVAL '5 minutes',
    CURRENT_TIMESTAMP - INTERVAL '5 minutes',
    '{"source":"stage8-demo"}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '5 minutes'
)
ON CONFLICT DO NOTHING;

CREATE INDEX IF NOT EXISTS idx_email_verification_status_expires_at
    ON email_verification (status, expires_at);

CREATE INDEX IF NOT EXISTS idx_visitor_session_status_expires_at
    ON visitor_session (status, expires_at);
