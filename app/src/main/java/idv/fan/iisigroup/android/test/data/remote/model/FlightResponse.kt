package idv.fan.iisigroup.android.test.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FlightResponse(
    @Json(name = "expectTime") val expectTime: String?,
    @Json(name = "realTime") val realTime: String?,
    @Json(name = "airLineName") val airLineName: String?,
    @Json(name = "airLineCode") val airLineCode: String?,
    @Json(name = "airLineLogo") val airLineLogo: String?,
    @Json(name = "airLineUrl") val airLineUrl: String?,
    @Json(name = "airLineNum") val airLineNum: String?,
    @Json(name = "upAirportCode") val upAirportCode: String?,
    @Json(name = "upAirportName") val upAirportName: String?,
    @Json(name = "airPlaneType") val airPlaneType: String?,
    @Json(name = "airBoardingGate") val airBoardingGate: String?,
    @Json(name = "airFlyStatus") val airFlyStatus: String?,
    @Json(name = "airFlyDelayCause") val airFlyDelayCause: String?,
)
