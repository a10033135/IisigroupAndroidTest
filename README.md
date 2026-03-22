
# IisigroupAndroidTest

以 Jetpack Compose 建構的應用程式，提供即時匯率換算、桃園機場航班查詢及個人化設定功能。
---

## 建置環境

- **Android Studio**: Ladybug 以上
- **compileSdk**: 36
- **minSdk**: 26
- **targetSdk**: 36
- **JVM**: Java 11

### 執行步驟

1. Clone 專案
2. 在 `local.properties` 設定 `EXCHANGE_RATE_API_KEY`（或確認 `app/build.gradle.kts` 中已設定）
3. 使用 Android Studio 執行，或：

```bash
./gradlew assembleDebug
```


---

## 目錄

- [功能介紹](#功能介紹)
- [技術棧](#技術棧)
- [架構](#架構)
- [專案結構](#專案結構)
- [主要程式碼](#主要程式碼)
- [API 資訊](#api-資訊)
- [建置環境](#建置環境)

---

## 功能介紹

### 匯率換算（Exchange Rate）
- 透過 [freecurrencyapi.com](https://api.freecurrencyapi.com/v1/latest) 獲取即時匯率
- 支援幣別：USD、JPY、KRW、CNY、THB、GBP（以 USD 為基準）
- 每 10 秒自動刷新，支援手動下拉重新整理
- 內建浮動計算機（FloatingButton + BottomSheet），可直接進行換算
- 骨架屏（Shimmer）載入動畫

### 航班資訊（Flight）
- 查詢桃園國際機場即時航班（[KIA API](https://www.kia.gov.tw/API/InstantSchedule.ashx)）
- 支援依抵達狀態及地區篩選
- 每 10 秒自動刷新，支援手動下拉重新整理
- 點擊航班項目可開啟瀏覽器查看詳情
- 骨架屏（Shimmer）載入動畫

### 設定（Setting）
- 深色模式開關
- 自動同步開關及間隔設定（10 秒、1 分鐘、5 分鐘）
- 預設貨幣選擇（預設 USD）
- 顯示應用版本號

---

## 技術棧

| 類別 | 技術 |
|------|------|
| 語言 | Kotlin 2.0.21 |
| UI | Jetpack Compose (BOM 2024.12.01)、Material Design 3 |
| 架構 | Clean Architecture + MVVM + 單向資料流 (UDF) |
| 依賴注入 | Hilt 2.52 |
| 非同步 | Coroutines + Flow |
| 網路 | Retrofit 2.11.0 + OkHttp 4.12.0 + Moshi 1.15.1 |
| 本地儲存 | DataStore Preferences 1.1.1 |
| 導航 | AndroidX Navigation Compose 2.8.5 |
| 日誌 | Timber 5.0.1 |
| 除錯 | Flipper 0.260.0 |
| 測試 | JUnit 4、Mockk 1.13.13 |

---

## 架構

本專案採用 **Clean Architecture** 分三層設計，配合 **MVVM** 模式實現單向資料流。

```
┌─────────────────────────────────────┐
│          Presentation Layer          │
│  Composable UI ← ViewModel → State  │
│     (Feature Screens + Components)  │
├─────────────────────────────────────┤
│             Domain Layer             │
│   Repository Interfaces + Models    │
│            Use Cases                │
├─────────────────────────────────────┤
│              Data Layer             │
│  Repository Impl + Remote API       │
│       + Local DataStore             │
├─────────────────────────────────────┤
│           Network / Common          │
│  Interceptors + ApiResult + DI      │
└─────────────────────────────────────┘
```

### 資料流

```
UI (Composable)
    │  collect StateFlow
    ▼
ViewModel
    │  invoke
    ▼
Use Case
    │  call
    ▼
Repository Interface (Domain)
    │  implemented by
    ▼
Repository Impl (Data)
    │  fetch
    ▼
Remote API / Local DataStore
```

---

## 主要程式碼

### 應用入口

| 檔案 | 說明 |
|------|------|
| `MainActivity.kt` | 設定 `IssigroupTheme`、`NavHost`（底部導航列），並監聽 `MainViewModel` 的深色模式狀態 |
| `MainApplication.kt` | `@HiltAndroidApp` 入口點，初始化 Timber 日誌與 Flipper 除錯工具 |
| `MainViewModel.kt` | 從 `UserPreferencesDataStore` 讀取深色模式設定，以 `StateFlow` 暴露給 UI |

### 匯率換算

| 檔案 | 說明 |
|------|------|
| `ExchangeRateViewModel.kt` | 每 10 秒呼叫 `GetExchangeRatesUseCase`，透過 `StateFlow` 管理 `ExchangeRateUiState` |
| `ExchangeRateScreen.kt` | 顯示匯率列表、最後更新時間，包含下拉重新整理與 FloatingButton 觸發計算機 |
| `CalculatorBottomSheet.kt` | 數字鍵盤 UI，輸入金額後即時換算各幣別結果 |
| `ExchangeRateRepositoryImpl.kt` | 呼叫 `ExchangeRateApiService`，將 API 回應映射為 `ExchangeRate` 領域模型 |

### 航班資訊

| 檔案 | 說明 |
|------|------|
| `FlightViewModel.kt` | 每 10 秒呼叫 `GetFlightsUseCase`，處理篩選邏輯，以 `StateFlow` 管理 `FlightUiState` |
| `FlightScreen.kt` | 顯示航班列表、篩選選項（抵達狀態 / 地區），支援下拉重新整理 |
| `FlightEvent.kt` | 定義 UI 事件（開啟瀏覽器、顯示錯誤 SnackBar） |
| `FlightRepositoryImpl.kt` | 呼叫 `FlightApiService`（含 JS 重導攔截），映射為 `Flight` 領域模型 |

---

## API 資訊

| 功能 | API Endpoint |
|------|-------------|
| 匯率 | `https://api.freecurrencyapi.com/v1/latest` |
| 航班 | `https://www.kia.gov.tw/API/InstantSchedule.ashx` |

