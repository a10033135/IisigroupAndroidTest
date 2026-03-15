package idv.fan.iisigroup.android.test.feature.flight

sealed class FlightEvent {
    data class ShowRefreshError(val message: String) : FlightEvent()
}
