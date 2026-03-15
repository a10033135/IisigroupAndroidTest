package idv.fan.iisigroup.android.test.ui.state

import idv.fan.iisigroup.android.test.domain.model.SyncInterval

data class SettingUiState(
    val appVersion: String = "",
    val isDarkTheme: Boolean = false,
    val autoSyncEnabled: Boolean = true,
    val autoSyncInterval: SyncInterval = SyncInterval.default,
)
