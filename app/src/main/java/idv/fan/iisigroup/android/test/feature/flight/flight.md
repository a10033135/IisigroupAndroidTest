### 功能說明
- 此為桃園機場航班資訊功能需顯示航班資訊、狀態、時間
- 畫面分為三個區塊
  - 標題 TopAppBar：「航班資訊」
  - 更新資訊：
    - Loading：尚未取得資料
    - Success：顯示最後一次刷新時間
    - Failed：錯誤資訊
  - 篩選器：
    - 提供 Label Filters 給使用者多選篩選，沒有選擇代表不篩選-全部顯示
    - 分為
      - 「抵達」：篩選出 airFlyStatus = 抵達
      - 地區列表：將所有 Response 中 upAirportName 列出
  - 內容
    - Loading：畫面空白，以 Shimmer 顯示讀取狀態
    - Success：
      - LoadingProgress 消失，顯示航班資訊列表，成功後需每十秒需刷新一次，每十秒刷新時需在畫面顯示 LoadingProgress 文字撰寫刷新中，刷新失敗時在頁面最上方，有個獨立區塊顯示錯誤訊息，但不進入錯誤畫面
      - Empty 需顯示目前無任何航班資訊
      - 橫向轉為一行兩列
      - 下拉可以打 API 即時刷新資訊
      - 點擊項目即開啟外開瀏覽器至 airLineUrl ，若沒有網址則顯示 Toast ：「此航班沒有提供連結」
    - Failed：
      - API 錯誤時顯示錯誤訊息及重新嘗試的按鈕

### API 說明
- API 串接： https://www.kia.gov.tw/API/InstantSchedule.ashx?AirFlyLine=2&AirFlyIO=2
- 此連結為 HTML 連結，實際 Response 需要倚靠導轉才能取得，因此需分析 HTML 導轉取得實際 JSON 連結
- 請為此建立獨立的 Retrofit API Interface ，並將 BASE_URL 抽出至環境變數之中，需要分為 Production / Dev 環境
- API Request

  | 欄位        | 型別  | 說明   |
  |-----------|-----|------|
  | AirFlyLine | Int | 預設為2 |
  | AirFlyIO  | Int | 預設為2 |
- API Response 參考

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

```
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