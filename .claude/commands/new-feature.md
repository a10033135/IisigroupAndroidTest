請依照專案的 Clean Architecture，為「$ARGUMENTS」建立完整功能。

## Domain 層

### `domain/model/***Model.kt`
- Pure Kotlin data class，不含任何 Android / Retrofit / Moshi 依賴
- 欄位型別盡量使用強型別（enum / sealed class），避免裸 String

### `domain/repository/***Repository.kt`
- Interface，定義資料操作 contract
- 回傳 `ApiResult<T>`（`Success` / `Error`）

### `domain/usecase/Get***UseCase.kt`
- 單一職責，從 Repository 取得資料並做必要的業務邏輯轉換
- 以 `operator fun invoke()` 呼叫

---

## Data 層

### `data/remote/model/***Response.kt`
- Moshi `@JsonClass(generateAdapter = true)` data class
- 欄位命名對應 JSON key（或搭配 `@Json(name = ...)`)

### `data/remote/api/***ApiService.kt`
- Retrofit `interface`，`suspend fun` 回傳 Response model

### `data/repository/***RepositoryImpl.kt`
- 實作 `***Repository` interface
- 以 `runCatching { }.fold(onSuccess, onFailure)` 處理結果
- 裝置網路問題（無網路等）拋出訊息：`「網路異常，請檢查網路連線後再試」`
- 提供 `private fun ***Response.toDomain()` 轉換函式

---

## DI 層

### `di/***Module.kt`
- `@Binds` 將 `***RepositoryImpl` 綁定至 `***Repository`
- `@Singleton` scope

---

## Feature 層

參考 `/new-screen` skill 建立：
`***Navigation` / `***Route` / `***Screen` / `***ViewModel` / `***UiState`

---

## 完成後
依功能拆解 commit 並自動提交。