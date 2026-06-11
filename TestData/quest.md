# ===== DOCS/quest.md =====
# quest（關卡）資料需求

## 來源
- `ADMIN_TEST_DATA_FORMAT.md`：`## quest`

## API
- `POST /api/admin/quests`
- `PUT /api/admin/quests/{questId}`

## request body（單筆）
```json
{
  "code": "bridge-route-01",
  "title": "小半天高架橋",
  "description": "位於南投鹿谷小半天入口動線的高架橋，跨越溪谷並連接小半天三村，是兼具交通功能與山谷景觀視野的在地地標。",
  "sortOrder": 1,
  "status": "ACTIVE"
}
```

## 欄位規格
- `code`: string，必填，不可空白
  - 最大長度：64
  - 同一個 game 內不可重複
- `title`: string，必填，不可空白，最大長度 255
- `description`: string，必填，不可空白
- `sortOrder`: integer，必填
  - 同一個 game 內不可重複
- `status`: string，必填，不可空白
  - 可接受值：`DRAFT`、`ACTIVE`、`DISABLED`
  - 會被轉成大寫後驗證

## 五景點 quest 建立範例

> quest 只作為景點介紹：`title` 僅填地點名，`description` 僅填景點簡介，不寫導覽任務或解謎說明。

```json
[
  {
    "code": "bridge-route-01",
    "title": "小半天高架橋",
    "description": "位於南投鹿谷小半天入口動線的高架橋，跨越溪谷並連接小半天三村，是兼具交通功能與山谷景觀視野的在地地標。",
    "sortOrder": 1,
    "status": "ACTIVE"
  },
  {
    "code": "shima-park-02",
    "title": "小半天石馬公園",
    "description": "位於小半天主要觀光動線上的休憩公園，以河津櫻與季節花景聞名，兼具步道入口、拍照停留與賞花特色。",
    "sortOrder": 2,
    "status": "ACTIVE"
  },
  {
    "code": "bamboo-workshop-03",
    "title": "小半天竹藝工坊",
    "description": "位於鹿谷竹林村的小半天竹藝文化據點，展示竹編、竹雕與生活竹器，呈現孟宗竹產業與地方工藝特色。",
    "sortOrder": 3,
    "status": "ACTIVE"
  },
  {
    "code": "linshuangwen-04",
    "title": "林爽文古戰場／孟宗竹林古戰場",
    "description": "位於小半天孟宗竹林與步道環境中的歷史景點，連結清代林爽文事件、竹林地景與象棋殘局紀念意象。",
    "sortOrder": 4,
    "status": "ACTIVE"
  },
  {
    "code": "wuxiu-charcoal-05",
    "title": "武岫竹炭窯文化園區／竹炭窯文化園區",
    "description": "位於鹿谷小半天的竹炭文化園區，結合孟宗竹林、茶園、生態步道與竹炭窯燒製程展示。",
    "sortOrder": 5,
    "status": "ACTIVE"
  }
]
```

## 最小 payload（範例）
```json
{
  "code": "bridge-route",
  "title": "小半天高架橋",
  "description": "跨越溪谷並連接小半天三村的交通與景觀地標。",
  "sortOrder": 1,
  "status": "DRAFT"
}
```
