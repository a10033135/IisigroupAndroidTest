package idv.fan.iisigroup.android.test.ui.state

import idv.fan.iisigroup.android.test.domain.model.Flight

sealed class FlightUiState {
    data object Loading : FlightUiState()
    data class Success(
        val flights: List<Flight>,
        val isRefreshing: Boolean = false,
        val refreshError: String? = null,
    ) : FlightUiState()
    data class Error(val message: String) : FlightUiState()
}
