package idv.fan.iisigroup.android.test.ui.state

import idv.fan.iisigroup.android.test.domain.model.Flight
import idv.fan.iisigroup.android.test.feature.flight.FlightFilterOption

data class FlightApiState(
    val flights: List<Flight>,
    val availableFilters: List<FlightFilterOption>,
    val lastRefreshTime: String,
    val isRefreshing: Boolean = false,
    val refreshError: String? = null,
)

data class FlightUserState(
    val selectedFilters: Set<FlightFilterOption> = emptySet(),
)

sealed class FlightUiState {
    data object Loading : FlightUiState()
    data class Success(
        val apiState: FlightApiState,
        val userState: FlightUserState = FlightUserState(),
    ) : FlightUiState()
    data class Error(val message: String) : FlightUiState()
}
