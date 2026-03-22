# Code Review 報告

> 日期：2026-03-15
> 審查範圍：全專案架構及各功能頁面
> 架構模式：Clean Architecture + MVVM + Jetpack Compose

---

## 目錄

1. [架構總覽](#架構總覽)
2. [🔴 嚴重問題 (Critical)](#嚴重問題)
3. [🟠 安全性問題 (Security)](#安全性問題)
4. [🟡 架構設計問題 (Architecture)](#架構設計問題)
5. [🔵 程式碼品質 (Code Quality)](#程式碼品質)
6. [🟣 網路層問題 (Network)](#網路層問題)
7. [⚪ 測試覆蓋率 (Testing)](#測試覆蓋率)
8. [✅ 優點整理 (Strengths)](#優點整理)
9. [總結](#總結)

---

## 架構總覽

```
app/
├── core/               IssConstants（全域常數）
├── data/
│   ├── local/          UserPreferencesDataStore
│   ├── remote/         API Service, Response Model
│   └── repository/     Repository 實作
├── di/                 Hilt 模組（Network, DB, Repository）
├── domain/
│   ├── model/          Flight, ExchangeRate, Currency, SyncInterval
│   ├── repository/     Repository 介面
│   └── usecase/        GetFlightsUseCase, GetExchangeRatesUseCase
├── feature/
│   ├── flight/         航班功能頁
│   ├── exchangeRate/   匯率功能頁
│   └── setting/        設定頁
├── network/            Interceptor, ApiResult, NetworkException
└── ui/
    ├── components/     Shimmer 元件
    ├── state/          UiState 定義
    └── theme/          Material Design 3 主題
```

整體架構分層清晰，遵循 Clean Architecture 及 MVVM 單向資料流原則。以下依問題嚴重性分類說明。

---

## 嚴重問題

### 🔴 [BUG] `SyncInterval.default` 屬性不存在 — 編譯失敗

**檔案：** `feature/flight/FlightViewModel.kt:47`

```kotlin
// ❌ 錯誤：SyncInterval 並無 default companion property
private var currentAutoSyncIntervalMs: Long = SyncInterval.default.ms
```

`SyncInterval` enum 僅定義了 `fromMs(ms: Long)` companion function，並無 `default` 屬性，此行會導致**編譯失敗**。

對照 `ExchangeRateViewModel.kt:37` 的正確寫法：

```kotlin
// ✅ 正確（ExchangeRateViewModel）
private var currentAutoSyncIntervalMs: Long = IssConstants.UserPreferences.DEFAULT_SYNC_INTERVAL.ms
```

**修正方式：**
```kotlin
private var currentAutoSyncIntervalMs: Long = IssConstants.UserPreferences.DEFAULT_SYNC_INTERVAL.ms
```

---

## 安全性問題

### 🟠 [SECURITY] API Key 明文寫入版控

**檔案：** `app/build.gradle.kts`

```kotlin
// ❌ API Key 硬編碼在 gradle 檔案，會進入版控
buildConfigField("String", "EXCHANGE_RATE_API_KEY", "\"fca_live_iG1uxn9wgTllfRilqJbwrkQzYj9gYfCUbCvOGjda\"")
```

API Key 一旦進入 git history，即使後續刪除也無法完全消除洩漏風險。

**修正方式：** 將 key 移至 `local.properties`（已列於 `.gitignore`）：

```
# local.properties
EXCHANGE_RATE_API_KEY=fca_live_xxx
```

```kotlin
// build.gradle.kts
val exchangeRateApiKey = localProperties.getProperty("EXCHANGE_RATE_API_KEY") ?: ""
buildConfigField("String", "EXCHANGE_RATE_API_KEY", "\"$exchangeRateApiKey\"")
```

---

## 架構設計問題

### 🟡 [ARCH] UI 對話框狀態混入 UiState，兩頁設計不一致

**檔案：** `ui/state/ExchangeRateUiState.kt` vs `feature/setting/SettingScreen.kt`

`ExchangeRateUiState.Success` 將對話框可見性（屬於 UI 層關注點）放入 ViewModel 管理的 state：

```kotlin
// ExchangeRateUiState.Success — 含有純 UI 狀態
data class Success(
    val rates: List<ExchangeRate>,
    val baseCurrency: Currency,
    val lastRefreshTime: String,
    val showCurrencyPicker: Boolean = false,  // ← 純 UI 對話框狀態
    val showCalculator: Boolean = false,       // ← 純 UI 對話框狀態
    val calculatorAmount: Double = ...
) : ExchangeRateUiState()
```

相較之下，`SettingScreen.kt` 用 local `remember` 管理 `showCurrencyPicker`：

```kotlin
// SettingScreen.kt — 對話框狀態留在 Composable
var showCurrencyPicker by remember { mutableStateOf(false) }
```

**問題：**
1. 兩個頁面採用不同策略，降低可讀性與可維護性。
2. `showCalculator`、`showCurrencyPicker` 放在 ViewModel 中，使 `ExchangeRateUiState` 難以在測試中保持乾淨。
3. 每次開關對話框都會觸發 `StateFlow` emit，可能造成不必要的 recompose。

**建議：** 統一策略。對話框可見性屬於短暫 UI 互動狀態，建議以 local `remember` 管理，僅將業務資料（`calculatorAmount`、`baseCurrency`）保留在 ViewModel。

---

### 🟡 [ARCH] Auto-refresh 邏輯在兩個 ViewModel 完全重複

**檔案：** `FlightViewModel.kt`、`ExchangeRateViewModel.kt`

兩個 ViewModel 均實作了幾乎相同的 auto-refresh 模式：

```kotlin
// 兩者皆有，邏輯幾乎一樣
private var loadJob: Job? = null
private var refreshJob: Job? = null
private var currentAutoSyncEnabled: Boolean = ...
private var currentAutoSyncIntervalMs: Long = ...

private fun observeAutoSyncSettings() { ... }
private fun startAutoRefresh() { while (true) { delay(...); ... } }
fun pullToRefresh() { ... }
```

**問題：** 重複代碼導致：若 auto-refresh 邏輯有 bug 或需求變更，需同步修改兩處。

**建議：** 抽取 `AutoRefreshManager` 或使用 Kotlin delegation pattern 統一管理 auto-refresh 職責。

---

### 🟡 [ARCH] `MainViewModel.setDarkTheme` 似乎未被使用

**檔案：** `MainViewModel.kt`、`MainActivity.kt`

`MainViewModel` 暴露了 `isDarkTheme: StateFlow<Boolean>` 和 `setDarkTheme()`，但 `MainActivity` 的 `IisigroupAndroidTestApp()` 並未注入或讀取 `MainViewModel` 來控制主題切換：

```kotlin
// MainActivity.kt — 未見 MainViewModel 被呼叫
setContent {
    IisigroupAndroidTestTheme { // 主題未接收 isDarkTheme 參數
        IisigroupAndroidTestApp()
    }
}
```

若深色模式切換預期由 `MainViewModel` 驅動，這條路徑目前是**斷路**的。

---

### 🟡 [ARCH] `DatabaseModule` 命名誤導

**檔案：** `di/DatabaseModule.kt`

```kotlin
// 僅提供 DataStore，與 "Database" 無關
object DatabaseModule {
    fun provideDataStore(...): DataStore<Preferences>
}
```

模組名稱暗示有 Room Database，實際只提供 DataStore Preferences，建議重新命名為 `DataStoreModule`。

---

### 🟡 [ARCH] `onBaseCurrencySelected` 與 DataStore 的同步問題

**檔案：** `feature/exchangeRate/ExchangeRateViewModel.kt:150`

```kotlin
fun onBaseCurrencySelected(currency: Currency) {
    currentBaseCurrency = currency         // 僅更新 in-memory，不存至 DataStore
    ...
    loadRates()
}
```

使用者在匯率頁切換基準幣別時，選擇不會被持久化到 DataStore。`observeDefaultCurrency()` 監聽的是 DataStore（由設定頁控制），導致：

- 應用重啟後，匯率頁的基準幣別會回到設定頁的預設值（可能是設計決策）
- 但若使用者在設定頁改變預設幣別，`observeDefaultCurrency` 會觸發並覆蓋使用者在匯率頁的臨時選擇

**建議：** 明確在文件或程式碼註解說明此為「臨時 session 覆蓋」的設計決策，避免後續維護者誤判為 bug。

---

## 程式碼品質

### 🔵 [QUALITY] 硬編碼中文狀態字串

**檔案：** `feature/flight/FlightScreen.kt:299`、`feature/flight/FlightViewModel.kt:183`

```kotlin
// FlightScreen.kt — 硬編碼中文 string 做狀態比對
val containerColor = when (status) {
    "抵達" -> FlightStatusArrived
    "延誤" -> FlightStatusDelayed
    "取消" -> FlightStatusCancelled
    "起飛" -> FlightStatusDeparted
    else   -> FlightStatusDefault
}

// FlightViewModel.kt — 同樣的硬編碼
is FlightFilterOption.Arrived -> flight.airFlyStatus == "抵達"
```

**問題：** 若 API 變更回傳字串或需要多語言支援，需搜尋所有比對點才能修正。

**建議：** 在 domain model 中定義 `FlightStatus` enum 並在 `FlightRepositoryImpl.toDomain()` 中做映射：

```kotlin
enum class FlightStatus { ARRIVED, DELAYED, CANCELLED, DEPARTED, UNKNOWN }
data class Flight(
    ...
    val flyStatus: FlightStatus,
)
```

---

### 🔵 [QUALITY] 數字格式化邏輯重複

**檔案：** `feature/exchangeRate/ExchangeRateScreen.kt:259`、`feature/exchangeRate/CalculatorBottomSheet.kt:268`

```kotlin
// ExchangeRateScreen.kt — 內嵌格式化
val amountLabel = if (calculatorAmount == 1.0) "1"
else "%.4f".format(calculatorAmount).trimEnd('0').trimEnd('.')

// CalculatorBottomSheet.kt — 相同邏輯，抽成私有函式
private fun formatNumber(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString()
    else "%.4f".format(value).trimEnd('0').trimEnd('.')
```

**建議：** 將 `formatNumber` 移至共用的 extension function 或 utility 檔案供兩處使用。

---

### 🔵 [QUALITY] `IssConstants` 中的 `val` 應使用 `const val`

**檔案：** `core/IssConstants.kt`

```kotlin
object UserPreferences {
    val DEFAULT_DARK_THEME = false           // ❌ 應為 const val
    val DEFAULT_AUTO_SYNC_ENABLED = true     // ❌ 應為 const val
    val DEFAULT_SYNC_INTERVAL: SyncInterval = ...  // 正確，非 primitive，無法 const
    val DEFAULT_CURRENCY: Currency = ...           // 正確，非 primitive，無法 const
}
```

`Boolean` 常數應宣告為 `const val`，可在 compile time 內聯，避免 runtime 的 object 存取。

---

### 🔵 [QUALITY] `UserPreferencesDataStore` Preferences Keys 應為 private

**檔案：** `data/local/datastore/UserPreferencesDataStore.kt:49`

```kotlin
companion object {
    val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")         // ❌ 公開
    val AUTO_SYNC_ENABLED_KEY = booleanPreferencesKey("auto_sync_enabled")
    val AUTO_SYNC_INTERVAL_MS_KEY = longPreferencesKey("auto_sync_interval_ms")
    val DEFAULT_CURRENCY_KEY = stringPreferencesKey("default_currency")
}
```

DataStore key 對外暴露代表外部可以直接繞過 DataStore class 存取，破壞封裝。除非有測試需求，否則應改為 `private val`。

---

### 🔵 [QUALITY] `ExchangeRateRoute.kt` 重複 smart cast

**檔案：** `feature/exchangeRate/ExchangeRateRoute.kt:53`

```kotlin
// 同一個 uiState 做了兩次 is check + smart cast
if (uiState is ExchangeRateUiState.Success && (uiState as ExchangeRateUiState.Success).showCalculator) {
```

由於 `uiState` 是 `collectAsState()` 的 `val`，Kotlin smart cast 在第一個 `is` check 後就成立，第二次 `as` 強轉是多餘的：

```kotlin
// ✅ 可簡化為
val successState = uiState as? ExchangeRateUiState.Success
if (successState?.showCalculator == true) { ... }
```

---

### 🔵 [QUALITY] `FlightRoute.kt` 縮排不一致

**檔案：** `feature/flight/FlightRoute.kt:49`

```kotlin
FlightScreen(
    uiState = uiState,
    onRetry = viewModel::loadFlights,
                onFilterToggle = viewModel::onFilterToggle,  // ❌ 多餘縮排
    onFlightClick = viewModel::onFlightClick,
    ...
)
```

`onFilterToggle` 行有多餘的縮排，推測為合併或重構時造成的格式錯誤。

---

### 🔵 [QUALITY] `CurrencyPickerDialog` 重複實作

**檔案：** `feature/exchangeRate/ExchangeRateScreen.kt:290`、`feature/setting/SettingScreen.kt:199`

兩個幣別選擇 Dialog 的 UI 結構幾乎完全相同（`AlertDialog` + `Currency.entries.forEach` + `TextButton`），可以抽出為共用 `ui/components/CurrencyPickerDialog` 元件。

---

## 網路層問題

### 🟣 [NETWORK] `NetworkErrorInterceptor` 未捕捉 `SocketTimeoutException`

**檔案：** `network/NetworkErrorInterceptor.kt`

```kotlin
// ❌ 漏掉 SocketTimeoutException，超時錯誤不會轉為友善訊息
} catch (e: UnknownHostException) {
    throw NetworkException(context.getString(R.string.error_network), e)
} catch (e: SocketException) {
    throw NetworkException(context.getString(R.string.error_network), e)
} catch (e: ConnectException) {
    throw NetworkException(context.getString(R.string.error_network), e)
}
```

`SocketTimeoutException` 是常見的網路錯誤（請求超時），應一併攔截並轉為使用者友善訊息。

**修正：**
```kotlin
} catch (e: SocketTimeoutException) {
    throw NetworkException(context.getString(R.string.error_network_timeout), e)
}
```

---

### 🟣 [NETWORK] `FlightJsRedirectInterceptor` 潛在記憶體問題

**檔案：** `network/FlightJsRedirectInterceptor.kt:43`

```kotlin
val html = response.peekBody(Long.MAX_VALUE).string()
```

`peekBody(Long.MAX_VALUE)` 會將整個 response body 讀入記憶體。若伺服器回傳異常大的 HTML，可能造成 `OutOfMemoryError`。應設定合理上限（如 1MB）：

```kotlin
val html = response.peekBody(1_048_576L).string() // 1MB 上限
```

---

### 🟣 [NETWORK] `HttpLoggingInterceptor` 在 Release Build 也以 BODY level 執行

**檔案：** `di/FlightNetworkModule.kt:41`、`di/ExchangeRateNetworkModule.kt`

```kotlin
// ❌ 未區分 build variant，release 也會 log request/response body
.addInterceptor(HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
})
```

BODY level 會輸出完整請求與回應內容（包含 API key header），在 release build 可能洩漏敏感資訊至 logcat。

**修正：**
```kotlin
.addInterceptor(HttpLoggingInterceptor().apply {
    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
})
```

---

## 測試覆蓋率

根據 `CLAUDE.md` 的規範：「需撰寫 ComposeUI test 及單元測試」，目前覆蓋率不足：

| 類別 | 現況 | 建議優先補充 |
|------|------|------------|
| ViewModel 單元測試 | 部分（FlightViewModelTest 存在） | ExchangeRateViewModel、SettingViewModel |
| Repository 單元測試 | 缺少 | FlightRepositoryImpl、ExchangeRateRepositoryImpl |
| UseCase 測試 | 缺少 | GetFlightsUseCase、GetExchangeRatesUseCase |
| Compose UI 測試 | 缺少 | FlightScreen、ExchangeRateScreen、SettingScreen |
| Interceptor 測試 | 缺少 | FlightJsRedirectInterceptor（regex 邏輯複雜，高風險） |

特別建議補充 `FlightJsRedirectInterceptor` 的單元測試，因其包含複雜的 regex 解析邏輯，且依賴第三方 HTML 格式，容易因伺服器更新而靜默失效。

---

## 優點整理

以下為架構與實作上值得保留的良好設計：

- **✅ Clean Architecture 分層清晰**：domain、data、ui 層職責分明，Repository pattern 落實完整。
- **✅ 單向資料流**：所有 ViewModel 以 `StateFlow` 暴露 state，副作用用 `SharedFlow` 傳遞（FlightEvent），符合 MVVM 規範。
- **✅ 字串資源化**：UI 文字全數抽出至 `strings.xml`，無硬編碼（除狀態比對字串）。
- **✅ Shimmer Loading**：載入中有良好的 skeleton UI，不直接顯示空白。
- **✅ 錯誤處理分層**：網路層統一在 `NetworkErrorInterceptor` 轉換，Repository 用 `ApiResult` 包裝，ViewModel 不需處理 raw exception。
- **✅ Debug 工具整合**：Flipper 透過 build variant 的 source set 分離，release build 乾淨無 debug 依賴。
- **✅ `FlightJsRedirectInterceptor` 設計**：優雅處理了 JS redirect 的特殊情境，且有完整的 KDoc 說明解析邏輯。
- **✅ Responsive UI**：FlightScreen、ExchangeRateScreen 支援 landscape/portrait 的 grid 切換。
- **✅ Material Design 3**：顏色、字體等設計 token 統一管理在 `ui/theme`，未散落在各頁面。
- **✅ 型別安全的 Navigation**：使用 `@Serializable` data object 定義 route，搭配 `hasRoute()` 判斷選中狀態。

---

## 總結

| 類別 | 問題數 | 最高優先處理 |
|------|--------|------------|
| 🔴 嚴重 | 1 | `SyncInterval.default` 編譯錯誤 |
| 🟠 安全性 | 1 | API Key 移至 local.properties |
| 🟡 架構 | 4 | 對話框狀態策略統一、Auto-refresh 重構 |
| 🔵 品質 | 7 | 硬編碼狀態字串、重複邏輯抽取 |
| 🟣 網路 | 3 | SocketTimeoutException、Log level |
| ⚪ 測試 | 缺口大 | Repository、Interceptor、Compose UI 測試 |

**建議修復順序：**
1. 立即修復 `FlightViewModel.kt:47` 的編譯錯誤
2. 將 API Key 移出版控
3. `HttpLoggingInterceptor` 限制為 Debug only
4. 補充 `NetworkErrorInterceptor` 的 `SocketTimeoutException`
5. 重構 dialog state 策略，統一兩頁做法
6. 抽取 Auto-refresh 共用邏輯
7. 補充測試覆蓋率（依優先序：Interceptor → Repository → ViewModel → Compose UI）