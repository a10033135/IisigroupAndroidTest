package idv.fan.iisigroup.android.test.domain.repository

import idv.fan.iisigroup.android.test.domain.model.Flight
import idv.fan.iisigroup.android.test.network.ApiResult

interface FlightRepository {
    suspend fun getFlights(): ApiResult<List<Flight>>
}
