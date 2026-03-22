
# IisigroupAndroidTest

一個以 Jetpack Compose 建構的 Android 應用程式，提供即時匯率換算、桃園機場航班查詢及個人化設定功能。

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

## 專案結構

```
app/src/main/java/idv/fan/iisigroup/android/test/
│
├── MainActivity.kt                    # 應用入口，設定 NavHost 及主題
├── MainApplication.kt                 # Application 類別（Hilt 注入點）
├── MainViewModel.kt                   # 管理全域深色模式狀態
│
├── core/
│   └── IssConstants.kt                # 應用全域常數與預設值
│
├── network/                           # 網路共用元件
│   ├── ApiResult.kt                   # 封裝 API 結果（Success / Error）
│   ├── NetworkException.kt            # 自訂網路異常類別
│   ├── ApiKeyInterceptor.kt           # 自動附加 API Key 攔截器
│   ├── NetworkErrorInterceptor.kt     # 統一處理 HTTP 錯誤攔截器
│   └── FlightJsRedirectInterceptor.kt # 處理 KIA 航班 API JS 重導
│
├── data/
│   ├── remote/
│   │   ├── api/
│   │   │   ├── ExchangeRateApiService.kt  # 匯率 Retrofit 介面
│   │   │   └── FlightApiService.kt        # 航班 Retrofit 介面
│   │   └── model/
│   │       ├── ExchangeRateResponse.kt    # 匯率 API 回應模型
│   │       └── FlightResponse.kt          # 航班 API 回應模型
│   ├── local/
│   │   └── datastore/
│   │       └── UserPreferencesDataStore.kt # DataStore 偏好設定存取
│   └── repository/
│       ├── ExchangeRateRepositoryImpl.kt  # 匯率儲存庫實作
│       └── FlightRepositoryImpl.kt        # 航班儲存庫實作
│
├── domain/
│   ├── model/
│   │   ├── Currency.kt                # 貨幣 enum（USD/JPY/KRW/CNY/THB/GBP）
│   │   ├── ExchangeRate.kt            # 匯率資料模型
│   │   ├── Flight.kt                  # 航班資料模型
│   │   └── SyncInterval.kt            # 同步間隔 enum（10s/1m/5m）
│   ├── repository/
│   │   ├── ExchangeRateRepository.kt  # 匯率儲存庫介面
│   │   └── FlightRepository.kt        # 航班儲存庫介面
│   └── usecase/
│       ├── GetExchangeRatesUseCase.kt # 取得匯率 Use Case
│       └── GetFlightsUseCase.kt       # 取得航班 Use Case
│
├── di/                                # Hilt 依賴注入模組
│   ├── NetworkModule.kt               # Moshi JSON 設定
│   ├── ExchangeRateNetworkModule.kt   # 匯率 Retrofit 實例
│   ├── FlightNetworkModule.kt         # 航班 Retrofit 實例
│   ├── ExchangeRateRepositoryModule.kt # 匯率 Repository 綁定
│   ├── RepositoryModule.kt            # 航班 Repository 綁定
│   └── DatabaseModule.kt             # DataStore 實例
│
├── ui/
│   ├── theme/
│   │   ├── Color.kt                   # 顏色定義
│   │   ├── Theme.kt                   # Material Design 3 主題設定
│   │   └── Type.kt                    # 文字排版定義
│   ├── components/
│   │   ├── ExchangeRateShimmer.kt     # 匯率骨架屏
│   │   └── FlightShimmer.kt           # 航班骨架屏
│   └── state/
│       ├── ExchangeRateUiState.kt     # 匯率 UI 狀態（Loading/Success/Error）
│       ├── FlightUiState.kt           # 航班 UI 狀態
│       └── SettingUiState.kt          # 設定 UI 狀態
│
└── feature/
    ├── exchangeRate/                  # 匯率換算功能
    │   ├── ExchangeRateScreen.kt
    │   ├── ExchangeRateViewModel.kt
    │   ├── ExchangeRateRoute.kt
    │   ├── ExchangeRateNavigation.kt
    │   └── CalculatorBottomSheet.kt
    ├── flight/                        # 航班資訊功能
    │   ├── FlightScreen.kt
    │   ├── FlightViewModel.kt
    │   ├── FlightRoute.kt
    │   ├── FlightNavigation.kt
    │   ├── FlightEvent.kt
    │   └── FlightFilterOption.kt
    └── setting/                       # 設定功能
        ├── SettingScreen.kt
        ├── SettingViewModel.kt
        ├── SettingRoute.kt
        └── SettingNavigation.kt
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

### 設定

| 檔案 | 說明 |
|------|------|
| `SettingViewModel.kt` | 讀寫 `UserPreferencesDataStore`，管理深色模式、自動同步及預設貨幣 |
| `SettingScreen.kt` | 提供深色模式切換、同步間隔 DropdownMenu、預設貨幣選擇及版本顯示 |

### 網路層

| 檔案 | 說明 |
|------|------|
| `ApiResult.kt` | sealed class 封裝 `Success<T>` 和 `Error`，避免直接拋出例外 |
| `ApiKeyInterceptor.kt` | 為所有匯率 API 請求自動附加 `apikey` 查詢參數 |
| `NetworkErrorInterceptor.kt` | 攔截非 2xx 回應，轉換為 `NetworkException` |
| `FlightJsRedirectInterceptor.kt` | 解析 KIA 航班 API 返回的 JavaScript 重導向，取得真實資料 URL |

### 依賴注入

| 檔案 | 說明 |
|------|------|
| `ExchangeRateNetworkModule.kt` | 建立含 `ApiKeyInterceptor` 的 Retrofit 實例（匯率 API 專用） |
| `FlightNetworkModule.kt` | 建立含 `FlightJsRedirectInterceptor` 的 Retrofit 實例（航班 API 專用） |
| `DatabaseModule.kt` | 建立 `UserPreferencesDataStore` 單例 |

---

## API 資訊

| 功能 | API Endpoint |
|------|-------------|
| 匯率 | `https://api.freecurrencyapi.com/v1/latest` |
| 航班 | `https://www.kia.gov.tw/API/InstantSchedule.ashx` |

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