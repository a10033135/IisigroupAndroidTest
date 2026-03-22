package idv.fan.iisigroup.android.test.domain.usecase

import idv.fan.iisigroup.android.test.domain.model.Flight
import idv.fan.iisigroup.android.test.domain.model.FlightStatus
import idv.fan.iisigroup.android.test.domain.repository.FlightRepository
import idv.fan.iisigroup.android.test.network.ApiResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetFlightsUseCaseTest {

    private lateinit var repository: FlightRepository
    private lateinit var useCase: GetFlightsUseCase

    private fun createFlight() = Flight(
        expectTime = "10:00",
        realTime = "10:05",
        airLineName = "Test Airline",
        airLineCode = "TA",
        airLineLogo = null,
        airLineUrl = "https://example.com",
        airLineNum = "TA001",
        upAirportCode = "TYO",
        upAirportName = "Tokyo",
        airPlaneType = "737",
        airBoardingGate = "A1",
        airFlyStatus = FlightStatus.UNKNOWN,
        airFlyDelayCause = null,
    )

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetFlightsUseCase(repository)
    }

    // -------------------------------------------------------------------------
    // 1. returns Success when repository returns success
    // -------------------------------------------------------------------------
    @Test
    fun `returns Success when repository returns success`() = runTest {
        val flights = listOf(createFlight())
        coEvery { repository.getFlights() } returns ApiResult.Success(flights)

        val result = useCase()

        assertTrue(result is ApiResult.Success)
        assertEquals(flights, (result as ApiResult.Success).data)
        coVerify(exactly = 1) { repository.getFlights() }
    }

    // -------------------------------------------------------------------------
    // 2. returns Error when repository returns error
    // -------------------------------------------------------------------------
    @Test
    fun `returns Error when repository returns error`() = runTest {
        coEvery { repository.getFlights() } returns ApiResult.Error("Something went wrong")

        val result = useCase()

        assertTrue(result is ApiResult.Error)
        assertEquals("Something went wrong", (result as ApiResult.Error).message)
        coVerify(exactly = 1) { repository.getFlights() }
    }
}
