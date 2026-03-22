請依照本專案規範，審查「$ARGUMENTS」的程式碼，輸出問題清單與修正建議。

## 架構規範

- [ ] 符合 Clean Architecture 分層（`domain` / `data` / `feature`），各層職責未混用
- [ ] ViewModel 只透過 `StateFlow` 暴露 UI state，`SharedFlow` 發送一次性事件
- [ ] ViewModel 未直接使用 API Response model；資料已透過 UseCase + UiState 轉換
- [ ] Repository 作為資料單一來源，頁面未直接存取 DataSource

## MVVM / Coroutine

- [ ] 所有 Coroutine 使用 `viewModelScope`，未使用 `GlobalScope`
- [ ] StateFlow 對外只暴露唯讀介面（`.asStateFlow()`）
- [ ] SharedFlow 對外只暴露唯讀介面（`.asSharedFlow()`）

## Compose UI

- [ ] 使用 **functional Composable**，無 class-based Composable
- [ ] 顏色、文字大小等設計參數定義於 `ui/theme`，未在頁面寫死數值
- [ ] 可複用元件已抽至 `ui/components/`，未在多個頁面重複定義
- [ ] 單一 Composable function **不超過 300 行**
- [ ] 所有顯示文字透過 `R.string` 資源，無硬編碼中英文字串

## 網路層

- [ ] 使用 Retrofit + Moshi，未混用其他解析函式庫
- [ ] 裝置網路異常訊息格式為「網路異常，...」（非單純拋出原始 exception message）
- [ ] `runCatching` 正確處理 onSuccess / onFailure，無 try-catch 遺漏

## DI（Hilt）

- [ ] 無重複 `@Provides` / `@Binds` 綁定同一型別
- [ ] `DataStore<Preferences>` provider 統一在 `DataStoreModule`，其他 Module 未重複提供
- [ ] Singleton scope 使用正確，未在不該 Singleton 的地方標注

## DataStore

- [ ] 預設值統一定義於 `IssConstants` 物件，未在各處分散硬編碼

---

輸出格式：
**[嚴重度: 高/中/低]** `檔案路徑:行號` — 問題描述
> 建議修正：...