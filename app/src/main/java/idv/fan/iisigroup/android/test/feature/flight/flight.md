### 功能說明
- 顯示桃園機場即時航班資訊，包含狀態、時間及登機門等資料
- 支援標籤篩選（抵達狀態、起飛地區）
- 每十秒自動刷新，亦可下拉手動刷新
- 點擊航班可開啟外部瀏覽器查看詳細資訊

### 頁面內容
- 標題 TopAppBar：「航班資訊」
- 更新資訊區塊：
  - Loading：尚未取得資料
  - Success：顯示最後一次刷新時間
  - Failed：錯誤資訊
- 篩選器：
  - 提供 Label Filters 給使用者多選篩選，沒有選擇代表不篩選（全部顯示）
  - 分為：
    - 「抵達」：篩選出 airFlyStatus = 抵達
    - 地區列表：將所有 Response 中 upAirportName 列出
- 內容：
  - Loading：畫面空白，以 Shimmer 顯示讀取狀態
  - Success：
    - LoadingProgress 消失，顯示航班資訊列表
    - 成功後每十秒刷新一次，刷新時顯示 LoadingProgress 文字「刷新中」
    - 刷新失敗時在頁面最上方，以獨立區塊顯示錯誤訊息，不進入錯誤畫面
    - Empty：顯示「目前無任何航班資訊」
    - 橫向轉為一行兩列
    - 下拉可即時刷新資訊
    - 點擊項目即開啟外部瀏覽器至 airLineUrl，若無網址則顯示 Toast：「此航班沒有提供連結」
  - Failed：顯示錯誤訊息及重新嘗試按鈕

### 技術文件

#### API 串接
- URL：`https://www.kia.gov.tw/API/InstantSchedule.ashx?AirFlyLine=2&AirFlyIO=2`
- 此端點回傳含 JavaScript 的 HTML，透過 `window.location.href` 導向實際 JSON 檔案，OkHttp 無法執行 JS，需以 Interceptor 解析
- 請為此建立獨立的 Retrofit API Interface，並將 BASE_URL 抽出至環境變數，分為 Production / Dev 環境

#### FlightJsRedirectInterceptor
- 用途：動態解析 kia.gov.tw 的 JS redirect，自動重打至實際 JSON URL
- 流程：
  1. 攔截 Response，確認 Content-Type 為 `text/html` 才處理
  2. 從原始 Request URL 取得 `AirFlyLine` 與 `AirFlyIO` 參數
  3. 以 regex 解析 HTML 中的條件邏輯，比對 `line === "X" && io === "Y"` 找出對應 `target` 檔名
  4. 以 regex 解析 `window.location.href = "/path/" + target` 取得 base path
  5. 組合 `basePath + target` 建立新 URL，移除原本的 Query Parameters，重打 request
- 異常處理：若 regex 解析失敗（target 或 basePath 為 null），以 Timber.w 記錄警告並回傳原始 response
- 讀取 HTML 上限：1 MB（防止異常大 HTML 導致 OOM）

#### Request

| 欄位 | 型別 | 說明 |
| :--- | :--- | :--- |
| AirFlyLine | Int | 預設為 2 |
| AirFlyIO | Int | 預設為 2 |

#### Response

| 欄位 | 型別 | 說明 |
| :--- | :--- | :--- |
| expectTime | String? | 預計時間 (例如: "09:15") |
| realTime | String? | 實際時間 (例如: "09:13") |
| airLineName | String? | 航空公司名稱 (例如: "立榮航空") |
| airLineCode | String? | 航空公司代碼 (例如: "UIA") |
| airLineLogo | String? | 航空公司標誌圖片 URL |
| airLineUrl | String? | 航空公司聯絡網址 URL |
| airLineNum | String? | 航班編號 (例如: "B78690") |
| upAirportCode | String? | 起飛機場代碼 (例如: "MZG") |
| upAirportName | String? | 起飛機場名稱 (例如: "澎湖") |
| airPlaneType | String? | 機型代碼 (例如: "AT76") |
| airBoardingGate | String? | 登機門編號 (例如: "15") |
| airFlyStatus | String? | 航班飛行狀態 (例如: "抵達") |
| airFlyDelayCause | String? | 航班延誤原因說明 |

#### Response 範例

```json
[
  {
    "expectTime": "09:15",
    "realTime": "09:13",
    "airLineName": "立榮航空",
    "airLineCode": "UIA",
    "airLineLogo": "https://www.kia.gov.tw/images/ALL-square/B7.png",
    "airLineUrl": "https://www.kia.gov.tw/contact.html#立榮航空",
    "airLineNum": "B78690",
    "upAirportCode": "MZG",
    "upAirportName": "澎湖",
    "airPlaneType": "AT76",
    "airBoardingGate": "15",
    "airFlyStatus": "抵達",
    "airFlyDelayCause": ""
  }
]
```

#### ViewModel UiState 設計
- UiState 拆成 `flightApiResponse` 與用戶操作行為的 `uiState`，讓 API 行為與用戶行為彼此不互相影響
- `labelFilter` 因資料來源為 ApiResponse，歸屬於 ApiResponse UiState 內