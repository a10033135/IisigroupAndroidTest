package idv.fan.iisigroup.android.test.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import idv.fan.iisigroup.android.test.domain.model.SyncInterval
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val isDarkTheme: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[DARK_THEME_KEY] ?: false
    }

    val autoSyncEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[AUTO_SYNC_ENABLED_KEY] ?: true
    }

    val autoSyncIntervalMs: Flow<Long> = dataStore.data.map { prefs ->
        prefs[AUTO_SYNC_INTERVAL_MS_KEY] ?: SyncInterval.default.ms
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[DARK_THEME_KEY] = enabled }
    }

    suspend fun setAutoSyncEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[AUTO_SYNC_ENABLED_KEY] = enabled }
    }

    suspend fun setAutoSyncIntervalMs(ms: Long) {
        dataStore.edit { prefs -> prefs[AUTO_SYNC_INTERVAL_MS_KEY] = ms }
    }

    companion object {
        val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
        val AUTO_SYNC_ENABLED_KEY = booleanPreferencesKey("auto_sync_enabled")
        val AUTO_SYNC_INTERVAL_MS_KEY = longPreferencesKey("auto_sync_interval_ms")
    }
}
