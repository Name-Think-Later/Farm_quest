# ===== DOCS/README.md =====
# ADMIN_TEST_DATA_FORMAT 資料需求對照（DOCS 概覽）

本目錄彙整 `ADMIN_TEST_DATA_FORMAT.md` 中各類 admin 後台測試資料格式，作為開發、測試與資料補齊的共用參考。

## 來源文件
- `ADMIN_TEST_DATA_FORMAT.md`
- `.omo/notepads/rag-data-collection/knowledge-documents.md`
- `.omo/notepads/rag-data-collection/knowledge-documents.json`

## 已拆分文件清單
- `quest.md`：關卡基本資料（`POST /api/admin/quests`, `PUT /api/admin/quests/{questId}`）
- `ai-riddle-config.md`：AI 謎題規則（`PUT /api/admin/ai-riddles/{questId}`）
- `location.md`：地點與 hotspot 設定（`PUT /api/admin/locations/{locationId}/hotspot`）
- `coupon-campaign.md`：優惠券活動（`POST /api/admin/coupon-campaigns`）
- `knowledge-document.md`：RAG 知識文件（`POST /api/admin/knowledge-documents`）

## 目標景點（共 5 筆）
1. 小半天高架橋
2. 小半天石馬公園
3. 小半天竹藝工坊
4. 林爽文古戰場／孟宗竹林古戰場
5. 武岫竹炭窯文化園區／竹炭窯文化園區

## 共用欄位規則（跨檔）
- 以 `ADMIN_TEST_DATA_FORMAT.md` 為唯一欄位規範來源。
- status 類欄位須符合各 API 的可接受值並以大寫驗證。
- 路徑參數（`questId` / `locationId`）欄位，按實際建立結果替換。
- `title/content`、`source` 需注意 `max` 長度限制。

## 建議實作順序（對應五景點）
1. 先建立 5 筆 `quest`。
2. 逐筆更新該 quest 對應 `location` 的 hotspot。
3. 逐筆更新 `ai-riddle-config`。
4. （有需求）建立 `coupon-campaign`。
5. 建立五筆 `knowledge-document`。

## 五景點示例 ID 對應（示意）
> 以下 ID 僅作文件內對照，實際請改為系統回傳/即時資料。

- 小半天高架橋：`questId=11111111-1111-1111-1111-111111111111`、`locationId=aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa`
- 小半天石馬公園：`questId=22222222-2222-2222-2222-222222222222`、`locationId=bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb`
- 小半天竹藝工坊：`questId=33333333-3333-3333-3333-333333333333`、`locationId=cccccccc-cccc-cccc-cccc-cccccccccccc`
- 林爽文古戰場：`questId=44444444-4444-4444-4444-444444444444`、`locationId=dddddddd-dddd-dddd-dddd-dddddddddddd`
- 武岫竹炭窯文化園區：`questId=55555555-5555-5555-5555-555555555555`、`locationId=eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee`

## 現有現成可匯入檔
- `.omo/notepads/rag-data-collection/knowledge-documents.md`
- `.omo/notepads/rag-data-collection/knowledge-documents.json`
