# Admin 基礎測試資料格式

以下整理目前 admin 基礎測試常用資料的格式。若程式或資料庫沒有明確格式限制，則標示為「無」。

## quest

來源：
- `src/main/java/nutc/sot/farm_quest/dto/admin/AdminQuestRequest.java`
- `src/main/java/nutc/sot/farm_quest/service/admin/AdminQuestService.java`
- `src/main/resources/db/migration/V1__init_stage2_schema.sql`

### 用途
- 用來建立一個關卡 / 任務的基本外殼。
- 玩家在前台看到的任務名稱、任務說明、排序主要來自這份資料。

### 代表的內容
- 代表「這一題 / 這一關是什麼」。
- 它描述的是關卡本身，不包含 AI 出題規則、GPS 驗證點細節或背景知識全文。

### 建立 / 更新 API 格式
`POST /api/admin/quests`
`PUT /api/admin/quests/{questId}`

```json
{
  "code": "tea-riddle-01",
  "title": "茶園解謎任務",
  "description": "在茶園中完成提示並找出答案",
  "sortOrder": 1,
  "status": "ACTIVE"
}
```

### 欄位格式
- `code`: 字串，必填，不可空白
  - 最大長度：64
  - 唯一限制：同一個 game 內不可重複
- `title`: 字串，必填，不可空白
  - 最大長度：255
- `description`: 字串，必填，不可空白
  - 格式限制：無
- `sortOrder`: 整數，必填
  - 唯一限制：同一個 game 內不可重複
- `status`: 字串，必填，不可空白
  - 可接受值：`DRAFT`、`ACTIVE`、`DISABLED`
  - 會被轉成大寫後驗證

---

## ai-riddle-config

來源：
- `src/main/java/nutc/sot/farm_quest/dto/admin/AdminAiRiddleConfigRequest.java`
- `src/main/java/nutc/sot/farm_quest/service/admin/AdminAiRiddleService.java`
- `src/main/java/nutc/sot/farm_quest/service/quest/DefaultPromptPolicyService.java`

### 用途
- 用來設定這一關的 AI 題目規則。
- 系統會把這份資料和 quest 一起組成 AI prompt，決定要怎麼出題、怎麼提示、怎麼判定是否答對。

### 代表的內容
- 代表「這題怎麼玩、怎麼算對、怎麼算完成」。
- 它是題目規則本體，不是任務標題，也不是背景知識文件。

### 建立 / 更新 API 格式
`PUT /api/admin/ai-riddles/{questId}`

```json
{
  "riddlePrompt": "你要引導玩家觀察茶園入口的線索，逐步提示，不要直接公布答案。",
  "answerCriteria": "製茶師",
  "spoilerPolicy": "只能給方向性提示，不可直接說出答案，也不可逐字重述知識文件中的關鍵句。",
  "completionPolicy": "當玩家明確回答出正確答案時才算完成，模糊接近答案時只給進一步提示。",
  "status": "ACTIVE"
}
```

### 欄位格式
- `questId`: UUID，必填
  - 由 API path 傳入，不在 request body 內
  - 必須對應到既有 quest
- `riddlePrompt`: 字串，必填，不可空白
  - 格式限制：無
- `answerCriteria`: 字串，必填，不可空白
  - 格式限制：無
- `spoilerPolicy`: 字串，必填，不可空白
  - 格式限制：無
- `completionPolicy`: 字串，必填，不可空白
  - 格式限制：無
- `status`: 字串，必填，不可空白
  - 可接受值：`ACTIVE`、`INACTIVE`
  - 會被轉成大寫後驗證

### 這份資料實際對應的內容
- `riddlePrompt`: 這題要怎麼出題、怎麼引導玩家
- `answerCriteria`: 什麼算答對
- `spoilerPolicy`: 可以提示到什麼程度
- `completionPolicy`: 什麼情況算過關

### 備註
- 這份資料是「題目規則」本體，不是背景知識。
- 系統會把 `quest.title` 與這份設定一起組成 AI 題目 prompt。
- 若沒有對應的 ai-riddle-config，該 quest 就不會有完整的 AI 謎題設定。

---

## location

來源：
- `src/main/java/nutc/sot/farm_quest/persistence/entity/LocationEntity.java`
- `src/main/java/nutc/sot/farm_quest/dto/admin/AdminLocationHotspotRequest.java`
- `src/main/resources/db/migration/V1__init_stage2_schema.sql`

### 用途
- 用來設定這一關的實體地點與 GPS 驗證範圍。
- 玩家到達現場後，系統會用這份資料驗證位置，並提供地點提示。

### 代表的內容
- 代表「這一關要去哪裡」與「到哪裡算抵達」。
- 它描述的是地點、座標、半徑與提示，不是題目規則本身。

### 目前 admin 可修改的格式
`PUT /api/admin/locations/{locationId}/hotspot`

```json
{
  "latitude": 24.146672,
  "longitude": 120.673648,
  "radiusMeters": 30,
  "maxAccuracyMeters": 50,
  "hintText": "請靠近主要茶園入口"
}
```

### 欄位格式
- `questId`: UUID，必填
  - 來源於資料關聯，不在 hotspot API body 內
- `name`: 字串，必填
  - 最大長度：255
- `latitude`: 數字，必填
  - 範圍：`-90` ~ `90`
  - DB 格式：`NUMERIC(9,6)`
- `longitude`: 數字，必填
  - 範圍：`-180` ~ `180`
  - DB 格式：`NUMERIC(9,6)`
- `radiusMeters`: 整數，必填
  - 限制：`> 0`
- `maxAccuracyMeters`: 整數，必填
  - 限制：`> 0`
- `hintText`: 字串
  - 格式限制：無
- `status`: 字串，必填
  - 可接受值：`ACTIVE`、`INACTIVE`
- `sortOrder`: 整數，必填
  - 唯一限制：同一個 quest 內不可重複
- `isPrimary`: 布林，必填
  - 格式限制：無

### 備註
- 目前 admin API 只提供 hotspot 更新，沒有直接提供建立 location 的 API。
- 如果只是問欄位格式，上述欄位即為 location 資料格式。

---

## coupon-campaign

來源：
- `src/main/java/nutc/sot/farm_quest/dto/admin/AdminCouponCampaignRequest.java`
- `src/main/java/nutc/sot/farm_quest/service/admin/AdminCouponService.java`
- `src/main/java/nutc/sot/farm_quest/persistence/entity/CouponCampaignEntity.java`
- `src/main/resources/db/migration/V1__init_stage2_schema.sql`
- `src/main/resources/db/migration/V3__stage6_coupon_campaign_validity.sql`

### 用途
- 用來設定玩家完成某一關後可對應的優惠券活動。
- 系統會依這份資料決定獎勵券的活動代碼、名稱、有效期間與可用狀態。

### 代表的內容
- 代表「這一關完成後，會對應到什麼優惠券活動」。
- 它是優惠活動規則，不是玩家實際持有的那張 coupon。

### 建立 API 格式
`POST /api/admin/coupon-campaigns`

```json
{
  "questId": "11111111-1111-1111-1111-111111111111",
  "merchantCode": "merchant-tea-house",
  "code": "tea-coupon-01",
  "title": "春茶兌換券",
  "description": "完成任務後可兌換一杯茶",
  "status": "ACTIVE",
  "validFrom": "2026-06-10T09:00:00+08:00",
  "validUntil": "2026-06-30T18:00:00+08:00"
}
```

### 欄位格式
- `questId`: UUID，必填
- `merchantCode`: 字串，必填，不可空白
  - 需能對應到系統內既有 merchant 的 `code`
- `code`: 字串，必填，不可空白
  - 最大長度：64
  - 唯一限制：同一個 game 內不可重複
- `title`: 字串，必填，不可空白
  - 最大長度：255
- `description`: 字串
  - 格式限制：無
- `status`: 字串
  - 可接受值：`DRAFT`、`ACTIVE`、`INACTIVE`、`EXPIRED`
  - 若未提供，系統預設為 `DRAFT`
- `validFrom`: 時間
  - 格式：ISO-8601 `OffsetDateTime`
- `validUntil`: 時間
  - 格式：ISO-8601 `OffsetDateTime`

### 關聯與資料限制
- `questId` 必須存在，且屬於目前 game。
- `merchantCode` 必須存在，且屬於目前 game。
- 同一個 `quest` 只能綁一筆 coupon campaign。
- 若同時提供 `validFrom` 與 `validUntil`，則 `validUntil` 不可早於 `validFrom`。

---

## knowledge document

來源：
- `src/main/java/nutc/sot/farm_quest/dto/admin/KnowledgeDocumentRequest.java`
- `src/main/java/nutc/sot/farm_quest/service/admin/AdminKnowledgeService.java`
- `src/main/java/nutc/sot/farm_quest/persistence/entity/KnowledgeDocumentEntity.java`
- `src/main/resources/db/migration/V1__init_stage2_schema.sql`
- `src/main/resources/db/migration/V4__stage7_knowledge_document_content.sql`

### 用途
- 用來提供 AI 解謎時可檢索的背景知識與補充內容。
- 系統會把這些文件做索引，讓 AI 在互動過程中拿來當提示與回答依據。

### 代表的內容
- 代表「這一題相關的背景資訊、場景知識、補充說明」。
- 它不是題目規則本身，而是支援題目互動的知識來源。

### 建立 API 格式
`POST /api/admin/knowledge-documents`

```json
{
  "title": "茶園背景知識",
  "content": "本茶園主要種植青心烏龍，入口區域設有導覽牌。",
  "source": "admin-seed",
  "questId": "11111111-1111-1111-1111-111111111111",
  "locationId": "22222222-2222-2222-2222-222222222222",
  "spoilerLevel": "GENERAL"
}
```

### 欄位格式
- `title`: 字串，必填，不可空白
  - 最大長度：255
- `content`: 字串，必填，不可空白
  - 格式限制：無
- `source`: 字串，必填，不可空白
  - 最大長度：255
- `questId`: UUID
  - 格式限制：無
- `locationId`: UUID
  - 格式限制：無
- `spoilerLevel`: 字串，必填，不可空白
  - 可接受值限制：無

### 關聯限制
- `questId` 與 `locationId` 都可不填。
- 若只填 `locationId`，系統會自動帶入該 location 所屬的 `quest`。
- 若 `questId` 與 `locationId` 同時有值，則 `locationId` 必須屬於該 `questId`，否則會報錯。

### 系統自動產生欄位
以下欄位不是建立 request 必填，但資料表中會存在：
- `version`: 整數
  - 系統自動決定；同 location 或同 quest 會往上累加
- `embeddingStatus`: 字串
  - 建立時固定先為 `PENDING`
  - DB 可見值：`PENDING`、`INDEXED`、`FAILED`、`ARCHIVED`
- `indexedAt`: 時間
  - 建立當下為 `null`

---

## 補充
若你要拿這份文件做「admin 基礎測試資料準備」，可以先準備：
- 1 筆 quest
- 1 筆 ai-riddle-config
- 1 筆 location
- 1 筆 coupon-campaign
- 1 筆 knowledge document

其中：
- `ai-riddle-config` 要對到該 `quest`
- `location.questId` 要對到該 `quest`
- `coupon-campaign.questId` 要對到該 `quest`
- `coupon-campaign.merchantCode` 要對到系統內既有 merchant
- `knowledge document` 若填 `locationId`，最好也對到同一個 `quest`
