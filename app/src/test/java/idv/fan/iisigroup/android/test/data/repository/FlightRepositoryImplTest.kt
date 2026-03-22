package idv.fan.iisigroup.android.test.data.repository

import android.content.Context
import idv.fan.iisigroup.android.test.R
import idv.fan.iisigroup.android.test.data.remote.api.FlightApiService
import idv.fan.iisigroup.android.test.data.remote.model.FlightResponse
import idv.fan.iisigroup.android.test.domain.model.FlightStatus
import idv.fan.iisigroup.android.test.network.ApiResult
import idv.fan.iisigroup.android.test.network.NetworkException
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FlightRepositoryImplTest {

    private lateinit var apiService: FlightApiService
    private lateinit var context: Context
    private lateinit var repository: FlightRepositoryImpl

    private val unknownErrorMessage = "Unknown error occurred"

    private fun createFlightResponse(
        airFlyStatus: String? = "抵達",
        airLineName: String? = "Test Airline",
        upAirportName: String? = "Tokyo",
        airLineUrl: String? = "https://example.com",
    ) = FlightResponse(
        expectTime = "10:00",
        realTime = "10:05",
        airLineName = airLineName,
        airLineCode = "TA",
        airLineLogo = null,
        airLineUrl = airLineUrl,
        airLineNum = "TA001",
        upAirportCode = "TYO",
        upAirportName = upAirportName,
        airPlaneType = "737",
        airBoardingGate = "A1",
        airFlyStatus = airFlyStatus,
        airFlyDelayCause = null,
    )

    @Before
    fun setUp() {
        apiService = mockk()
        context = mockk()
        every { context.getString(R.string.error_unknown) } returns unknownErrorMessage
        repository = FlightRepositoryImpl(apiService, context)
    }

    // -------------------------------------------------------------------------
    // 1. getFlights success maps FlightResponse to Flight correctly
    // -------------------------------------------------------------------------
    @Test
    fun `getFlights success maps FlightResponse to Flight correctly`() = runTest {
        val response = createFlightResponse(airFlyStatus = "抵達", airLineName = "Test Airline")
        coEvery { apiService.getFlights() } returns listOf(response)

        val result = repository.getFlights()

        assertTrue(result is ApiResult.Success)
        val flights = (result as ApiResult.Success).data
        assertEquals(1, flights.size)
        val flight = flights[0]
        assertEquals("Test Airline", flight.airLineName)
        assertEquals(FlightStatus.ARRIVED, flight.airFlyStatus)
        assertEquals("Tokyo", flight.upAirportName)
        assertEquals("https://example.com", flight.airLineUrl)
    }

    // -------------------------------------------------------------------------
    // 2. getFlights maps airFlyStatus "抵達" to ARRIVED
    // -------------------------------------------------------------------------
    @Test
    fun `getFlights maps airFlyStatus 抵達 to ARRIVED`() = runTest {
        val response = createFlightResponse(airFlyStatus = "抵達")
        coEvery { apiService.getFlights() } returns listOf(response)

        val result = repository.getFlights() as ApiResult.Success

        assertEquals(FlightStatus.ARRIVED, result.data[0].airFlyStatus)
    }

    // -------------------------------------------------------------------------
    // 3. getFlights maps null airFlyStatus to UNKNOWN
    // -------------------------------------------------------------------------
    @Test
    fun `getFlights maps null airFlyStatus to UNKNOWN`() = runTest {
        val response = createFlightResponse(airFlyStatus = null)
        coEvery { apiService.getFlights() } returns listOf(response)

        val result = repository.getFlights() as ApiResult.Success

        assertEquals(FlightStatus.UNKNOWN, result.data[0].airFlyStatus)
    }

    // -------------------------------------------------------------------------
    // 4. getFlights NetworkException returns Error with network message
    // -------------------------------------------------------------------------
    @Test
    fun `getFlights NetworkException returns Error with network message`() = runTest {
        val networkMessage = "網路異常，請確認網路連線後再試"
        coEvery { apiService.getFlights() } throws NetworkException(networkMessage)

        val result = repository.getFlights()

        assertTrue(result is ApiResult.Error)
        assertEquals(networkMessage, (result as ApiResult.Error).message)
    }

    // -------------------------------------------------------------------------
    // 5. getFlights generic Exception returns Error with message
    // -------------------------------------------------------------------------
    @Test
    fun `getFlights generic Exception returns Error with message`() = runTest {
        coEvery { apiService.getFlights() } throws RuntimeException("unknown")

        val result = repository.getFlights()

        assertTrue(result is ApiResult.Error)
        assertEquals("unknown", (result as ApiResult.Error).message)
    }
}
