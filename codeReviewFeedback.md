# Code Review 反饋 — 待確認事項

> 以下為 Code Review 中需要設計決策確認的問題，其餘問題已於對應 commit 中修正。

---

## 1. 🟡 Dialog 狀態策略統一方向

**問題描述：**
目前兩頁採用不同策略管理對話框可見性：

- `ExchangeRateUiState.Success` 將 `showCurrencyPicker`、`showCalculator` 存於 ViewModel
- `SettingScreen.kt` 用 `remember { mutableStateOf(false) }` 管理 `showCurrencyPicker`

**兩種方向的取捨：**

| | ViewModel 管理 | local `remember` 管理 |
|---|---|---|
| 優點 | 存活於螢幕旋轉（config change）；易於 ViewModel 單元測試驗證 | 程式碼更簡潔；不污染業務 UiState |
| 缺點 | UiState 混入純 UI 關注點；每次開關觸發 StateFlow emit | 旋轉後 dialog 自動關閉；無法從 ViewModel 測試 |

**請確認：** 應將 ExchangeRateScreen 的對話框狀態移至 local `remember`（與 SettingScreen 一致），還是將 SettingScreen 的對話框狀態移至 ViewModel（與 ExchangeRateScreen 一致）？

---

## 2. 🟡 Auto-refresh 邏輯重複抽取方式

**問題描述：**
`FlightViewModel` 與 `ExchangeRateViewModel` 的 auto-refresh 實作幾乎完全相同（`loadJob`、`refreshJob`、`observeAutoSyncSettings()`、`startAutoRefresh()`、`pullToRefresh()`）。

**可能的重構方向：**

**選項 A：`AutoRefreshManager` 獨立類別**
```kotlin
class AutoRefreshManager(
    private val scope: CoroutineScope,
    private val dataStore: UserPreferencesDataStore,
    private val onRefresh: suspend () -> Unit,
) { ... }
```

**選項 B：抽象 ViewModel 基礎類**
```kotlin
abstract class AutoRefreshViewModel(...) : ViewModel() {
    protected fun observeAutoSyncSettings() { ... }
    protected fun startAutoRefresh() { ... }
    abstract suspend fun doRefresh()
}
```

**請確認：** 偏好哪個方向？或是認為目前規模的重複程度可以接受，暫不抽取？

---

## 3. ⚪ 測試補充範圍與優先序

**問題描述：**
CLAUDE.md 規範需撰寫 ComposeUI test 及單元測試，目前覆蓋率不足。

**待確認優先序：**

| 優先序 | 類型 | 說明 |
|---|---|---|
| 1 | Interceptor 單元測試 | `FlightJsRedirectInterceptor` regex 邏輯複雜，靜默失效風險高 |
| 2 | ViewModel 單元測試 | `ExchangeRateViewModel`、`SettingViewModel` |
| 3 | Repository 單元測試 | `FlightRepositoryImpl`、`ExchangeRateRepositoryImpl` |
| 4 | UseCase 測試 | `GetFlightsUseCase`、`GetExchangeRatesUseCase` |
| 5 | Compose UI 測試 | `FlightScreen`、`ExchangeRateScreen`、`SettingScreen` |

**請確認：** 是否按上述優先序進行？是否有特定場景希望優先覆蓋？

---

## 已修正項目（無需確認）

| 嚴重度 | 問題 | 狀態 |
|---|---|---|
| 🔴 | `FlightViewModel` `SyncInterval.default` 編譯錯誤 | ✅ 已修正 |
| 🟠 | API Key 移至 `local.properties` | ✅ 已修正 |
| 🟣 | `NetworkErrorInterceptor` 補充 `SocketTimeoutException` | ✅ 已修正 |
| 🟣 | `FlightJsRedirectInterceptor` peekBody 限制 1MB | ✅ 已修正 |
| 🟣 | `HttpLoggingInterceptor` 限制為 Debug only | ✅ 已修正 |
| 🔵 | `IssConstants` Boolean 改 `const val` | ✅ 已修正 |
| 🔵 | DataStore keys 改 `private` | ✅ 已修正 |
| 🔵 | `ExchangeRateRoute` 重複 smart cast | ✅ 已修正 |
| 🔵 | `FlightRoute` 縮排不一致 | ✅ 已修正 |
| 🔵 | `formatNumber` 重複邏輯抽取至共用工具 | ✅ 已修正 |
| 🔵 | 硬編碼中文狀態字串 → `FlightStatus` enum | ✅ 已修正 |
| 🔵 | `CurrencyPickerDialog` 重複實作 → 抽出共用元件 | ✅ 已修正 |
| 🟡 | `DatabaseModule` 重新命名為 `DataStoreModule` | ✅ 已修正 |
| 🟡 | `onBaseCurrencySelected` 設計決策補充說明 | ✅ 已修正 |
| 🟡 | `MainViewModel` `isDarkTheme` 斷路 | ✅ 已於前次 commit 修正 |
