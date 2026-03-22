### 功能說明
- 此為匯率計算機，可換算多國即時匯率
- 可選擇基準貨幣，換算各國對應金額
- 內建計算機，供使用者輸入金額進行換算
- 每十秒自動刷新，亦可下拉手動刷新

### 頁面內容
- 標題 TopAppBar：「匯率換算」
- 更新資訊區塊：
  - Loading：尚未取得資料
  - Success：顯示最後一次刷新時間及目前預設貨幣代碼，點擊可切換並重新打 API
  - Failed：錯誤資訊
- 計算機輸入結果：
  - 預設顯示「歡迎使用計算機 ✏️」，點擊後顯示 CalculatorBottomSheet
  - 當有計算結果時，改為顯示「$數字 $當前幣值」
- 內容：
  - Loading：畫面空白，以 Shimmer 顯示讀取狀態
  - Success：
    - LoadingProgress 消失，顯示「貨幣代碼/匯率」
    - 使用者透過 CalculatorBottomSheet 輸入金額後，改為顯示「貨幣代碼/計算金額/匯率」
    - 成功後每十秒刷新一次，刷新時顯示 LoadingProgress 文字「刷新中」
    - 刷新失敗時在頁面最上方，以獨立區塊顯示錯誤訊息，不進入錯誤畫面
    - Empty：顯示「目前無任何匯率資訊」
    - 橫向轉為一行兩列
    - 下拉可即時刷新資訊
  - Failed：顯示錯誤訊息及重新嘗試按鈕
- FloatingButton：點擊後顯示 CalculatorBottomSheet
- CalculatorBottomSheet：
  - 輸入窗：上方顯示計算公式及結果
  - 按鈕排列（一排四個）：`1 / 2 / 3 / × / 4 / 5 / 6 / ÷ / 7 / 8 / 9 / - / 0 / . / C / +`
  - 「輸入」按鈕：左右填滿
  - 規則：
    - 使用者點擊按鈕時開始記錄於輸入窗
    - 點擊 C 可清除上一動作
    - 點擊「輸入」後記錄金額，依輸入金額作為基準，在各國匯率中顯示對應金額

### 技術文件

#### API 串接
- URL：`GET https://api.freecurrencyapi.com/v1/latest`
- BASE_URL：`https://api.freecurrencyapi.com/v1/`
- 請為此建立獨立的 Retrofit API Interface，並將 BASE_URL 抽出至環境變數，分為 Production / Dev 環境

#### Request

| 參數名稱 | 類型 | 必填 | 說明 |
| :--- | :--- | :--- | :--- |
| apikey | string | ✔ | API 金鑰，列於環境變數，分為 Production / Dev 環境 |
| base_currency | string | | 基準貨幣，預設為 USD |
| currencies | string | | 以逗號分隔的貨幣代碼，未指定則顯示所有貨幣 |

- `base_currency` 固定為 `USD`
- `currencies` 固定為 `USD, JPY, KRW, CNY, THB, GBP`
- 上述貨幣代碼請轉為 enum

#### Response

| 欄位名稱 | 類型 | 說明 |
| :--- | :--- | :--- |
| data | object | 包含實際的匯率資訊 |
| data.[CURRENCY_CODE] | float | 以基準貨幣為單位，各幣別對應的匯率數值 |

- Response 中的 currency_code 依 currencies 列出的代碼，與 Request enum 共用

#### ViewModel UiState 設計
- UiState 拆成 `exchangeApiResponse` 與 `calculatorBottomSheet`，讓 API 行為與 CalculatorBottomSheet 彼此不互相影響
