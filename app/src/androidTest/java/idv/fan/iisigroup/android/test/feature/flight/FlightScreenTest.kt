package idv.fan.iisigroup.android.test.feature.flight

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import idv.fan.iisigroup.android.test.domain.model.Flight
import idv.fan.iisigroup.android.test.domain.model.FlightStatus
import idv.fan.iisigroup.android.test.ui.state.FlightApiState
import idv.fan.iisigroup.android.test.ui.state.FlightUiState
import idv.fan.iisigroup.android.test.ui.state.FlightUserState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FlightScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun makeFlight(
        airLineNum: String = "B78690",
        upAirportName: String = "澎湖",
    ) = Flight(
        expectTime = "10:00",
        realTime = "10:15",
        airLineName = "測試航空",
        airLineCode = "B7",
        airLineLogo = null,
        airLineUrl = null,
        airLineNum = airLineNum,
        upAirportCode = "MZG",
        upAirportName = upAirportName,
        airPlaneType = "A320",
        airBoardingGate = "A1",
        airFlyStatus = FlightStatus.UNKNOWN,
        airFlyDelayCause = null,
    )

    private fun makeSuccessState(
        flights: List<Flight> = emptyList(),
        availableFilters: List<FlightFilterOption> = emptyList(),
        isRefreshing: Boolean = false,
        refreshError: String? = null,
        selectedFilters: Set<FlightFilterOption> = emptySet(),
    ) = FlightUiState.Success(
        apiState = FlightApiState(
            flights = flights,
            availableFilters = availableFilters,
            lastRefreshTime = "2024-01-01 00:00",
            isRefreshing = isRefreshing,
            refreshError = refreshError,
        ),
        userState = FlightUserState(selectedFilters = selectedFilters),
    )

    @Test
    fun loadingState_doesNotShowSuccessOrErrorContent() {
        composeTestRule.setContent {
            MaterialTheme {
                FlightScreen(
                    uiState = FlightUiState.Loading,
                    onRetry = {},
                    onFilterToggle = {},
                    onFlightClick = {},
                    onPullToRefresh = {},
                    contentPadding = PaddingValues(),
                )
            }
        }

        composeTestRule.onNodeWithText("目前無任何航班資訊").assertDoesNotExist()
        composeTestRule.onNodeWithText("重新嘗試").assertDoesNotExist()
    }

    @Test
    fun errorState_showsErrorMessageAndRetryButton() {
        composeTestRule.setContent {
            MaterialTheme {
                FlightScreen(
                    uiState = FlightUiState.Error("네트워크 오류"),
                    onRetry = {},
                    onFilterToggle = {},
                    onFlightClick = {},
                    onPullToRefresh = {},
                    contentPadding = PaddingValues(),
                )
            }
        }

        composeTestRule.onNodeWithText("네트워크 오류").assertIsDisplayed()
        composeTestRule.onNodeWithText("重新嘗試").assertIsDisplayed()
    }

    @Test
    fun successWithEmptyFlights_showsEmptyMessage() {
        composeTestRule.setContent {
            MaterialTheme {
                FlightScreen(
                    uiState = makeSuccessState(flights = emptyList()),
                    onRetry = {},
                    onFilterToggle = {},
                    onFlightClick = {},
                    onPullToRefresh = {},
                    contentPadding = PaddingValues(),
                )
            }
        }

        composeTestRule.onNodeWithText("目前無任何航班資訊").assertIsDisplayed()
    }

    @Test
    fun successWithFlights_showsFlightItems() {
        val flight = makeFlight(airLineNum = "B78690", upAirportName = "澎湖")
        composeTestRule.setContent {
            MaterialTheme {
                FlightScreen(
                    uiState = makeSuccessState(flights = listOf(flight)),
                    onRetry = {},
                    onFilterToggle = {},
                    onFlightClick = {},
                    onPullToRefresh = {},
                    contentPadding = PaddingValues(),
                )
            }
        }

        composeTestRule.onNodeWithText("B78690", substring = true).assertIsDisplayed()
    }

    @Test
    fun successWithIsRefreshing_showsRefreshingBanner() {
        composeTestRule.setContent {
            MaterialTheme {
                FlightScreen(
                    uiState = makeSuccessState(isRefreshing = true),
                    onRetry = {},
                    onFilterToggle = {},
                    onFlightClick = {},
                    onPullToRefresh = {},
                    contentPadding = PaddingValues(),
                )
            }
        }

        composeTestRule.onNodeWithText("刷新中").assertIsDisplayed()
    }

    @Test
    fun successWithRefreshError_showsErrorBannerNotRetryButton() {
        composeTestRule.setContent {
            MaterialTheme {
                FlightScreen(
                    uiState = makeSuccessState(refreshError = "刷新失敗"),
                    onRetry = {},
                    onFilterToggle = {},
                    onFlightClick = {},
                    onPullToRefresh = {},
                    contentPadding = PaddingValues(),
                )
            }
        }

        composeTestRule.onNodeWithText("刷新失敗").assertIsDisplayed()
        composeTestRule.onNodeWithText("重新嘗試").assertDoesNotExist()
    }

    @Test
    fun filterChip_isVisibleWhenFiltersAvailable() {
        composeTestRule.setContent {
            MaterialTheme {
                FlightScreen(
                    uiState = makeSuccessState(availableFilters = listOf(FlightFilterOption.Arrived)),
                    onRetry = {},
                    onFilterToggle = {},
                    onFlightClick = {},
                    onPullToRefresh = {},
                    contentPadding = PaddingValues(),
                )
            }
        }

        composeTestRule.onNodeWithText("抵達").assertIsDisplayed()
    }

    @Test
    fun retryButtonClick_triggersOnRetry() {
        var retryClicked = false
        composeTestRule.setContent {
            MaterialTheme {
                FlightScreen(
                    uiState = FlightUiState.Error("錯誤訊息"),
                    onRetry = { retryClicked = true },
                    onFilterToggle = {},
                    onFlightClick = {},
                    onPullToRefresh = {},
                    contentPadding = PaddingValues(),
                )
            }
        }

        composeTestRule.onNodeWithText("重新嘗試").performClick()
        assertTrue(retryClicked)
    }

    @Test
    fun flightCardClick_triggersOnFlightClick() {
        val flight = makeFlight(airLineNum = "B78690", upAirportName = "澎湖")
        var clickedFlight: Flight? = null
        composeTestRule.setContent {
            MaterialTheme {
                FlightScreen(
                    uiState = makeSuccessState(flights = listOf(flight)),
                    onRetry = {},
                    onFilterToggle = {},
                    onFlightClick = { clickedFlight = it },
                    onPullToRefresh = {},
                    contentPadding = PaddingValues(),
                )
            }
        }

        composeTestRule.onNodeWithText("B78690", substring = true).performClick()
        assertTrue(clickedFlight != null)
    }
}
