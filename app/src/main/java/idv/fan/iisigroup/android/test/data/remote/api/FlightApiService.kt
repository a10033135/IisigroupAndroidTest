package idv.fan.iisigroup.android.test.data.remote.api

import idv.fan.iisigroup.android.test.data.remote.model.FlightResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface FlightApiService {

    @GET("API/InstantSchedule.ashx")
    suspend fun getFlights(
        @Query("AirFlyLine") airFlyLine: Int = 2,
        @Query("AirFlyIO") airFlyIO: Int = 2,
    ): List<FlightResponse>
}
