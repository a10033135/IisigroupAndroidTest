package idv.fan.iisigroup.android.test.domain.model

data class Flight(
    val expectTime: String?,
    val realTime: String?,
    val airLineName: String?,
    val airLineCode: String?,
    val airLineLogo: String?,
    val airLineUrl: String?,
    val airLineNum: String?,
    val upAirportCode: String?,
    val upAirportName: String?,
    val airPlaneType: String?,
    val airBoardingGate: String?,
    val airFlyStatus: FlightStatus,
    val airFlyDelayCause: String?,
)
