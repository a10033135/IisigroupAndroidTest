package idv.fan.iisigroup.android.test.ui.state

import idv.fan.iisigroup.android.test.core.IssConstants
import idv.fan.iisigroup.android.test.domain.model.Currency
import idv.fan.iisigroup.android.test.domain.model.SyncInterval

data class SettingUiState(
    val appVersion: String = "",
    val isDarkTheme: Boolean = IssConstants.UserPreferences.DEFAULT_DARK_THEME,
    val autoSyncEnabled: Boolean = IssConstants.UserPreferences.DEFAULT_AUTO_SYNC_ENABLED,
    val autoSyncInterval: SyncInterval = IssConstants.UserPreferences.DEFAULT_SYNC_INTERVAL,
    val defaultCurrency: Currency = IssConstants.UserPreferences.DEFAULT_CURRENCY,
)
