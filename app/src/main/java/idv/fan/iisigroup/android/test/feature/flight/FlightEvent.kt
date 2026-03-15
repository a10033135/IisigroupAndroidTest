package idv.fan.iisigroup.android.test.feature.flight

sealed class FlightEvent {
    data class OpenUrl(val url: String) : FlightEvent()
    data class ShowToast(val message: String) : FlightEvent()
}
