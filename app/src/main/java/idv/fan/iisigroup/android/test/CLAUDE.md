### 專案說明
- 語言：Kotlin / Compose 
- 本地儲存：DataStore，預設值建立於 Constants 物件內統一管理
- 注入：Hilt
- 異步：coroutine

### 專案架構
- 採 CleanArchitecture
- MVVM 遵守單向資料流，由 StateFlow 掌握畫面狀態，由 SharedFlow 掌握發送事件
- 當接到 API Response 時，使用 UiState 轉為頁面所需的資料，不直接在 ViewModel 使用 Response

### 網路層
- Retrofit
- 解析使用 moshi
- 針對網路異常(用戶沒有開啟網路)等裝置網路問題，拋出：「網路異常，...」，將實際裝置網路問題回報給用戶

### Code Rules
- 使用 functional Composable，不用 class-based

### Design Guideline
- 採用 Material Design 3，優先使用 ut.theme 
- 將使用到的顏色及文字大小等設計元素，抽出在 ui.theme ，不在頁面直接寫死參數
- 請盡量將元件抽出至 ui/components 之中，ComposeFunction 不要超過300行
- 請將文字抽出放置於 resource/strings

### MVVM 原則
- ViewModel 只透過 StateFlow 暴露 UI state
- Repository pattern 作為資料單一來源
- 所有 Coroutine 使用 viewModelScope

### NavHost 原則
- 為 MainActivity 的 NavigationSuiteScaffold 實作 NavHost 
- 將各頁面的 Routing 抽出為 ***Navigation ，並將頁面 Route 使用最新的
- 將 routing 拆成 ***Route(將Screen及ViewModel整合) ***Screen(純UI)

### Debug Tool
- Flipper：
  - 支援 Network 監測
  - 透過 package 建立 production / debug 的 initFlipper function，根據 build variant 在 Debug 時才會 init
- Timber：Logger 

### 指令原則
- 每次使用 CLAUDE 處理後需拆解 Commit 並自動進 Commit
- 需撰寫 ComposeUI test 及單元測試