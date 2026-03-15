package idv.fan.iisigroup.android.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import idv.fan.iisigroup.android.test.data.local.datastore.UserPreferencesDataStore
import idv.fan.iisigroup.android.test.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore,
) : ViewModel() {

    private val _currentDestination = MutableStateFlow(AppDestinations.HOME)
    val currentDestination: StateFlow<AppDestinations> = _currentDestination.asStateFlow()

    val isDarkTheme: StateFlow<Boolean> = userPreferencesDataStore.isDarkTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun navigateTo(destination: AppDestinations) {
        _currentDestination.value = destination
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesDataStore.setDarkTheme(enabled)
        }
    }
}
