### 規格說明
- 此為匯率計算機
- 畫面分為三個區塊
    - 標題 TopAppBar：「匯率換算」
    - 更新資訊：
        - Loading：尚未取得資料
        - Success：顯示最後一次刷新時間及目前的預設貨幣代碼，點擊時可以切換預設貨幣代碼，重新打 API 
        - Failed：錯誤資訊
    - 內容
        - Loading：畫面空白，以 Shimmer 顯示讀取狀態
        - Success：
            - LoadingProgress 消失顯示貨幣代碼及匯率，成功後需每十秒需刷新一次，每十秒刷新時需在畫面顯示 LoadingProgress 文字撰寫刷新中，刷新失敗時在頁面最上方，有個獨立區塊顯示錯誤訊息，但不進入錯誤畫面
            - Empty 需顯示目前無任何匯率資訊
            - 橫向轉為一行兩列
            - 下拉可以打 API 即時刷新資訊
        - Failed：
            - API 錯誤時顯示錯誤訊息及重新嘗試的按鈕

### API 說明
- API 串接：@GET https://api.freecurrencyapi.com/v1/latest
- BaseURL 為 https://api.freecurrencyapi.com/v1/
- 請為此建立獨立的 Retrofit API Interface ，並將 BASE_URL 抽出至環境變數之中，需要分為 Production / Dev 環境
- API Request

| 參數名稱 (Parameter) | 類型 (Type) | 必填 (Mandatory) | 說明 (Description) |
| :--- | :--- | :--- | :--- |
| **apikey** | string | 是 (✔) | 您的 API 金鑰。 |
| **base_currency** | string | 否 | 基準貨幣（所有匯率將相對於此貨幣）。預設為 **USD**。 |
| **currencies** | string | 否 | 以逗號分隔的貨幣代碼列表（例如：EUR,USD,CAD）。若未指定，預設將顯示所有可用貨幣。 |

- base_currency 為 TWD
- currencies 為 USD / JPY / KRW / CNY / THB / GBP 
- apiKey 列在環境變數需分為 Production / Dev 環境
- 請將上述使用到的貨幣代碼轉為 enum 

- API Response

  | 欄位名稱 (Field) | 類型 (Type) | 說明 (Description) |
  | :--- | :--- | :--- |
  | **meta** | object | 包含 API 請求的輔助資訊，如數據最後更新時間。 |
  | **meta.last_updated_at** | string (ISO 8601) | 此數據集的最後更新時間（例如：2023-04-27T10:15:00Z）。 |
  | **data** | object | 包含實際的匯率資訊。 |
  | **data.[CURRENCY_CODE]** | float | 以基準貨幣（Base Currency）為單位，對應各國貨幣代碼的匯率數值。 |
- 請將上述 currency_code 依據 request currencies 列出來的代碼轉為相同的 enum 