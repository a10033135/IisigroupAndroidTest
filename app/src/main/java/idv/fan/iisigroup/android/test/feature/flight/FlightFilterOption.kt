package idv.fan.iisigroup.android.test.feature.flight

sealed class FlightFilterOption {

    /** 固定篩選：airFlyStatus = 抵達 */
    data object Arrived : FlightFilterOption()

    /** 動態篩選：依 upAirportName 列出 */
    data class Region(val airportName: String) : FlightFilterOption()
}
