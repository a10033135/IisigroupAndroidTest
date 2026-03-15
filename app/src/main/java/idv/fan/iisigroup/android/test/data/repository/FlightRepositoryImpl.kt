package idv.fan.iisigroup.android.test.data.repository

import idv.fan.iisigroup.android.test.data.remote.api.FlightApiService
import idv.fan.iisigroup.android.test.data.remote.model.FlightResponse
import idv.fan.iisigroup.android.test.domain.model.Flight
import idv.fan.iisigroup.android.test.domain.repository.FlightRepository
import idv.fan.iisigroup.android.test.network.ApiResult
import java.net.ConnectException
import java.net.SocketException
import java.net.UnknownHostException
import javax.inject.Inject

class FlightRepositoryImpl @Inject constructor(
    private val apiService: FlightApiService,
) : FlightRepository {

    override suspend fun getFlights(): ApiResult<List<Flight>> = runCatching {
        apiService.getFlights().map { it.toDomain() }
    }.fold(
        onSuccess = { ApiResult.Success(it) },
        onFailure = { throwable ->
            val message = when (throwable) {
                is UnknownHostException, is SocketException, is ConnectException ->
                    "網路異常，請確認網路連線後再試"
                else -> throwable.message ?: "發生未知錯誤"
            }
            ApiResult.Error(message, throwable)
        },
    )

    private fun FlightResponse.toDomain() = Flight(
        expectTime = expectTime,
        realTime = realTime,
        airLineName = airLineName,
        airLineCode = airLineCode,
        airLineLogo = airLineLogo,
        airLineUrl = airLineUrl,
        airLineNum = airLineNum,
        upAirportCode = upAirportCode,
        upAirportName = upAirportName,
        airPlaneType = airPlaneType,
        airBoardingGate = airBoardingGate,
        airFlyStatus = airFlyStatus,
        airFlyDelayCause = airFlyDelayCause,
    )
}
