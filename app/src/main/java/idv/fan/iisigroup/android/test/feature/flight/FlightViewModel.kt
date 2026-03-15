package idv.fan.iisigroup.android.test.feature.flight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import idv.fan.iisigroup.android.test.domain.usecase.GetFlightsUseCase
import idv.fan.iisigroup.android.test.network.ApiResult
import idv.fan.iisigroup.android.test.ui.state.FlightUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FlightViewModel @Inject constructor(
    private val getFlightsUseCase: GetFlightsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<FlightUiState>(FlightUiState.Loading)
    val uiState: StateFlow<FlightUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var refreshJob: Job? = null

    init {
        loadFlights()
    }

    fun loadFlights() {
        loadJob?.cancel()
        refreshJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = FlightUiState.Loading
            when (val result = getFlightsUseCase()) {
                is ApiResult.Success -> {
                    _uiState.value = FlightUiState.Success(result.data)
                    startAutoRefresh()
                }
                is ApiResult.Error -> _uiState.value = FlightUiState.Error(result.message)
            }
        }
    }

    private fun startAutoRefresh() {
        refreshJob = viewModelScope.launch {
            while (true) {
                delay(REFRESH_INTERVAL_MS)
                val current = _uiState.value as? FlightUiState.Success ?: break
                _uiState.value = current.copy(isRefreshing = true, refreshError = null)
                when (val result = getFlightsUseCase()) {
                    is ApiResult.Success -> _uiState.value = FlightUiState.Success(result.data)
                    is ApiResult.Error -> _uiState.value = current.copy(
                        isRefreshing = false,
                        refreshError = result.message,
                    )
                }
            }
        }
    }

    companion object {
        const val REFRESH_INTERVAL_MS = 10_000L
    }
}
