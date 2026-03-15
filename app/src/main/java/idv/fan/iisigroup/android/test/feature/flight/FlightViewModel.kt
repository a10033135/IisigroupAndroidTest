package idv.fan.iisigroup.android.test.feature.flight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import idv.fan.iisigroup.android.test.domain.model.Flight
import idv.fan.iisigroup.android.test.domain.usecase.GetFlightsUseCase
import idv.fan.iisigroup.android.test.network.ApiResult
import idv.fan.iisigroup.android.test.ui.state.FlightUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class FlightViewModel @Inject constructor(
    private val getFlightsUseCase: GetFlightsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<FlightUiState>(FlightUiState.Loading)
    val uiState: StateFlow<FlightUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<FlightEvent>()
    val events: SharedFlow<FlightEvent> = _events.asSharedFlow()

    private var allFlights: List<Flight> = emptyList()
    private var currentFilters: Set<FlightFilterOption> = emptySet()

    private var loadJob: Job? = null
    private var refreshJob: Job? = null

    init {
        loadFlights()
    }

    fun loadFlights() {
        loadJob?.cancel()
        refreshJob?.cancel()
        currentFilters = emptySet()
        loadJob = viewModelScope.launch {
            Timber.d("Loading flights")
            _uiState.value = FlightUiState.Loading
            when (val result = getFlightsUseCase()) {
                is ApiResult.Success -> {
                    Timber.d("Loaded ${result.data.size} flights")
                    allFlights = result.data
                    _uiState.value = buildSuccessState(currentTime())
                    startAutoRefresh()
                }
                is ApiResult.Error -> {
                    Timber.e("Load failed: ${result.message}")
                    _uiState.value = FlightUiState.Error(result.message)
                }
            }
        }
    }

    fun pullToRefresh() {
        val current = _uiState.value as? FlightUiState.Success ?: return
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _uiState.value = current.copy(isRefreshing = true, refreshError = null)
            when (val result = getFlightsUseCase()) {
                is ApiResult.Success -> {
                    allFlights = result.data
                    _uiState.value = buildSuccessState(currentTime())
                    startAutoRefresh()
                }
                is ApiResult.Error -> {
                    _uiState.value = current.copy(
                        isRefreshing = false,
                        refreshError = result.message,
                    )
                    startAutoRefresh()
                }
            }
        }
    }

    fun onFilterToggle(filter: FlightFilterOption) {
        currentFilters = if (filter in currentFilters) currentFilters - filter else currentFilters + filter
        val current = _uiState.value as? FlightUiState.Success ?: return
        _uiState.value = current.copy(
            flights = applyFilters(),
            selectedFilters = currentFilters,
        )
    }

    fun onFlightClick(flight: Flight) {
        viewModelScope.launch {
            if (!flight.airLineUrl.isNullOrEmpty()) {
                _events.emit(FlightEvent.OpenUrl(flight.airLineUrl))
            } else {
                _events.emit(FlightEvent.ShowToast("此航班沒有提供連結"))
            }
        }
    }

    private fun startAutoRefresh() {
        refreshJob = viewModelScope.launch {
            while (true) {
                delay(REFRESH_INTERVAL_MS)
                val current = _uiState.value as? FlightUiState.Success ?: break
                Timber.d("Auto-refreshing flights")
                _uiState.value = current.copy(isRefreshing = true, refreshError = null)
                when (val result = getFlightsUseCase()) {
                    is ApiResult.Success -> {
                        allFlights = result.data
                        _uiState.value = buildSuccessState(currentTime())
                    }
                    is ApiResult.Error -> {
                        _uiState.value = current.copy(
                            isRefreshing = false,
                            refreshError = result.message,
                        )
                    }
                }
            }
        }
    }

    private fun buildSuccessState(refreshTime: String): FlightUiState.Success =
        FlightUiState.Success(
            flights = applyFilters(),
            lastRefreshTime = refreshTime,
            availableFilters = buildAvailableFilters(),
            selectedFilters = currentFilters,
        )

    private fun buildAvailableFilters(): List<FlightFilterOption> {
        val regions = allFlights
            .mapNotNull { it.upAirportName?.takeIf { name -> name.isNotBlank() } }
            .distinct()
            .sorted()
            .map { FlightFilterOption.Region(it) }
        return listOf(FlightFilterOption.Arrived) + regions
    }

    private fun applyFilters(): List<Flight> {
        if (currentFilters.isEmpty()) return allFlights
        return allFlights.filter { flight ->
            currentFilters.any { filter ->
                when (filter) {
                    is FlightFilterOption.Arrived -> flight.airFlyStatus == "抵達"
                    is FlightFilterOption.Region -> flight.upAirportName == filter.airportName
                }
            }
        }
    }

    private fun currentTime(): String =
        LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))

    companion object {
        const val REFRESH_INTERVAL_MS = 10_000L
    }
}
