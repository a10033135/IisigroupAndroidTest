package idv.fan.iisigroup.android.test

import idv.fan.iisigroup.android.test.domain.model.Flight
import idv.fan.iisigroup.android.test.domain.usecase.GetFlightsUseCase
import idv.fan.iisigroup.android.test.feature.flight.FlightViewModel
import idv.fan.iisigroup.android.test.network.ApiResult
import idv.fan.iisigroup.android.test.ui.state.FlightUiState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FlightViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val mockGetFlightsUseCase = mockk<GetFlightsUseCase>()
    private lateinit var viewModel: FlightViewModel

    private val testFlight = Flight(
        expectTime = "09:15",
        realTime = "09:13",
        airLineName = "立榮航空",
        airLineCode = "UIA",
        airLineLogo = null,
        airLineUrl = null,
        airLineNum = "B78690",
        upAirportCode = "MZG",
        upAirportName = "澎湖",
        airPlaneType = "AT76",
        airBoardingGate = "15",
        airFlyStatus = "抵達",
        airFlyDelayCause = "",
    )

    @Before
    fun setup() {
        coEvery { mockGetFlightsUseCase() } returns ApiResult.Success(emptyList())
        viewModel = FlightViewModel(mockGetFlightsUseCase)
    }

    @Test
    fun `initial state is Loading before coroutines run`() {
        assertTrue(viewModel.uiState.value is FlightUiState.Loading)
    }

    @Test
    fun `loadFlights success sets Success state`() = runTest(testDispatcher) {
        val flights = listOf(testFlight)
        coEvery { mockGetFlightsUseCase() } returns ApiResult.Success(flights)

        viewModel.loadFlights()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is FlightUiState.Success)
        assertEquals(flights, (state as FlightUiState.Success).flights)
    }

    @Test
    fun `loadFlights error sets Error state`() = runTest(testDispatcher) {
        coEvery { mockGetFlightsUseCase() } returns ApiResult.Error("Network error")

        viewModel.loadFlights()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is FlightUiState.Error)
        assertEquals("Network error", (state as FlightUiState.Error).message)
    }

    @Test
    fun `auto refresh updates flights after 10 seconds`() = runTest(testDispatcher) {
        val initial = listOf(testFlight)
        val refreshed = listOf(testFlight, testFlight.copy(airLineNum = "B78691"))
        coEvery { mockGetFlightsUseCase() } returnsMany listOf(
            ApiResult.Success(initial),
            ApiResult.Success(refreshed),
        )

        viewModel.loadFlights()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is FlightUiState.Success)

        advanceTimeBy(FlightViewModel.REFRESH_INTERVAL_MS + 1)
        advanceUntilIdle()

        val state = viewModel.uiState.value as FlightUiState.Success
        assertEquals(2, state.flights.size)
        assertNull(state.refreshError)
    }

    @Test
    fun `auto refresh failure shows error banner without entering Error state`() = runTest(testDispatcher) {
        coEvery { mockGetFlightsUseCase() } returnsMany listOf(
            ApiResult.Success(listOf(testFlight)),
            ApiResult.Error("Refresh failed"),
        )

        viewModel.loadFlights()
        advanceUntilIdle()

        advanceTimeBy(FlightViewModel.REFRESH_INTERVAL_MS + 1)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is FlightUiState.Success)
        assertEquals("Refresh failed", (state as FlightUiState.Success).refreshError)
        assertEquals(1, state.flights.size)
    }

    @Test
    fun `loadFlights cancels ongoing refresh and reloads`() = runTest(testDispatcher) {
        val initial = listOf(testFlight)
        val retried = listOf(testFlight.copy(airFlyStatus = "準時"))
        coEvery { mockGetFlightsUseCase() } returnsMany listOf(
            ApiResult.Success(initial),
            ApiResult.Success(retried),
        )

        viewModel.loadFlights()
        advanceUntilIdle()

        // Call loadFlights again (simulates retry)
        viewModel.loadFlights()
        advanceUntilIdle()

        val state = viewModel.uiState.value as FlightUiState.Success
        assertEquals("準時", state.flights.first().airFlyStatus)
    }
}
