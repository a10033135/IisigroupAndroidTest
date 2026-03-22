package idv.fan.iisigroup.android.test.feature.flight

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import idv.fan.iisigroup.android.test.R
import idv.fan.iisigroup.android.test.data.local.datastore.UserPreferencesDataStore
import idv.fan.iisigroup.android.test.core.IssConstants
import idv.fan.iisigroup.android.test.domain.model.Flight
import idv.fan.iisigroup.android.test.domain.model.FlightStatus
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class FlightViewModel @Inject constructor(
    private val getFlightsUseCase: GetFlightsUseCase,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow<FlightUiState>(FlightUiState.Loading)
    val uiState: StateFlow<FlightUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<FlightEvent>()
    val events: SharedFlow<FlightEvent> = _events.asSharedFlow()

    private var allFlights: List<Flight> = emptyList()
    private var currentFilters: Set<FlightFilterOption> = emptySet()

    private var currentAutoSyncEnabled: Boolean = IssConstants.UserPreferences.DEFAULT_AUTO_SYNC_ENABLED
    private var currentAutoSyncIntervalMs: Long = IssConstants.UserPreferences.DEFAULT_SYNC_INTERVAL.ms

    private var loadJob: Job? = null
    private var refreshJob: Job? = null

    init {
        observeAutoSyncSettings()
        loadFlights()
    }

    private fun observeAutoSyncSettings() {
        viewModelScope.launch {
            combine(
                userPreferencesDataStore.autoSyncEnabled,
                userPreferencesDataStore.autoSyncIntervalMs,
            ) { enabled, intervalMs -> enabled to intervalMs }
                .collect { (enabled, intervalMs) ->
                    val changed = enabled != currentAutoSyncEnabled || intervalMs != currentAutoSyncIntervalMs
                    currentAutoSyncEnabled = enabled
                    currentAutoSyncIntervalMs = intervalMs
                    if (changed && _uiState.value is FlightUiState.Success) {
                        refreshJob?.cancel()
                        if (enabled) startAutoRefresh()
                    }
                }
        }
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
                    if (currentAutoSyncEnabled) startAutoRefresh()
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
                    if (currentAutoSyncEnabled) startAutoRefresh()
                }
                is ApiResult.Error -> {
                    _uiState.value = current.copy(
                        isRefreshing = false,
                        refreshError = result.message,
                    )
                    if (currentAutoSyncEnabled) startAutoRefresh()
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
                _events.emit(FlightEvent.ShowToast(context.getString(R.string.flight_no_url_toast)))
            }
        }
    }

    private fun startAutoRefresh() {
        refreshJob = viewModelScope.launch {
            while (true) {
                delay(currentAutoSyncIntervalMs)
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
                    is FlightFilterOption.Arrived -> flight.airFlyStatus == FlightStatus.ARRIVED
                    is FlightFilterOption.Region -> flight.upAirportName == filter.airportName
                }
            }
        }
    }

    private fun currentTime(): String =
        LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
}
