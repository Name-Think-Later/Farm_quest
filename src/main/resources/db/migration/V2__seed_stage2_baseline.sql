INSERT INTO game (
    id,
    code,
    name,
    status,
    entry_path,
    starts_at,
    ends_at,
    created_at,
    updated_at
) VALUES (
    '11111111-1111-1111-1111-111111111111',
    'farm-quest-mvp',
    'Farm Quest MVP',
    'ACTIVE',
    '/play',
    CURRENT_TIMESTAMP,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO quest (
    id,
    game_id,
    code,
    title,
    description,
    sort_order,
    status,
    created_at,
    updated_at
) VALUES (
    '22222222-2222-2222-2222-222222222222',
    '11111111-1111-1111-1111-111111111111',
    'tea-riddle-01',
    '茶園謎題',
    '作為階段二最小可用資料的示範任務。',
    1,
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO location (
    id,
    quest_id,
    name,
    latitude,
    longitude,
    radius_meters,
    max_accuracy_meters,
    hint_text,
    status,
    sort_order,
    is_primary,
    created_at,
    updated_at
) VALUES (
    '33333333-3333-3333-3333-333333333333',
    '22222222-2222-2222-2222-222222222222',
    '主要茶園定位點',
    24.147736,
    120.673648,
    30,
    50,
    '請前往茶園入口附近。',
    'ACTIVE',
    1,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO merchant (
    id,
    game_id,
    code,
    name,
    description,
    address,
    contact_name,
    contact_phone,
    status,
    created_at,
    updated_at
) VALUES (
    '44444444-4444-4444-4444-444444444444',
    '11111111-1111-1111-1111-111111111111',
    'merchant-tea-house',
    '茶香合作店家',
    '提供完成任務後的優惠券兌換。',
    '台中市示範路 1 號',
    '林小茶',
    '04-12345678',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO coupon_campaign (
    id,
    game_id,
    quest_id,
    merchant_id,
    code,
    title,
    description,
    status,
    created_at,
    updated_at
) VALUES (
    '55555555-5555-5555-5555-555555555555',
    '11111111-1111-1111-1111-111111111111',
    '22222222-2222-2222-2222-222222222222',
    '44444444-4444-4444-4444-444444444444',
    'tea-coupon-01',
    '茶香折扣券',
    '完成茶園謎題後可取得的示範優惠券活動。',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO knowledge_document (
    id,
    game_id,
    quest_id,
    location_id,
    title,
    source,
    spoiler_level,
    version,
    embedding_status,
    indexed_at,
    created_at,
    updated_at
) VALUES (
    '66666666-6666-6666-6666-666666666666',
    '11111111-1111-1111-1111-111111111111',
    '22222222-2222-2222-2222-222222222222',
    '33333333-3333-3333-3333-333333333333',
    '茶園任務知識文件',
    'seed://knowledge/tea-riddle-01.md',
    'LOW',
    1,
    'PENDING',
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
