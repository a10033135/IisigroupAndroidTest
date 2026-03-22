
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

## 功能介紹

各功能的頁面規格與技術文件請參閱對應的 md 檔案。

| 功能 | 說明 | 規格文件 |
|------|------|----------|
| 匯率換算 | 即時匯率查詢與計算機換算 | [exchangeRate.md](app/src/main/java/idv/fan/iisigroup/android/test/feature/exchangeRate/exchangeRate.md) |
| 航班資訊 | 桃園機場即時航班查詢與篩選 | [flight.md](app/src/main/java/idv/fan/iisigroup/android/test/feature/flight/flight.md) |
| 設定 | 深色模式、自動同步、貨幣偏好 | [setting.md](app/src/main/java/idv/fan/iisigroup/android/test/feature/setting/setting.md) |

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
