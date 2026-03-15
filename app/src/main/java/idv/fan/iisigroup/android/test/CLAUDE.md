### 專案架構
- 語言：Kotlin / Compose 
- 架構：CleanArchitecture / MVVM
- 使用 Room 與 DataStore 
- 注入：Hilt
- 網路： Retrofit 
- 解析使用 moshi
- 異步：coroutine
- 需撰寫 ComposeUI test 及單元測試

### Code Rules
- 使用 functional Composable，不用 class-based

### Design Guideline
- 採用 Material Design 3，優先使用 ut.theme 
- 將使用到的顏色及文字大小等設計元素，抽出在 ui.theme ，不在頁面直接寫死參數

### MVVM 原則
- ViewModel 只透過 StateFlow 暴露 UI state
- Repository pattern 作為資料單一來源
- 所有 Coroutine 使用 viewModelScope

### 每次使用 CLAUDE 處理後需自動下 Commit 