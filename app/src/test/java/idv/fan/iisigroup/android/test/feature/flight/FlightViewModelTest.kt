package idv.fan.iisigroup.android.test.feature.flight

import android.content.Context
import idv.fan.iisigroup.android.test.MainDispatcherRule
import idv.fan.iisigroup.android.test.R
import idv.fan.iisigroup.android.test.data.local.datastore.UserPreferencesDataStore
import idv.fan.iisigroup.android.test.domain.model.Flight
import idv.fan.iisigroup.android.test.domain.model.FlightStatus
import idv.fan.iisigroup.android.test.domain.usecase.GetFlightsUseCase
import idv.fan.iisigroup.android.test.network.ApiResult
import idv.fan.iisigroup.android.test.ui.state.FlightUiState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FlightViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private lateinit var getFlightsUseCase: GetFlightsUseCase
    private lateinit var userPreferencesDataStore: UserPreferencesDataStore
    private lateinit var context: Context
    private lateinit var viewModel: FlightViewModel

    private val toastMessage = "No airline URL available"

    private fun createFlight(
        airLineUrl: String? = "https://example.com",
        airFlyStatus: FlightStatus = FlightStatus.UNKNOWN,
        upAirportName: String? = "Tokyo",
        airLineName: String? = "Test Airline",
    ) = Flight(
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
        getFlightsUseCase = mockk()
        userPreferencesDataStore = mockk()
        context = mockk()

        every { userPreferencesDataStore.autoSyncEnabled } returns flowOf(false)
        every { userPreferencesDataStore.autoSyncIntervalMs } returns flowOf(10_000L)
        every { context.getString(R.string.flight_no_url_toast) } returns toastMessage
    }

    private fun createViewModel(): FlightViewModel =
        FlightViewModel(getFlightsUseCase, userPreferencesDataStore, context)

    // -------------------------------------------------------------------------
    // 1. Initial state is Loading before coroutines run
    // -------------------------------------------------------------------------
    @Test
    fun `initial state is Loading before coroutines run`() = runTest {
        coEvery { getFlightsUseCase() } returns ApiResult.Success(emptyList())
        viewModel = createViewModel()
        // Before advancing time the state should still be Loading
        assertEquals(FlightUiState.Loading, viewModel.uiState.value)
    }

    // -------------------------------------------------------------------------
    // 2. loadFlights success transitions to Success state with correct flights
    // -------------------------------------------------------------------------
    @Test
    fun `loadFlights success transitions to Success state with correct flights`() = runTest {
        val flights = listOf(createFlight())
        coEvery { getFlightsUseCase() } returns ApiResult.Success(flights)

        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is FlightUiState.Success)
        val success = state as FlightUiState.Success
        assertEquals(flights, success.apiState.flights)
    }

    // -------------------------------------------------------------------------
    // 3. loadFlights error transitions to Error state
    // -------------------------------------------------------------------------
    @Test
    fun `loadFlights error transitions to Error state`() = runTest {
        coEvery { getFlightsUseCase() } returns ApiResult.Error("Network failure")

        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is FlightUiState.Error)
        assertEquals("Network failure", (state as FlightUiState.Error).message)
    }

    // -------------------------------------------------------------------------
    // 4. pullToRefresh success updates flights and preserves userState
    // -------------------------------------------------------------------------
    @Test
    fun `pullToRefresh success updates flights and preserves userState`() = runTest {
        val initialFlights = listOf(createFlight(airLineName = "Airline A"))
        val updatedFlights = listOf(createFlight(airLineName = "Airline B"))

        coEvery { getFlightsUseCase() } returnsMany listOf(
            ApiResult.Success(initialFlights),
            ApiResult.Success(updatedFlights),
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        val filter = FlightFilterOption.Region("Tokyo")
        viewModel.onFilterToggle(filter)

        viewModel.pullToRefresh()
        advanceUntilIdle()

        val state = viewModel.uiState.value as FlightUiState.Success
        // Updated flights should be shown (filter still applied but Tokyo matches)
        assertTrue(state.apiState.flights.isNotEmpty())
        // userState selectedFilters should be preserved
        assertTrue(state.filterState.selectedFilters.contains(filter))
    }

    // -------------------------------------------------------------------------
    // 5. pullToRefresh failure sets refreshError without leaving Success state
    //    and preserves userState
    // -------------------------------------------------------------------------
    @Test
    fun `pullToRefresh failure sets refreshError without leaving Success state and preserves userState`() = runTest {
        val initialFlights = listOf(createFlight())
        coEvery { getFlightsUseCase() } returnsMany listOf(
            ApiResult.Success(initialFlights),
            ApiResult.Error("Refresh failed"),
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        val filter = FlightFilterOption.Arrived
        viewModel.onFilterToggle(filter)

        viewModel.pullToRefresh()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is FlightUiState.Success)
        val success = state as FlightUiState.Success
        assertEquals("Refresh failed", success.apiState.refreshError)
        assertTrue(success.filterState.selectedFilters.contains(filter))
    }

    // -------------------------------------------------------------------------
    // 6. onFilterToggle adds filter to selectedFilters
    // -------------------------------------------------------------------------
    @Test
    fun `onFilterToggle adds filter to selectedFilters`() = runTest {
        val flights = listOf(createFlight(airFlyStatus = FlightStatus.ARRIVED))
        coEvery { getFlightsUseCase() } returns ApiResult.Success(flights)

        viewModel = createViewModel()
        advanceUntilIdle()

        val filter = FlightFilterOption.Arrived
        viewModel.onFilterToggle(filter)

        val state = viewModel.uiState.value as FlightUiState.Success
        assertTrue(state.filterState.selectedFilters.contains(filter))
    }

    // -------------------------------------------------------------------------
    // 7. onFilterToggle removes already selected filter
    // -------------------------------------------------------------------------
    @Test
    fun `onFilterToggle removes already selected filter`() = runTest {
        val flights = listOf(createFlight(airFlyStatus = FlightStatus.ARRIVED))
        coEvery { getFlightsUseCase() } returns ApiResult.Success(flights)

        viewModel = createViewModel()
        advanceUntilIdle()

        val filter = FlightFilterOption.Arrived
        viewModel.onFilterToggle(filter) // add
        viewModel.onFilterToggle(filter) // remove

        val state = viewModel.uiState.value as FlightUiState.Success
        assertTrue(state.filterState.selectedFilters.isEmpty())
    }

    // -------------------------------------------------------------------------
    // 8. onFilterToggle Arrived shows only ARRIVED flights
    // -------------------------------------------------------------------------
    @Test
    fun `onFilterToggle Arrived shows only ARRIVED flights`() = runTest {
        val arrivedFlight = createFlight(airFlyStatus = FlightStatus.ARRIVED)
        val unknownFlight = createFlight(airFlyStatus = FlightStatus.UNKNOWN)
        coEvery { getFlightsUseCase() } returns ApiResult.Success(listOf(arrivedFlight, unknownFlight))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onFilterToggle(FlightFilterOption.Arrived)

        val state = viewModel.uiState.value as FlightUiState.Success
        assertEquals(1, state.apiState.flights.size)
        assertEquals(FlightStatus.ARRIVED, state.apiState.flights[0].airFlyStatus)
    }

    // -------------------------------------------------------------------------
    // 9. onFilterToggle Region shows only matching airport flights
    // -------------------------------------------------------------------------
    @Test
    fun `onFilterToggle Region shows only matching airport flights`() = runTest {
        val tokyoFlight = createFlight(upAirportName = "Tokyo")
        val osakaFlight = createFlight(upAirportName = "Osaka")
        coEvery { getFlightsUseCase() } returns ApiResult.Success(listOf(tokyoFlight, osakaFlight))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onFilterToggle(FlightFilterOption.Region("Tokyo"))

        val state = viewModel.uiState.value as FlightUiState.Success
        assertEquals(1, state.apiState.flights.size)
        assertEquals("Tokyo", state.apiState.flights[0].upAirportName)
    }

    // -------------------------------------------------------------------------
    // 10. onFlightClick with URL emits OpenUrl event
    // -------------------------------------------------------------------------
    @Test
    fun `onFlightClick with URL emits OpenUrl event`() = runTest {
        coEvery { getFlightsUseCase() } returns ApiResult.Success(emptyList())

        viewModel = createViewModel()
        advanceUntilIdle()

        val events = mutableListOf<FlightEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        val flight = createFlight(airLineUrl = "https://example.com")
        viewModel.onFlightClick(flight)
        advanceUntilIdle()

        job.cancel()

        assertEquals(1, events.size)
        assertTrue(events[0] is FlightEvent.OpenUrl)
        assertEquals("https://example.com", (events[0] as FlightEvent.OpenUrl).url)
    }

    // -------------------------------------------------------------------------
    // 11. onFlightClick without URL emits ShowToast event
    // -------------------------------------------------------------------------
    @Test
    fun `onFlightClick without URL emits ShowToast event`() = runTest {
        coEvery { getFlightsUseCase() } returns ApiResult.Success(emptyList())

        viewModel = createViewModel()
        advanceUntilIdle()

        val events = mutableListOf<FlightEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        val flight = createFlight(airLineUrl = null)
        viewModel.onFlightClick(flight)
        advanceUntilIdle()

        job.cancel()

        assertEquals(1, events.size)
        assertTrue(events[0] is FlightEvent.ShowToast)
        assertEquals(toastMessage, (events[0] as FlightEvent.ShowToast).message)
    }

    // -------------------------------------------------------------------------
    // 12. loadFlights resets selectedFilters
    // -------------------------------------------------------------------------
    @Test
    fun `loadFlights resets selectedFilters`() = runTest {
        val flights = listOf(createFlight(airFlyStatus = FlightStatus.ARRIVED))
        coEvery { getFlightsUseCase() } returns ApiResult.Success(flights)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onFilterToggle(FlightFilterOption.Arrived)
        val stateWithFilter = viewModel.uiState.value as FlightUiState.Success
        assertTrue(stateWithFilter.filterState.selectedFilters.isNotEmpty())

        viewModel.loadFlights()
        advanceUntilIdle()

        val stateAfterReload = viewModel.uiState.value as FlightUiState.Success
        assertTrue(stateAfterReload.filterState.selectedFilters.isEmpty())
    }

    // -------------------------------------------------------------------------
    // 13. auto refresh updates flights after interval
    // -------------------------------------------------------------------------
    @Test
    fun `auto refresh updates flights after interval`() = runTest {
        every { userPreferencesDataStore.autoSyncEnabled } returns flowOf(true)
        every { userPreferencesDataStore.autoSyncIntervalMs } returns flowOf(10_000L)

        val initialFlights = listOf(createFlight(airLineName = "Airline A"))
        val refreshedFlights = listOf(createFlight(airLineName = "Airline B"))

        coEvery { getFlightsUseCase() } returnsMany listOf(
            ApiResult.Success(initialFlights),
            ApiResult.Success(refreshedFlights),
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        val stateAfterLoad = viewModel.uiState.value as FlightUiState.Success
        assertEquals("Airline A", stateAfterLoad.apiState.flights[0].airLineName)

        advanceTimeBy(10_001L)
        advanceUntilIdle()

        val stateAfterRefresh = viewModel.uiState.value as FlightUiState.Success
        assertEquals("Airline B", stateAfterRefresh.apiState.flights[0].airLineName)
    }

    // -------------------------------------------------------------------------
    // 14. auto refresh failure shows refreshError without entering Error state
    // -------------------------------------------------------------------------
    @Test
    fun `auto refresh failure shows refreshError without entering Error state`() = runTest {
        every { userPreferencesDataStore.autoSyncEnabled } returns flowOf(true)
        every { userPreferencesDataStore.autoSyncIntervalMs } returns flowOf(10_000L)

        val initialFlights = listOf(createFlight())
        coEvery { getFlightsUseCase() } returnsMany listOf(
            ApiResult.Success(initialFlights),
            ApiResult.Error("Auto refresh failed"),
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        advanceTimeBy(10_001L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is FlightUiState.Success)
        val success = state as FlightUiState.Success
        assertNotNull(success.apiState.refreshError)
        assertEquals("Auto refresh failed", success.apiState.refreshError)
    }
}
