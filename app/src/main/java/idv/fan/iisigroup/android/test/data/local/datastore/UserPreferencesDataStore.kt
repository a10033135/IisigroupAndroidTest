package idv.fan.iisigroup.android.test.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import idv.fan.iisigroup.android.test.core.IssConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val isDarkTheme: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[DARK_THEME_KEY] ?: IssConstants.UserPreferences.DEFAULT_DARK_THEME
    }

    val autoSyncEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[AUTO_SYNC_ENABLED_KEY] ?: IssConstants.UserPreferences.DEFAULT_AUTO_SYNC_ENABLED
    }

    val autoSyncIntervalMs: Flow<Long> = dataStore.data.map { prefs ->
        prefs[AUTO_SYNC_INTERVAL_MS_KEY] ?: IssConstants.UserPreferences.DEFAULT_SYNC_INTERVAL.ms
    }

    val defaultCurrencyCode: Flow<String> = dataStore.data.map { prefs ->
        prefs[DEFAULT_CURRENCY_KEY] ?: IssConstants.UserPreferences.DEFAULT_CURRENCY.code
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

    suspend fun setDefaultCurrencyCode(code: String) {
        dataStore.edit { prefs -> prefs[DEFAULT_CURRENCY_KEY] = code }
    }

    companion object {
        val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
        val AUTO_SYNC_ENABLED_KEY = booleanPreferencesKey("auto_sync_enabled")
        val AUTO_SYNC_INTERVAL_MS_KEY = longPreferencesKey("auto_sync_interval_ms")
        val DEFAULT_CURRENCY_KEY = stringPreferencesKey("default_currency")
    }
}
