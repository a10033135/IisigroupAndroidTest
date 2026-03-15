package idv.fan.iisigroup.android.test.feature.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import idv.fan.iisigroup.android.test.BuildConfig
import idv.fan.iisigroup.android.test.data.local.datastore.UserPreferencesDataStore
import idv.fan.iisigroup.android.test.domain.model.SyncInterval
import idv.fan.iisigroup.android.test.ui.state.SettingUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore,
) : ViewModel() {

    val uiState: StateFlow<SettingUiState> = combine(
        userPreferencesDataStore.isDarkTheme,
        userPreferencesDataStore.autoSyncEnabled,
        userPreferencesDataStore.autoSyncIntervalMs,
    ) { isDark, autoSync, intervalMs ->
        SettingUiState(
            appVersion = BuildConfig.VERSION_NAME,
            isDarkTheme = isDark,
            autoSyncEnabled = autoSync,
            autoSyncInterval = SyncInterval.fromMs(intervalMs),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingUiState())

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { userPreferencesDataStore.setDarkTheme(enabled) }
    }

    fun setAutoSyncEnabled(enabled: Boolean) {
        viewModelScope.launch { userPreferencesDataStore.setAutoSyncEnabled(enabled) }
    }

    fun setAutoSyncInterval(interval: SyncInterval) {
        viewModelScope.launch { userPreferencesDataStore.setAutoSyncIntervalMs(interval.ms) }
    }
}
