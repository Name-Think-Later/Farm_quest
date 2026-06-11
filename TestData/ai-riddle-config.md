# ===== DOCS/ai-riddle-config.md =====
# ai-riddle-config（AI 題目設定）資料需求

## 來源
- `ADMIN_TEST_DATA_FORMAT.md`：`## ai-riddle-config`
- 題目答案需能從 `knowledge-document.md` 找到。

## API
- `PUT /api/admin/ai-riddles/{questId}`

## request body
```json
{
  "riddlePrompt": "你要引導玩家觀察景點內可見、可抵達、具唯一性的線索，不要直接公布答案。",
  "answerCriteria": "竹葉意象",
  "spoilerPolicy": "只能提示觀察方向與公開文化資訊，不直接說出答案，也不可逐字重述知識文件中的關鍵句。",
  "completionPolicy": "玩家回答出指定物理特徵或文化資訊，且能以現場觀察或知識文件佐證時才算完成。",
  "status": "ACTIVE"
}
```

## 欄位規格
- `riddlePrompt`: string，必填，不可空白
- `answerCriteria`: string，必填，不可空白
- `spoilerPolicy`: string，必填，不可空白
- `completionPolicy`: string，必填，不可空白
- `status`: string，必填，不可空白；允許值 `ACTIVE` / `INACTIVE`

## 出題原則
- 題目不以「景點名稱」作為答案。
- 題目答案需符合以下之一：
  1. 景點內或周邊明顯可見、可接觸、玩家可抵達、具唯一性的物理特徵。
  2. 與景點相關、公開且簡明的文化或歷史資訊。
- 題目的答案必須能由 `knowledge-document.md` 對應景點內容找到。

## 五景點對應 ai-riddle-config

> `questId` 請使用對應 quest 建立後回傳值。以下示例保留 `questId` 以便對照，但實際 API path 傳入時不放在 body。

```json
[
  {
    "questId": "11111111-1111-1111-1111-111111111111",
    "riddlePrompt": "請玩家在橋頭或橋邊觀察地方產業意象。提示：它和小半天孟宗竹產業有關，常以長條葉片或筍形圖案出現在橋頭柱、擋土牆等設計上。",
    "answerCriteria": "竹葉意象或冬筍意象",
    "spoilerPolicy": "可提示觀察橋頭柱、擋土牆、地方產業圖案與孟宗竹關聯；不可直接說出竹葉或冬筍。",
    "completionPolicy": "玩家答出竹葉意象、冬筍意象，或明確描述橋上與孟宗竹產業相關的圖案，即判定完成。",
    "status": "ACTIVE"
  },
  {
    "questId": "22222222-2222-2222-2222-222222222222",
    "riddlePrompt": "請玩家找出公園中最具代表性的季節植物。提示：它在這個公園常被介紹為一年可在二月與九月開花兩次，花色常吸引遊客拍照。",
    "answerCriteria": "河津櫻",
    "spoilerPolicy": "可提示櫻花、花季、二月與九月兩次開花；不可直接說出河津櫻。",
    "completionPolicy": "玩家答出河津櫻，或答出此處代表性的櫻花樹並能補充一年兩次花期，即判定完成。",
    "status": "ACTIVE"
  },
  {
    "questId": "33333333-3333-3333-3333-333333333333",
    "riddlePrompt": "請玩家觀察館內或展示區的手工藝物件。提示：答案是一種用竹材交錯成形的地方工藝，可出現在籃、器具或裝飾作品上。",
    "answerCriteria": "竹編",
    "spoilerPolicy": "可提示竹材紋理、交錯結構、手作展示與生活器具；不可直接說出竹編。",
    "completionPolicy": "玩家答出竹編，或能指認以竹材交錯編成的器物與小半天竹工藝關聯，即判定完成。",
    "status": "ACTIVE"
  },
  {
    "questId": "44444444-4444-4444-4444-444444444444",
    "riddlePrompt": "請玩家尋找古戰場中用來紀念歷史事件的特殊實體物件。提示：它像一場尚未完成的對局，放在步道旁，象徵起義未竟之意。",
    "answerCriteria": "巨大象棋殘局",
    "spoilerPolicy": "可提示步道旁、未完成對局、紀念林爽文事件；不可直接說出象棋殘局。",
    "completionPolicy": "玩家答出巨大象棋殘局、象棋盤、未下完的象棋局，且能連結林爽文事件紀念意義，即判定完成。",
    "status": "ACTIVE"
  },
  {
    "questId": "55555555-5555-5555-5555-555555555555",
    "riddlePrompt": "請玩家找出園區中和孟宗竹轉化利用最相關的黑色材料或製程設施。提示：它由竹材經窯燒而成，可延伸成食品、生活用品與文創商品。",
    "answerCriteria": "竹炭或竹炭窯",
    "spoilerPolicy": "可提示黑色材料、窯燒、孟宗竹、產品陳列與CAS優良林產品；不可直接說出竹炭或竹炭窯。",
    "completionPolicy": "玩家答出竹炭、竹炭窯，或明確描述孟宗竹經窯燒後形成的黑色材料與產品，即判定完成。",
    "status": "ACTIVE"
  }
]
```

## 常見錯誤
- 把景點名稱本身當作答案。
- 題目答案無法從 `knowledge-document.md` 找到。
- 答案不是玩家可觀察的實體特徵，也不是公開且簡明的文化資訊。
