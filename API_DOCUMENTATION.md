# Farm Quest API 文件

此文件整理目前後端已提供的 API，作為前端串接參考。

- Base URL：`/api`
- 成功回應格式：`{ "success": true, "data": ... }`
- 錯誤回應格式：`{ "code": "...", "message": "...", "timestamp": "...", "path": "..." }`

## 1. 共用規則

### 1.1 Header

#### 訪客登入後 API
需要帶 Bearer Token：

```http
Authorization: Bearer <sessionToken>
```

適用於：
- `GET /api/auth/visitor/session`
- `GET /api/auth/me`
- `POST /api/auth/logout`
- `GET /api/game/state`
- `GET /api/quests`
- `GET /api/quests/current`
- `POST /api/quests/{questId}/start`
- `GET /api/quests/{questId}/location-hint`
- `POST /api/quests/{questId}/location-verifications`
- `GET /api/quests/{questId}/ai-riddle/messages`
- `POST /api/quests/{questId}/ai-riddle/messages`
- `GET /api/coupons/my`
- `GET /api/coupons/{couponId}`
- `POST /api/coupons/{couponId}/consume`

#### 後台管理 API
也使用 Bearer Token，但 token 內容是後台管理密鑰，不是訪客 session token：

```http
Authorization: Bearer <adminSecret>
```

適用於所有 `/api/admin/**`。

### 1.2 成功回應格式

```json
{
  "success": true,
  "data": {}
}
```

### 1.3 錯誤回應格式

```json
{
  "code": "SESSION_INVALID",
  "message": "Session token is invalid",
  "timestamp": "2026-06-10T12:34:56+08:00",
  "path": "/api/auth/me"
}
```

### 1.4 常見錯誤代碼

#### Auth 類
- `INVALID_EMAIL`
- `EMAIL_VERIFICATION_EXPIRED`
- `EMAIL_VERIFICATION_INVALID`
- `EMAIL_VERIFICATION_RATE_LIMITED`
- `SESSION_EXPIRED`
- `SESSION_INVALID`
- `ADMIN_UNAUTHORIZED`
- `ADMIN_SECRET_NOT_CONFIGURED`

#### Quest / Coupon / Admin 類
- `GAME_NOT_FOUND`
- `QUEST_NOT_FOUND`
- `QUEST_NOT_AVAILABLE`
- `QUEST_NOT_STARTED`
- `LOCATION_REQUIRED`
- `LOCATION_NOT_FOUND`
- `LOCATION_ACCURACY_TOO_LOW`
- `LOCATION_TOO_FAR`
- `GPS_PERMISSION_REQUIRED`
- `AI_RIDDLE_NOT_AVAILABLE`
- `AI_RIDDLE_MESSAGE_EMPTY`
- `AI_RIDDLE_ALREADY_COMPLETED`
- `QUEST_LOCATION_NOT_VERIFIED`
- `AI_PROVIDER_UNAVAILABLE`
- `RAG_RETRIEVAL_FAILED`
- `COUPON_NOT_FOUND`
- `COUPON_NOT_OWNED`
- `COUPON_EXPIRED`
- `COUPON_ALREADY_CONSUMED`
- `COUPON_NOT_AVAILABLE`
- `COUPON_DUPLICATE_ISSUE`
- `MERCHANT_NOT_FOUND`
- `KNOWLEDGE_DOCUMENT_NOT_FOUND`
- `ADMIN_INVALID_REQUEST`
- `ADMIN_RESOURCE_CONFLICT`

### 1.5 前端流程常用欄位

#### progress status
常見值：
- `NOT_STARTED`
- `STARTED`
- `LOCATION_VERIFIED`
- `AI_RIDDLE_STARTED`
- `COMPLETED`

#### nextStep
目前後端會回傳：
- `START_QUEST`
- `VERIFY_LOCATION`
- `AI_RIDDLE_AVAILABLE`

可用於前端決定目前顯示的主要操作。

---

## 2. Auth API

### 2.1 發送 Email 驗證碼
`POST /api/auth/visitor/email-verifications`

#### Request Body
```json
{
  "email": "user@example.com"
}
```

#### Request 欄位
| 欄位 | 型別 | 必填 | 說明 |
|---|---|---:|---|
| email | string | Y | Email，需符合格式 |

#### Response Data
```json
{
  "email": "user@example.com",
  "expiresAt": "2026-06-10T12:34:56+08:00",
  "resendAvailableAt": "2026-06-10T12:35:30+08:00",
  "status": "PENDING"
}
```

| 欄位 | 型別 | 說明 |
|---|---|---|
| email | string | 正規化後 email |
| expiresAt | string(datetime) | 驗證碼到期時間 |
| resendAvailableAt | string(datetime) | 可再次發送時間 |
| status | string | 驗證狀態 |

### 2.2 確認 Email 驗證碼並建立 Session
`POST /api/auth/visitor/email-verifications/confirm`

#### Request Body
```json
{
  "email": "user@example.com",
  "otp": "123456"
}
```

| 欄位 | 型別 | 必填 | 說明 |
|---|---|---:|---|
| email | string | Y | Email |
| otp | string | Y | 6 位數驗證碼 |

#### Response Data
```json
{
  "visitorAccountId": "uuid",
  "email": "user@example.com",
  "sessionToken": "token-string",
  "issuedAt": "2026-06-10T12:34:56+08:00",
  "expiresAt": "2026-06-10T20:34:56+08:00",
  "authenticated": true
}
```

| 欄位 | 型別 | 說明 |
|---|---|---|
| visitorAccountId | string(uuid) | 訪客帳號 ID |
| email | string | email |
| sessionToken | string | 前端後續 Bearer Token |
| issuedAt | string(datetime) | session 建立時間 |
| expiresAt | string(datetime) | session 到期時間 |
| authenticated | boolean | 是否已登入 |

### 2.3 取得目前 Session
`GET /api/auth/visitor/session`

#### Header
```http
Authorization: Bearer <sessionToken>
```

#### Response Data
```json
{
  "visitorAccountId": "uuid",
  "email": "user@example.com",
  "sessionToken": null,
  "issuedAt": "2026-06-10T12:34:56+08:00",
  "expiresAt": "2026-06-10T20:34:56+08:00",
  "authenticated": true
}
```

> 注意：此 API 回傳的 `sessionToken` 目前為 `null`。

### 2.4 取得目前登入使用者
`GET /api/auth/me`

#### Header
```http
Authorization: Bearer <sessionToken>
```

#### Response Data
```json
{
  "authenticated": true,
  "visitorAccountId": "uuid",
  "email": "user@example.com",
  "sessionExpiresAt": "2026-06-10T20:34:56+08:00"
}
```

### 2.5 登出
`POST /api/auth/logout`

#### Header
```http
Authorization: Bearer <sessionToken>
```

#### Response Data
```json
{
  "success": true
}
```

---

## 3. Game API

### 3.1 取得活動中的遊戲資訊
`GET /api/game`

#### Response Data
```json
{
  "gameId": "uuid",
  "code": "farm-quest-mvp",
  "name": "Farm Quest",
  "entryPath": "/game",
  "startsAt": "2026-06-10T09:00:00+08:00",
  "endsAt": "2026-06-30T18:00:00+08:00"
}
```

### 3.2 取得目前遊戲進度
`GET /api/game/state`

#### Header
```http
Authorization: Bearer <sessionToken>
```

#### Response Data
```json
{
  "gameId": "uuid",
  "visitorAccountId": "uuid",
  "currentQuestId": "uuid",
  "currentQuestTitle": "第一關任務",
  "progressStatus": "STARTED",
  "gpsVerified": false,
  "aiRiddleAvailable": false,
  "nextStep": "VERIFY_LOCATION"
}
```

| 欄位 | 型別 | 說明 |
|---|---|---|
| progressStatus | string | 任務進度狀態 |
| gpsVerified | boolean | 是否已完成 GPS 驗證 |
| aiRiddleAvailable | boolean | 是否可進入 AI 猜謎 |
| nextStep | string | 前端建議下一步 |

---

## 4. Quest API

### 4.1 取得任務列表
`GET /api/quests`

#### Header
```http
Authorization: Bearer <sessionToken>
```

#### Response Data
```json
{
  "quests": [
    {
      "questId": "uuid",
      "title": "第一關任務",
      "description": "任務描述",
      "sortOrder": 1,
      "status": "NOT_STARTED",
      "startedAt": null,
      "locationVerifiedAt": null,
      "current": true,
      "nextStep": "START_QUEST"
    }
  ]
}
```

### 4.2 取得目前應進行的任務
`GET /api/quests/current`

#### Header
```http
Authorization: Bearer <sessionToken>
```

#### Response Data
與 `QuestDetailResponse` 相同：

```json
{
  "questId": "uuid",
  "title": "第一關任務",
  "description": "任務描述",
  "sortOrder": 1,
  "status": "STARTED",
  "startedAt": "2026-06-10T12:34:56+08:00",
  "locationVerifiedAt": null,
  "current": true,
  "nextStep": "VERIFY_LOCATION"
}
```

### 4.3 開始任務
`POST /api/quests/{questId}/start`

#### Path Params
| 參數 | 型別 | 說明 |
|---|---|---|
| questId | string(uuid) | 任務 ID |

#### Header
```http
Authorization: Bearer <sessionToken>
```

#### Response Data
```json
{
  "questId": "uuid",
  "status": "STARTED",
  "startedAt": "2026-06-10T12:34:56+08:00",
  "nextStep": "VERIFY_LOCATION"
}
```

### 4.4 取得 GPS 提示資訊
`GET /api/quests/{questId}/location-hint`

#### Header
```http
Authorization: Bearer <sessionToken>
```

#### Response Data
```json
{
  "questId": "uuid",
  "locationId": "uuid",
  "locationName": "景點名稱",
  "hintText": "請前往溫室旁邊",
  "radiusMeters": 30,
  "maxAccuracyMeters": 50
}
```

### 4.5 驗證 GPS 位置
`POST /api/quests/{questId}/location-verifications`

#### Header
```http
Authorization: Bearer <sessionToken>
```

#### Request Body
```json
{
  "permissionDenied": false,
  "latitude": 24.123456,
  "longitude": 120.123456,
  "accuracyMeters": 15
}
```

| 欄位 | 型別 | 必填 | 說明 |
|---|---|---:|---|
| permissionDenied | boolean | Y | 使用者是否拒絕定位權限 |
| latitude | number | N | 緯度，範圍 -90 ~ 90 |
| longitude | number | N | 經度，範圍 -180 ~ 180 |
| accuracyMeters | number | N | 定位精度，必須 >= 0 |

#### Response Data
```json
{
  "questId": "uuid",
  "status": "LOCATION_VERIFIED",
  "passed": true,
  "distanceMeters": 8.5,
  "accuracyMeters": 15,
  "locationVerifiedAt": "2026-06-10T12:40:00+08:00",
  "nextStep": "AI_RIDDLE_AVAILABLE"
}
```

### 4.6 取得 AI 猜謎對話紀錄
`GET /api/quests/{questId}/ai-riddle/messages`

#### Header
```http
Authorization: Bearer <sessionToken>
```

#### Response Data
```json
{
  "questId": "uuid",
  "conversationId": "uuid",
  "status": "AI_RIDDLE_STARTED",
  "questCompleted": false,
  "nextStep": "AI_RIDDLE_AVAILABLE",
  "messages": [
    {
      "messageId": "uuid",
      "role": "VISITOR",
      "content": "我的答案是...",
      "answerCorrect": null,
      "createdAt": "2026-06-10T12:41:00+08:00"
    },
    {
      "messageId": "uuid",
      "role": "ASSISTANT",
      "content": "請再想想看。",
      "answerCorrect": false,
      "createdAt": "2026-06-10T12:41:05+08:00"
    }
  ]
}
```

| 欄位 | 型別 | 說明 |
|---|---|---|
| status | string | 任務進度狀態 |
| questCompleted | boolean | 該任務是否完成 |
| nextStep | string | 前端建議下一步 |
| messages[].role | string | `VISITOR` 或 `ASSISTANT` |
| messages[].answerCorrect | boolean/null | 助手訊息會帶判定結果，使用者訊息可能為 null |

### 4.7 傳送 AI 猜謎訊息
`POST /api/quests/{questId}/ai-riddle/messages`

#### Header
```http
Authorization: Bearer <sessionToken>
```

#### Request Body
```json
{
  "content": "我猜答案是茶葉"
}
```

| 欄位 | 型別 | 必填 | 說明 |
|---|---|---:|---|
| content | string | Y | 玩家輸入內容 |

#### Response Data
```json
{
  "questId": "uuid",
  "conversationId": "uuid",
  "status": "AI_RIDDLE_STARTED",
  "replyContent": "方向接近了，再補充用途。",
  "correct": false,
  "questCompleted": false,
  "nextStep": "AI_RIDDLE_AVAILABLE",
  "safeMessage": "請依照線索繼續作答。"
}
```

| 欄位 | 型別 | 說明 |
|---|---|---|
| replyContent | string | AI 回覆內容 |
| correct | boolean | 本次答案是否正確 |
| questCompleted | boolean | 任務是否完成 |
| safeMessage | string/null | 錯誤答案時給前端的安全提示 |

---

## 5. Coupon API

### 5.1 取得我的優惠券列表
`GET /api/coupons/my`

#### Header
```http
Authorization: Bearer <sessionToken>
```

#### Response Data
```json
{
  "coupons": [
    {
      "couponId": "uuid",
      "questId": "uuid",
      "couponCampaignId": "uuid",
      "merchantId": "uuid",
      "title": "飲品折扣券",
      "merchantName": "合作店家",
      "status": "ISSUED",
      "displayCode": "ABC123",
      "issuedAt": "2026-06-10T12:50:00+08:00",
      "expiresAt": "2026-06-20T23:59:59+08:00",
      "consumedAt": null
    }
  ]
}
```

### 5.2 取得優惠券詳情
`GET /api/coupons/{couponId}`

#### Header
```http
Authorization: Bearer <sessionToken>
```

#### Response Data
```json
{
  "couponId": "uuid",
  "questId": "uuid",
  "couponCampaignId": "uuid",
  "merchantId": "uuid",
  "title": "飲品折扣券",
  "description": "可折抵 20 元",
  "merchantName": "合作店家",
  "merchantAddress": "台中市...",
  "status": "ISSUED",
  "displayCode": "ABC123",
  "issuedAt": "2026-06-10T12:50:00+08:00",
  "expiresAt": "2026-06-20T23:59:59+08:00",
  "consumedAt": null
}
```

### 5.3 使用優惠券
`POST /api/coupons/{couponId}/consume`

#### Header
```http
Authorization: Bearer <sessionToken>
```

#### Request Body
可不帶 body；若前端要補充確認資訊，可傳：

```json
{
  "clientConfirmedAt": "2026-06-10T13:00:00+08:00",
  "metadata": {
    "source": "mobile-app"
  }
}
```

| 欄位 | 型別 | 必填 | 說明 |
|---|---|---:|---|
| clientConfirmedAt | string(datetime) | N | 前端確認使用時間 |
| metadata | object | N | 額外資訊 |

#### Response Data
```json
{
  "couponId": "uuid",
  "couponUsageId": "uuid",
  "status": "CONSUMED",
  "consumedAt": "2026-06-10T13:00:05+08:00"
}
```

---

## 6. Admin API

> 所有 `/api/admin/**` 都需要：
>
> ```http
> Authorization: Bearer <adminSecret>
> ```

### 6.1 任務管理

#### 6.1.1 取得任務列表
`GET /api/admin/quests`

#### Response Data
```json
{
  "quests": [
    {
      "questId": "uuid",
      "code": "quest-1",
      "title": "第一關任務",
      "description": "任務描述",
      "sortOrder": 1,
      "status": "ACTIVE",
      "updatedAt": "2026-06-10T10:00:00+08:00"
    }
  ]
}
```

#### 6.1.2 建立任務
`POST /api/admin/quests`

#### Request Body
```json
{
  "code": "quest-1",
  "title": "第一關任務",
  "description": "任務描述",
  "sortOrder": 1,
  "status": "ACTIVE"
}
```

#### 6.1.3 更新任務
`PUT /api/admin/quests/{questId}`

Request / Response 結構同上。

### 6.2 地點管理

#### 6.2.1 取得地點列表
`GET /api/admin/locations`

#### Response Data
```json
{
  "locations": [
    {
      "locationId": "uuid",
      "questId": "uuid",
      "questTitle": "第一關任務",
      "name": "溫室",
      "latitude": 24.123456,
      "longitude": 120.123456,
      "radiusMeters": 30,
      "maxAccuracyMeters": 50,
      "hintText": "靠近入口處",
      "status": "ACTIVE",
      "sortOrder": 1,
      "primary": true,
      "updatedAt": "2026-06-10T10:00:00+08:00"
    }
  ]
}
```

#### 6.2.2 更新地點 hotspot
`PUT /api/admin/locations/{locationId}/hotspot`

#### Request Body
```json
{
  "latitude": 24.123456,
  "longitude": 120.123456,
  "radiusMeters": 30,
  "maxAccuracyMeters": 50,
  "hintText": "靠近入口處"
}
```

### 6.3 AI 猜謎設定

#### 6.3.1 取得 AI 猜謎設定列表
`GET /api/admin/ai-riddles`

#### Response Data
```json
{
  "aiRiddles": [
    {
      "questId": "uuid",
      "questCode": "quest-1",
      "questTitle": "第一關任務",
      "riddlePrompt": "請描述茶葉特色",
      "answerCriteria": "需提到茶葉與用途",
      "spoilerPolicy": "不要直接說答案",
      "completionPolicy": "答對才完成",
      "status": "ACTIVE",
      "updatedAt": "2026-06-10T10:00:00+08:00"
    }
  ]
}
```

#### 6.3.2 更新指定任務的 AI 猜謎設定
`PUT /api/admin/ai-riddles/{questId}`

#### Request Body
```json
{
  "riddlePrompt": "請描述茶葉特色",
  "answerCriteria": "需提到茶葉與用途",
  "spoilerPolicy": "不要直接說答案",
  "completionPolicy": "答對才完成",
  "status": "ACTIVE"
}
```

### 6.4 優惠券活動管理

#### 6.4.1 取得優惠券活動列表
`GET /api/admin/coupon-campaigns`

#### Response Data
```json
{
  "campaigns": [
    {
      "campaignId": "uuid",
      "questId": "uuid",
      "merchantId": "uuid",
      "merchantName": "合作店家",
      "code": "drink-20",
      "title": "飲品折扣券",
      "description": "可折抵 20 元",
      "status": "ACTIVE",
      "validFrom": "2026-06-10T00:00:00+08:00",
      "validUntil": "2026-06-30T23:59:59+08:00",
      "updatedAt": "2026-06-10T10:00:00+08:00"
    }
  ]
}
```

#### 6.4.2 建立優惠券活動
`POST /api/admin/coupon-campaigns`

#### Request Body
```json
{
  "questId": "uuid",
  "merchantCode": "merchant-a",
  "code": "drink-20",
  "title": "飲品折扣券",
  "description": "可折抵 20 元",
  "status": "ACTIVE",
  "validFrom": "2026-06-10T00:00:00+08:00",
  "validUntil": "2026-06-30T23:59:59+08:00"
}
```

### 6.5 知識文件管理

#### 6.5.1 取得知識文件列表
`GET /api/admin/knowledge-documents`

#### Response Data
```json
{
  "documents": [
    {
      "documentId": "uuid",
      "questId": "uuid",
      "locationId": "uuid",
      "title": "茶葉介紹",
      "source": "manual",
      "spoilerLevel": "SAFE",
      "version": 1,
      "embeddingStatus": "INDEXED",
      "indexedAt": "2026-06-10T10:00:00+08:00",
      "updatedAt": "2026-06-10T10:00:00+08:00"
    }
  ]
}
```

#### 6.5.2 建立知識文件
`POST /api/admin/knowledge-documents`

#### Request Body
```json
{
  "title": "茶葉介紹",
  "content": "文件內容",
  "source": "manual",
  "questId": "uuid",
  "locationId": "uuid",
  "spoilerLevel": "SAFE"
}
```

| 欄位 | 型別 | 必填 | 說明 |
|---|---|---:|---|
| title | string | Y | 文件標題 |
| content | string | Y | 文件內容 |
| source | string | Y | 文件來源 |
| questId | string(uuid) | N | 關聯任務 |
| locationId | string(uuid) | N | 關聯地點 |
| spoilerLevel | string | Y | 劇透等級 |

#### 6.5.3 重新建立知識索引
`POST /api/admin/knowledge-documents/reindex`

#### Request Body
可不帶 body。

```json
{
  "fullRebuild": false,
  "mode": "TARGETED"
}
```

| 欄位 | 型別 | 必填 | 說明 |
|---|---|---:|---|
| fullRebuild | boolean | N | 是否完整重建 |
| mode | string | N | 重建模式 |

#### Response Data
```json
{
  "accepted": true,
  "queuedDocumentCount": 5,
  "status": "REINDEX_QUEUED",
  "requestedMode": "PENDING_OR_FAILED",
  "effectiveMode": "PENDING",
  "pendingDocumentCount": 5,
  "failedDocumentCount": 0
}
```

### 6.6 優惠券與使用紀錄查詢

#### 6.6.1 取得優惠券列表
`GET /api/admin/coupons`

#### Response Data
```json
{
  "coupons": [
    {
      "couponId": "uuid",
      "visitorAccountId": "uuid",
      "questId": "uuid",
      "couponCampaignId": "uuid",
      "title": "飲品折扣券",
      "status": "ISSUED",
      "displayCode": "ABC123",
      "issuedAt": "2026-06-10T12:50:00+08:00",
      "expiresAt": "2026-06-20T23:59:59+08:00",
      "consumedAt": null
    }
  ]
}
```

#### 6.6.2 取得優惠券使用紀錄
`GET /api/admin/coupon-usages`

#### Response Data
```json
{
  "couponUsages": [
    {
      "couponUsageId": "uuid",
      "couponId": "uuid",
      "visitorAccountId": "uuid",
      "usedAt": "2026-06-10T13:00:05+08:00",
      "clientConfirmedAt": "2026-06-10T13:00:00+08:00",
      "metadata": {
        "source": "mobile-app"
      }
    }
  ]
}
```

### 6.7 統計 API

#### 6.7.1 取得總覽統計
`GET /api/admin/stats/overview`

#### Response Data
```json
{
  "completedQuestCount": 10,
  "issuedCouponCount": 8,
  "usedCouponCount": 5,
  "usageRate": 0.625
}
```

#### 6.7.2 取得任務完成統計
`GET /api/admin/stats/quests`

#### Response Data
```json
{
  "quests": [
    {
      "questId": "uuid",
      "questCode": "quest-1",
      "questTitle": "第一關任務",
      "completedQuestCount": 12
    }
  ]
}
```

#### 6.7.3 取得優惠券活動統計
`GET /api/admin/stats/coupons`

#### Response Data
```json
{
  "campaigns": [
    {
      "campaignId": "uuid",
      "questId": "uuid",
      "campaignCode": "drink-20",
      "campaignTitle": "飲品折扣券",
      "issuedCouponCount": 10,
      "usedCouponCount": 6,
      "usageRate": 0.6
    }
  ]
}
```

---

## 7. System API

### 7.1 健康檢查
`GET /api/system/health`

#### Response Data
```json
{
  "status": "DEGRADED",
  "application": "farm-quest",
  "timestamp": "2026-06-10T12:00:00+08:00",
  "seedDataReady": true
}
```

### 7.2 相依服務狀態
`GET /api/system/dependencies`

#### Response Data
```json
{
  "status": "UP",
  "timestamp": "2026-06-10T12:00:00+08:00",
  "dependencies": [
    {
      "name": "redis",
      "status": "UP",
      "message": "PONG"
    }
  ]
}
```

### 7.3 AI / 外部相依服務探測
`GET /api/system/dependencies/probe`

#### Response Data
格式與 `/api/system/dependencies` 相同。

---

## 8. 串接建議

### 8.1 前端登入流程
1. 呼叫 `POST /api/auth/visitor/email-verifications`
2. 使用者輸入 OTP 後，呼叫 `POST /api/auth/visitor/email-verifications/confirm`
3. 保存 `sessionToken`
4. 後續玩家端 API 一律帶 `Authorization: Bearer <sessionToken>`

### 8.2 任務流程
1. `GET /api/game/state` 或 `GET /api/quests/current` 取得目前狀態
2. 若 `nextStep = START_QUEST`，呼叫 `POST /api/quests/{questId}/start`
3. 若 `nextStep = VERIFY_LOCATION`，先取 `GET /api/quests/{questId}/location-hint`，再送 `POST /api/quests/{questId}/location-verifications`
4. 若 `nextStep = AI_RIDDLE_AVAILABLE`，可使用 AI 猜謎對話 API

### 8.3 狀態判斷建議
- `NOT_STARTED`：尚未開始任務
- `STARTED`：任務已開始，等待 GPS 驗證
- `LOCATION_VERIFIED`：GPS 已通過，可進入 AI 猜謎
- `AI_RIDDLE_STARTED`：AI 猜謎進行中
- `COMPLETED`：任務完成，可能已發券

---

## 9. 參考程式位置

- Auth Controller：`src/main/java/nutc/sot/farm_quest/controller/auth/AuthController.java`
- Game Controller：`src/main/java/nutc/sot/farm_quest/controller/game/GameController.java`
- Quest Controller：`src/main/java/nutc/sot/farm_quest/controller/quest/QuestController.java`
- Coupon Controller：`src/main/java/nutc/sot/farm_quest/controller/coupon/CouponController.java`
- Admin Controller：`src/main/java/nutc/sot/farm_quest/controller/admin/AdminController.java`
- System Controller：`src/main/java/nutc/sot/farm_quest/controller/system/SystemController.java`
- 共用成功回應：`src/main/java/nutc/sot/farm_quest/dto/common/ApiResponse.java`
- 共用錯誤回應：`src/main/java/nutc/sot/farm_quest/exception/ApiErrorResponse.java`
