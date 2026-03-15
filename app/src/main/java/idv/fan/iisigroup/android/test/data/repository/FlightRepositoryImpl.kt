package idv.fan.iisigroup.android.test.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import idv.fan.iisigroup.android.test.R
import idv.fan.iisigroup.android.test.data.remote.api.FlightApiService
import idv.fan.iisigroup.android.test.data.remote.model.FlightResponse
import idv.fan.iisigroup.android.test.domain.model.Flight
import idv.fan.iisigroup.android.test.domain.repository.FlightRepository
import idv.fan.iisigroup.android.test.network.ApiResult
import javax.inject.Inject

class FlightRepositoryImpl @Inject constructor(
    private val apiService: FlightApiService,
    @ApplicationContext private val context: Context,
) : FlightRepository {

    override suspend fun getFlights(): ApiResult<List<Flight>> = runCatching {
        apiService.getFlights().map { it.toDomain() }
    }.fold(
        onSuccess = { ApiResult.Success(it) },
        onFailure = { ApiResult.Error(it.message ?: context.getString(R.string.error_unknown), it) },
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
