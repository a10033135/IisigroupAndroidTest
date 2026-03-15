package idv.fan.iisigroup.android.test.domain.usecase

import idv.fan.iisigroup.android.test.domain.model.Flight
import idv.fan.iisigroup.android.test.domain.repository.FlightRepository
import idv.fan.iisigroup.android.test.network.ApiResult
import javax.inject.Inject

class GetFlightsUseCase @Inject constructor(
    private val repository: FlightRepository,
) {
    suspend operator fun invoke(): ApiResult<List<Flight>> = repository.getFlights()
}
