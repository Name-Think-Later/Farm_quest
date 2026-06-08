# Farm Quest Frontend

這個目錄是 `Farm Quest` 的前端子專案，對應目前的**階段 1 與階段 2 前端任務**。

目前目標不是做正式視覺稿，而是先建立一個可執行、可驗證、可往後擴充的前端骨架，包含：

- 手機優先的頁面流程
- 基本 route / layout
- API client 與 health check 基礎
- loading / error / offline 狀態提示
- 以 mock API 串接階段 1 的主要遊客流程

## 目前範圍

本專案目前只處理前端：

- 不修改後端 API 實作
- 不做店家端頁面
- 不做後台 UI
- 不做正式視覺完稿
- 不在前端自行判定 OTP、GPS、答題正確與否、優惠券有效性

這些業務判定在目前設計上都由 API 回傳狀態決定；前端只負責呈現、導頁與送出資料。

## 技術棧

- React 18
- TypeScript
- Vite
- React Router
- TanStack Query
- Zustand
- Vitest
- Playwright（目前用於本地驗證流程）

## 目錄重點

```text
frontend/
├─ src/
│  ├─ app/              # App provider / router
│  ├─ components/       # 共用 UI 元件
│  ├─ features/         # domain logic、API、mock、state
│  ├─ lib/              # env、http client、network hook
│  ├─ pages/            # route-level pages
│  ├─ styles/           # global styles / tokens
│  └─ tests/            # 前端測試骨架
├─ README.md
├─ 實作回報.md
└─ package.json
```

## 路由

目前已建立的頁面路由：

- `/`：遊戲入口頁
- `/app`：前端骨架索引頁
- `/auth/email`：Email 登入頁
- `/auth/otp`：OTP 驗證頁
- `/quest/current`：目前任務頁
- `/quest/location`：地點驗證頁
- `/quest/riddle`：AI 對話猜謎頁
- `/coupons/current`：優惠券頁

其中 `/quest/*` 與 `/coupons/current` 目前有基本登入保護；未登入直接進入時，會被導回 `/auth/email`。

## 環境變數

本專案使用 Vite 環境變數：

- `VITE_PUBLIC_API_BASE_URL`：後端 API base URL，預設為 `http://localhost:8080`
- `VITE_USE_MOCK_API`：是否使用 mock API，預設為 `true`

可以建立 `.env.local`：

```env
VITE_PUBLIC_API_BASE_URL=http://localhost:8080
VITE_USE_MOCK_API=true
```

### 目前建議

如果你只是要本地看流程，先維持：

```env
VITE_USE_MOCK_API=true
```

如果你要開始接真實後端，再改成：

```env
VITE_USE_MOCK_API=false
```

## Mock 規則

目前為了先驗證前端流程，部分行為使用 mock：

- OTP
  - `123456`：驗證成功
  - `000000`：驗證碼過期
- GPS
  - 目前先拔掉手動輸入 `accuracyMeters` 的模擬
  - 在 mock 流程中，使用者按下「已抵達，前往下一步」後直接確認通過
  - 正式串接後再接入真實 GPS 驗證
- AI 猜謎
  - 對話畫面採左右氣泡樣式，類似即時通訊軟體
  - 內容包含 `提示`：回傳提示訊息
  - 內容包含 `高山茶`：完成任務並取得優惠券
- Health check
  - 目前預設走 mock health 狀態

## 安裝

在 `project/frontend` 目錄內執行：

```bash
npm install
```

## 啟動開發環境

```bash
npm run dev
```

預設會啟動 Vite 開發伺服器。

## 可用指令

```bash
npm run dev        # 啟動開發伺服器
npm run check      # TypeScript 檢查
npm run test       # 執行 Vitest
npm run build      # 產生正式版建置
npm run preview    # 預覽 build 結果
```

## 目前已驗證結果

這個前端骨架已完成一次本地驗證，包含：

- `npm install`
- `npm run check`
- `npm run test`
- `npm run build`
- `npm run dev`
- 實際走過主要頁面流程：
  - 入口頁
  - Email 登入
  - OTP 驗證
  - 任務頁
  - GPS 驗證
  - AI 猜謎
  - 優惠券頁
- 驗證受保護路由未登入時會被導回 `/auth/email`

詳細紀錄請看：[實作回報.md](./實作回報.md)

## 下一步建議

如果要從骨架進到可用版本，建議依序做：

1. 把 mock API 逐步改成真實 `/api/**` 串接
2. 補上更有意義的畫面與流程測試
3. 補齊 health check 真實後端連線驗證
4. 根據後續階段需求，逐步提升 UI 完整度與錯誤處理細節

## 相關文件

- [實作回報.md](./實作回報.md)
