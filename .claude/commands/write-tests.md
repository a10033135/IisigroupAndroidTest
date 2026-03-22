請為「$ARGUMENTS」撰寫完整測試。

## 單元測試（`src/test/`）

### ViewModel 測試
- 使用 `kotlinx-coroutines-test` + `Turbine` 測試 `StateFlow` / `SharedFlow`
- Mock repository 以 `mockk` 或 `fake` 實作
- 覆蓋情境：
  - [ ] 初始化載入（Loading → Success）
  - [ ] API 失敗（Loading → Error）
  - [ ] Pull-to-refresh 成功 / 失敗
  - [ ] 用戶操作事件（filter toggle、click 等）

### UseCase 測試
- 驗證業務邏輯（資料轉換、過濾條件等）
- 驗證 Repository 異常時正確傳遞 `ApiResult.Error`

### Repository 測試
- Mock `ApiService`，驗證 `toDomain()` 轉換正確性
- 驗證網路異常時回傳 `ApiResult.Error`，訊息包含「網路異常」

---

## Compose UI 測試（`src/androidTest/`）

### Screen 狀態測試
針對各 `UiState` 驗證畫面元件：
- [ ] `Loading` 狀態：Shimmer 或 loading indicator 顯示
- [ ] `Success` 狀態：資料列表 / 內容正確渲染
- [ ] `Error` 狀態：錯誤訊息與 Retry 按鈕顯示

### 互動測試
- [ ] 按鈕點擊觸發正確 callback
- [ ] 列表捲動、篩選等操作行為
- [ ] Dialog 開啟 / 關閉流程

---

## 完成後
依測試類型拆解 commit 並自動提交。