# ===== DOCS/location.md =====
# location / hotspot（地點）資料需求

## 來源
- `ADMIN_TEST_DATA_FORMAT.md`：`## location`

## API
- `PUT /api/admin/locations/{locationId}/hotspot`

> 提醒：目前 admin 僅提供 hotspot 更新 API，沒有直接建立 location 的 API。

## request body
```json
{
  "latitude": 24.146672,
  "longitude": 120.673648,
  "radiusMeters": 30,
  "maxAccuracyMeters": 50,
  "hintText": "請靠近主要茶園入口"
}
```

## 欄位規格
- `questId`: UUID，必填（來源於關聯，不在 body）
- `name`: string，必填，最大長度 255
- `latitude`: 數字，必填，-90 ~ 90，DB `NUMERIC(9,6)`
- `longitude`: 數字，必填，-180 ~ 180，DB `NUMERIC(9,6)`
- `radiusMeters`: integer，必填，`> 0`
- `maxAccuracyMeters`: integer，必填，`> 0`
- `hintText`: string，建議提供
- `status`: string，必填，`ACTIVE` / `INACTIVE`
- `sortOrder`: integer，必填，該 quest 下不可重複
- `isPrimary`: boolean，必填

## 五景點對應 hotspot

### 1) 小半天高架橋（`locationId=aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa`）
```json
{
  "latitude": 23.715000,
  "longitude": 120.760000,
  "radiusMeters": 50,
  "maxAccuracyMeters": 120,
  "hintText": "請沿著小半天通往三村主線前進，觀察高架橋與谷地景觀",
  "status": "ACTIVE",
  "sortOrder": 1,
  "isPrimary": true
}
```

### 2) 小半天石馬公園（`locationId=bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb`）
```json
{
  "latitude": 23.714000,
  "longitude": 120.761100,
  "radiusMeters": 80,
  "maxAccuracyMeters": 120,
  "hintText": "由高架橋側邊進入，尋找櫻花步道與休憩空間",
  "status": "ACTIVE",
  "sortOrder": 2,
  "isPrimary": false
}
```

### 3) 小半天竹藝工坊（`locationId=cccccccc-cccc-cccc-cccc-cccccccccccc`）
```json
{
  "latitude": 23.715300,
  "longitude": 120.761700,
  "radiusMeters": 50,
  "maxAccuracyMeters": 120,
  "hintText": "步入小半天觀光服務聚落後，沿指標抵達竹藝展示區",
  "status": "ACTIVE",
  "sortOrder": 3,
  "isPrimary": false
}
```

### 4) 林爽文古戰場／孟宗竹林古戰場（`locationId=dddddddd-dddd-dddd-dddd-dddddddddddd`）
```json
{
  "latitude": 23.716800,
  "longitude": 120.762900,
  "radiusMeters": 120,
  "maxAccuracyMeters": 150,
  "hintText": "沿長源圳步道方向前進，尋找歷史敘事牌與地景節點",
  "status": "ACTIVE",
  "sortOrder": 4,
  "isPrimary": false
}
```

### 5) 武岫竹炭窯文化園區／竹炭窯文化園區（`locationId=eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee`）
```json
{
  "latitude": 23.724034,
  "longitude": 120.757228,
  "radiusMeters": 80,
  "maxAccuracyMeters": 150,
  "hintText": "由竹林村方向進入，沿茶園與竹林地景抵達窯區",
  "status": "ACTIVE",
  "sortOrder": 5,
  "isPrimary": false
}
```

## 注意
- 以上座標以示意為主，建議以現場實測值為準。
- 每個 quest 建議至少保留一筆 `isPrimary=true`。
