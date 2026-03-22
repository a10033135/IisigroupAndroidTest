# 測試策略文件

## 目錄
1. [測試層級總覽](#測試層級總覽)
2. [Unit Test — ViewModel](#unit-test--viewmodel)
3. [Unit Test — UseCase](#unit-test--usecase)
4. [Unit Test — Repository](#unit-test--repository)
5. [Unit Test — 工具類](#unit-test--工具類)
6. [Network Test — Interceptor](#network-test--interceptor)
7. [UI Test — Screen](#ui-test--screen)
8. [UI Test — Navigation](#ui-test--navigation)
9. [測試工具與依賴](#測試工具與依賴)

---

## 測試層級總覽

| 類型 | 位置 | 框架 | 目標覆蓋率 |
|---|---|---|---|
| Unit Test | `src/test/` | JUnit4 + MockK + Turbine | ViewModel / UseCase / Repository / Interceptor |
| UI Test | `src/androidTest/` | Compose UI Test + Hilt | Screen 各 UiState / 互動行為 |
| Navigation Test | `src/androidTest/` | Compose UI Test + NavController | 底部導航切換 / Route 串接 |

---

## Unit Test — ViewModel

### FlightViewModel

**測試檔案：** `src/test/.../feature/flight/FlightViewModelTest.kt`

| # | 測試情境 | 前置條件 | 驗證重點 |
|---|---|---|---|
| 1 | 初始化成功載入 | `getFlightsUseCase` 回傳 Success | `uiState` 流程為 Loading → Success；`apiState.flights` 正確；`userState.selectedFilters` 為空 |
| 2 | 初始化載入失敗 | `getFlightsUseCase` 回傳 Error | `uiState` 流程為 Loading → Error；`message` 正確 |
| 3 | Pull-to-refresh 成功 | 已在 Success 狀態，再次呼叫 | `apiState.isRefreshing` 為 true → false；`apiState.flights` 更新；`userState` 保留原有篩選 |
| 4 | Pull-to-refresh 失敗 | 已在 Success 狀態，API 回傳 Error | `apiState.refreshError` 有訊息；不進入 Error 狀態；`userState` 保留 |
| 5 | 篩選切換（新增） | 已在 Success 狀態 | `userState.selectedFilters` 新增 filter；`apiState.flights` 重算 |
| 6 | 篩選切換（移除） | 已選取某 filter | `userState.selectedFilters` 移除 filter；`apiState.flights` 重算 |
| 7 | 篩選 Arrived | 已載入含不同 status 的航班 | 只顯示 `airFlyStatus == ARRIVED` 的航班 |
| 8 | 篩選 Region | 已載入含不同 upAirportName 的航班 | 只顯示對應地區的航班 |
| 9 | 點擊有 URL 的航班 | `airLineUrl` 不為空 | emit `FlightEvent.OpenUrl(url)` |
| 10 | 點擊無 URL 的航班 | `airLineUrl` 為 null 或空 | emit `FlightEvent.ShowToast("此航班沒有提供連結")` |
| 11 | 自動刷新啟動 | `autoSyncEnabled = true`、載入成功後 | 等待間隔後 `apiState.isRefreshing = true`；完成後更新資料 |
| 12 | 自動刷新關閉 | `autoSyncEnabled = false` | 不觸發自動刷新 |
| 13 | 設定同步間隔變更 | 已在 Success 狀態，DataStore 變更 intervalMs | 舊 refreshJob 取消；以新間隔重啟 |
| 14 | 重新載入時篩選重置 | 已選取 filter，呼叫 `loadFlights()` | `currentFilters` 清空；`userState.selectedFilters` 為空 |

---

### ExchangeRateViewModel

**測試檔案：** `src/test/.../feature/exchangeRate/ExchangeRateViewModelTest.kt`

| # | 測試情境 | 前置條件 | 驗證重點 |
|---|---|---|---|
| 1 | 初始化成功載入 | DataStore 有預設幣別；API 回傳 Success | `uiState` 流程 Loading → Success；`apiState.rates` 正確 |
| 2 | 初始化載入失敗 | API 回傳 Error | `uiState` 為 Error；`message` 正確 |
| 3 | Pull-to-refresh 成功 | 已在 Success | `apiState.isRefreshing` true → false；`calculatorState` 保留 |
| 4 | Pull-to-refresh 失敗 | 已在 Success，API 回傳 Error | `apiState.refreshError` 有訊息；`calculatorState` 保留 |
| 5 | 自動刷新不影響計算機狀態 | 自動刷新成功 | `calculatorState.amount` / `showCalculator` 不被重置 |
| 6 | 開啟計算機 | 已在 Success | `calculatorState.showCalculator = true` |
| 7 | 關閉計算機 | `showCalculator = true` | `calculatorState.showCalculator = false` |
| 8 | 計算機確認金額 | 輸入 100.0 | `calculatorState.amount = 100.0`；`showCalculator = false` |
| 9 | 點擊開啟幣別選擇器 | 已在 Success | `showCurrencyPicker = true` |
| 10 | 關閉幣別選擇器 | `showCurrencyPicker = true` | `showCurrencyPicker = false` |
| 11 | Session 層級切換幣別 | 選擇 JPY | `currentBaseCurrency = JPY`；重新呼叫 API；`showCurrencyPicker = false` |
| 12 | DataStore 幣別變更觸發重載 | DataStore `defaultCurrencyCode` 變更 | 自動呼叫 `loadRates()` |

---

### SettingViewModel

**測試檔案：** `src/test/.../feature/setting/SettingViewModelTest.kt`

| # | 測試情境 | 驗證重點 |
|---|---|---|
| 1 | 初始化時從 DataStore 收集設定 | `uiState` 正確反映 DataStore 初始值 |
| 2 | 設定暗黑模式 | `isDarkTheme` 更新；DataStore 寫入正確 |
| 3 | 設定自動同步開關 | `autoSyncEnabled` 更新；DataStore 寫入正確 |
| 4 | 設定同步間隔 | `autoSyncInterval` 更新；DataStore 寫入正確 |
| 5 | 設定預設貨幣 | `defaultCurrency` 更新；DataStore 寫入正確 |

---

## Unit Test — UseCase

### GetFlightsUseCase

**測試檔案：** `src/test/.../domain/usecase/GetFlightsUseCaseTest.kt`

| # | 測試情境 | 驗證重點 |
|---|---|---|
| 1 | Repository 回傳成功 | 回傳 `ApiResult.Success<List<Flight>>` |
| 2 | Repository 回傳錯誤 | 回傳 `ApiResult.Error` with message |

### GetExchangeRatesUseCase

**測試檔案：** `src/test/.../domain/usecase/GetExchangeRatesUseCaseTest.kt`

| # | 測試情境 | 驗證重點 |
|---|---|---|
| 1 | 以 USD 為基準幣別 | 呼叫 Repository 傳入 Currency.USD |
| 2 | Repository 回傳成功 | 回傳 `ApiResult.Success<List<ExchangeRate>>` |
| 3 | Repository 回傳錯誤 | 回傳 `ApiResult.Error` |

---

## Unit Test — Repository

### FlightRepositoryImpl

**測試檔案：** `src/test/.../data/repository/FlightRepositoryImplTest.kt`

| # | 測試情境 | 驗證重點 |
|---|---|---|
| 1 | API 成功，`toDomain()` 轉換正確 | `Flight` 各欄位與 `FlightResponse` 對應；`airFlyStatus` 由 `FlightStatus.fromString()` 轉換 |
| 2 | `airFlyStatus = "抵達"` | `FlightStatus.ARRIVED` |
| 3 | `airFlyStatus = null` | `FlightStatus.UNKNOWN` |
| 4 | 網路異常（`NetworkException`） | 回傳 `ApiResult.Error` with「網路異常」前綴訊息 |
| 5 | 其他 Exception | 回傳 `ApiResult.Error` with `error_unknown` 訊息 |

### ExchangeRateRepositoryImpl

**測試檔案：** `src/test/.../data/repository/ExchangeRateRepositoryImplTest.kt`

| # | 測試情境 | 驗證重點 |
|---|---|---|
| 1 | API 成功，正確篩選 Currency enum 支援的幣別 | `ExchangeRate` 列表只包含 Currency enum 中的幣別 |
| 2 | Response 中含有未知幣別代碼 | 該幣別不出現在結果中 |
| 3 | 網路異常 | 回傳 `ApiResult.Error` with 網路異常訊息 |
| 4 | rate 數值轉換正確 | `ExchangeRate.rate` 精度正確 |

---

## Unit Test — 工具類

### FlightStatus.fromString

**測試檔案：** `src/test/.../domain/model/FlightStatusTest.kt`

| # | 輸入 | 期望輸出 |
|---|---|---|
| 1 | `"抵達"` | `ARRIVED` |
| 2 | `"延誤"` | `DELAYED` |
| 3 | `"取消"` | `CANCELLED` |
| 4 | `"起飛"` | `DEPARTED` |
| 5 | `null` / 未知字串 | `UNKNOWN` |

### formatNumber

**測試檔案：** `src/test/.../core/NumberFormatTest.kt`

| # | 輸入 | 期望輸出 |
|---|---|---|
| 1 | `1.0` | `"1"` |
| 2 | `3.14` | `"3.14"` |
| 3 | `3.1400` | `"3.14"` |
| 4 | `0.0001` | `"0.0001"` |
| 5 | `100.0` | `"100"` |

---

## Network Test — Interceptor

### NetworkErrorInterceptor

**測試檔案：** `src/test/.../network/NetworkErrorInterceptorTest.kt`

| # | 測試情境 | 驗證重點 |
|---|---|---|
| 1 | `ConnectException` | 拋出 `NetworkException` |
| 2 | `UnknownHostException` | 拋出 `NetworkException` |
| 3 | 正常請求 | 不干預，回傳原始 Response |

### FlightJsRedirectInterceptor

**測試檔案：** `src/test/.../network/FlightJsRedirectInterceptorTest.kt`

| # | 測試情境 | 驗證重點 |
|---|---|---|
| 1 | HTML 回應含 JS redirect URL | 解析出正確的 JSON URL，發出第二次請求 |
| 2 | HTML 回應不含 redirect | 不發出第二次請求，直接回傳原始 Response |
| 3 | regex 正確提取 URL | URL 格式符合預期 |

---

## UI Test — Screen

> 所有 UI 測試使用 `@HiltAndroidTest` + `HiltTestRunner`，以 `ComposeTestRule` 驅動。

### FlightScreen

**測試檔案：** `src/androidTest/.../feature/flight/FlightScreenTest.kt`

| # | UiState | 驗證元素 |
|---|---|---|
| 1 | Loading | Shimmer 元件可見；列表不可見 |
| 2 | Error | 錯誤訊息文字可見；「重新嘗試」按鈕可見 |
| 3 | Success（空列表） | 「目前無任何航班資訊」文字可見 |
| 4 | Success（有資料） | 航班卡片列表可見；顯示航班號碼、機場名稱 |
| 5 | Success，isRefreshing = true | LinearProgressIndicator 可見；「刷新中」文字可見 |
| 6 | Success，refreshError != null | 錯誤訊息 Banner 可見；不進入 Error 畫面 |
| 7 | Success，有篩選器 | FilterChip 可見；點擊後 selected 狀態切換 |
| 8 | 點擊「重新嘗試」 | `onRetry` callback 被呼叫 |
| 9 | 點擊航班卡片 | `onFlightClick` callback 被呼叫，傳入正確 Flight |

### ExchangeRateScreen

**測試檔案：** `src/androidTest/.../feature/exchangeRate/ExchangeRateScreenTest.kt`

| # | UiState | 驗證元素 |
|---|---|---|
| 1 | Loading | Shimmer 元件可見 |
| 2 | Error | 錯誤訊息可見；「重新嘗試」按鈕可見 |
| 3 | Success（空列表） | 「目前無任何匯率資訊」文字可見 |
| 4 | Success（有資料） | 匯率卡片列表可見；幣別代碼可見 |
| 5 | Success，calculatorAmount == null | Banner 顯示「歡迎使用計算機」 |
| 6 | Success，calculatorAmount == 100.0 | Banner 顯示「100 USD」 |
| 7 | Success，isRefreshing = true | LinearProgressIndicator 可見 |
| 8 | Success，showCurrencyPicker = true | CurrencyPickerDialog 可見 |
| 9 | Success，showCalculator = true | CalculatorBottomSheet 可見 |
| 10 | 點擊 Banner | `onCalculatorClick` 被呼叫 |
| 11 | 點擊更新資訊列 | `onBaseCurrencyClick` 被呼叫 |

### CalculatorBottomSheet

**測試檔案：** `src/androidTest/.../feature/exchangeRate/CalculatorBottomSheetTest.kt`

| # | 測試情境 | 驗證重點 |
|---|---|---|
| 1 | 初始狀態 | 顯示「0」；無 result 文字 |
| 2 | 輸入數字 `1`, `2`, `3` | expression 顯示 `123` |
| 3 | 輸入 `1`, `+`, `2` | expression 顯示 `1+2`；result 顯示 `3` |
| 4 | 輸入 `c`（退格） | 刪除最後一個字元 |
| 5 | 點擊「輸入」 | `onConfirm` 被呼叫，傳入正確的 Double 值 |
| 6 | 表達式有結果，點擊「輸入」 | 以計算後的結果傳入 `onConfirm` |

### SettingScreen

**測試檔案：** `src/androidTest/.../feature/setting/SettingScreenTest.kt`

| # | 測試情境 | 驗證重點 |
|---|---|---|
| 1 | 預設狀態 | 版本號顯示；暗黑模式 Switch 可見 |
| 2 | autoSyncEnabled = true | 同步間隔選項可見 |
| 3 | autoSyncEnabled = false | 同步間隔選項不可見 |
| 4 | 點擊暗黑模式 Switch | `onDarkThemeChange` 被呼叫 |
| 5 | 點擊預設貨幣 | CurrencyPickerDialog 顯示 |
| 6 | 選擇貨幣後 | `onDefaultCurrencyChange` 被呼叫；Dialog 關閉 |

---

## UI Test — Navigation

**測試檔案：** `src/androidTest/.../MainActivityTest.kt`

| # | 測試情境 | 驗證重點 |
|---|---|---|
| 1 | 啟動 App | 預設顯示 FlightScreen（底部 Tab 選中「航班」） |
| 2 | 點擊「匯率」Tab | 導航至 ExchangeRateScreen；TopAppBar 顯示「匯率換算」 |
| 3 | 點擊「設定」Tab | 導航至 SettingScreen；TopAppBar 顯示「設定頁面」 |
| 4 | 切換 Tab 後再切回 | 原 Tab 狀態（如篩選）是否保留（由 Back Stack 行為決定） |
| 5 | 深色模式啟用 | MaterialTheme 套用 Dark 色彩方案 |

---

## 測試工具與依賴

```kotlin
// Unit Test
testImplementation("junit:junit:4.13.2")
testImplementation("io.mockk:mockk:1.13.x")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:x.x.x")
testImplementation("app.cash.turbine:turbine:x.x.x")  // StateFlow / SharedFlow 測試

// Instrumented Test
androidTestImplementation("androidx.compose.ui:ui-test-junit4")
androidTestImplementation("androidx.test.ext:junit:1.x.x")
androidTestImplementation("com.google.dagger:hilt-android-testing:x.x.x")
androidTestImplementation("androidx.navigation:navigation-testing:x.x.x")

// Test Helpers
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test") // MainDispatcherRule
```

### MainDispatcherRule（已存在）

```kotlin
class MainDispatcherRule(
    private val testDispatcher: TestCoroutineDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
```

### 建議 Fake 實作

- `FakeFlightRepository` — 可設定回傳 Success / Error
- `FakeExchangeRateRepository` — 可設定回傳 Success / Error
- `FakeUserPreferencesDataStore` — 以 `MutableStateFlow` 模擬 DataStore flows
