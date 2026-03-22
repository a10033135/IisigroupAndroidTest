package idv.fan.iisigroup.android.test.extensions

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import idv.fan.iisigroup.android.test.core.IssConstants

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = IssConstants.DataStore.USER_PREFERENCES_NAME,
)
