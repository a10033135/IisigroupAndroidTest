請依照專案的 NavHost 原則，為「$ARGUMENTS」建立完整的頁面結構。

## 需要建立的檔案

### 1. `***Navigation.kt`
- 定義 NavGraph route（使用最新 typed route 方式）
- 在 NavHost 中呼叫 `***Route`

### 2. `***Route.kt`
- 整合 Screen 與 HiltViewModel
- 以 `collectAsStateWithLifecycle()` 收集 `uiState`
- 以 `LaunchedEffect` 收集 `events` SharedFlow，處理導航與 Toast

### 3. `***Screen.kt`（純 UI，zero business logic）
- 只接收 uiState 與 callback lambda，不依賴 ViewModel
- 採 **functional Composable**，禁用 class-based
- 顏色、文字大小等設計參數統一定義於 `ui/theme`，禁止頁面內寫死
- 可複用元件抽至 `ui/components/`
- 每個 Composable function **不超過 300 行**
- 所有顯示文字透過 `R.string` 資源管理，禁止硬編碼字串

### 4. `***ViewModel.kt`
遵守 MVVM 單向資料流：
- `MutableStateFlow` → 對外只暴露 `StateFlow<***UiState>`
- `MutableSharedFlow` → 對外只暴露 `SharedFlow<***Event>` 發送一次性事件
- 所有 Coroutine 使用 `viewModelScope`
- 不直接持有 API Response model，資料透過 UseCase 取得後轉為 domain model

### 5. `***UiState.kt`
```kotlin
sealed class ***UiState {
    data object Loading : ***UiState()
    data class Success(...) : ***UiState()
    data class Error(val message: String) : ***UiState()
}
```

### 6. `***Event.kt`（如有需要）
```kotlin
sealed class ***Event {
    data class NavigateTo(...) : ***Event()
    data class ShowToast(val message: String) : ***Event()
}
```

## 完成後
依功能拆解 commit 並自動提交。