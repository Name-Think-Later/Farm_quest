# ===== DOCS/coupon-campaign.md =====
# coupon-campaign（優惠券活動）資料需求

## 來源
- `ADMIN_TEST_DATA_FORMAT.md`：`## coupon-campaign`

## API
- `POST /api/admin/coupon-campaigns`

## request body
```json
{
  "questId": "11111111-1111-1111-1111-111111111111",
  "merchantCode": "merchant-tea-house",
  "code": "tea-coupon-01",
  "title": "春茶購物折價券",
  "description": "完成任務後可於指定商家購買茶葉或伴手禮時折抵。",
  "status": "ACTIVE",
  "validFrom": "2026-06-10T09:00:00+08:00",
  "validUntil": "2026-06-30T18:00:00+08:00"
}
```

## 欄位規格
- `questId`: UUID，必填
- `merchantCode`: string，必填，不可空白，需對應既有 merchant
- `code`: string，必填，不可空白，max 64，同一 game 內唯一
- `title`: string，必填，不可空白，max 255
- `description`: string，可留空
- `status`: `DRAFT` / `ACTIVE` / `INACTIVE` / `EXPIRED`，未傳入預設 `DRAFT`
- `validFrom`, `validUntil`: ISO-8601，若皆提供，`validUntil >= validFrom`

## 優惠設定原則
- 優惠僅限購物折價或購物禮金。
- 不使用任何非購物型優惠。
- `merchantCode` 為可參考商家類型代碼，實際需對應系統內 merchant。

## 五景點對應 coupon-campaign

```json
[
  {
    "questId": "11111111-1111-1111-1111-111111111111",
    "merchantCode": "merchant-lugu-tea-shop",
    "code": "coupon-bridge-tea-01",
    "title": "鹿谷茶葉購物折價券",
    "description": "可於參考商家購買鹿谷茶葉、茶包或茶點伴手禮時折抵新台幣50元。",
    "status": "ACTIVE",
    "validFrom": "2026-06-01T00:00:00+08:00",
    "validUntil": "2026-12-31T23:59:59+08:00"
  },
  {
    "questId": "22222222-2222-2222-2222-222222222222",
    "merchantCode": "merchant-flower-souvenir",
    "code": "coupon-shima-souvenir-01",
    "title": "小半天伴手禮購物金",
    "description": "可於參考商家購買櫻花主題明信片、農產伴手禮或地方紀念品時折抵新台幣50元。",
    "status": "ACTIVE",
    "validFrom": "2026-06-01T00:00:00+08:00",
    "validUntil": "2026-12-31T23:59:59+08:00"
  },
  {
    "questId": "33333333-3333-3333-3333-333333333333",
    "merchantCode": "merchant-bamboo-craft-shop",
    "code": "coupon-bamboo-craft-01",
    "title": "竹藝商品購物折價券",
    "description": "可於參考商家購買竹編小物、竹製生活用品或地方工藝商品時享購物折抵新台幣80元。",
    "status": "ACTIVE",
    "validFrom": "2026-06-01T00:00:00+08:00",
    "validUntil": "2026-12-31T23:59:59+08:00"
  },
  {
    "questId": "44444444-4444-4444-4444-444444444444",
    "merchantCode": "merchant-trail-farm-goods",
    "code": "coupon-heritage-farm-01",
    "title": "小半天農產購物禮金",
    "description": "可於參考商家購買竹筍加工品、茶葉、果乾或步道周邊農產伴手禮時折抵新台幣50元。",
    "status": "ACTIVE",
    "validFrom": "2026-06-01T00:00:00+08:00",
    "validUntil": "2026-12-31T23:59:59+08:00"
  },
  {
    "questId": "55555555-5555-5555-5555-555555555555",
    "merchantCode": "merchant-charcoal-products",
    "code": "coupon-wuxiu-charcoal-01",
    "title": "竹炭商品購物折價券",
    "description": "可於參考商家購買竹炭商品、竹炭食品、竹炭清潔用品或茶葉伴手禮時折抵新台幣100元。",
    "status": "ACTIVE",
    "validFrom": "2026-06-01T00:00:00+08:00",
    "validUntil": "2026-12-31T23:59:59+08:00"
  }
]
```

## 參考商家方向
- 小半天高架橋：鹿谷茶葉店、沿線農特產商店、茶點伴手禮商家。
- 小半天石馬公園：小半天伴手禮店、花季紀念品、農產禮盒商家。
- 小半天竹藝工坊：竹藝商品店、竹編小物、竹製生活用品商家。
- 林爽文古戰場／孟宗竹林古戰場：步道周邊農產店、茶葉與竹筍加工品商家。
- 武岫竹炭窯文化園區／竹炭窯文化園區：竹炭商品、竹炭食品、茶葉與竹林文創商品商家。

> `merchantCode`、`questId` 需替換為系統內實際值。

