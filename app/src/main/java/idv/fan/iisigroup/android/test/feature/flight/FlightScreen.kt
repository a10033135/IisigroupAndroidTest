package idv.fan.iisigroup.android.test.feature.flight

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import idv.fan.iisigroup.android.test.R
import idv.fan.iisigroup.android.test.domain.model.Flight
import idv.fan.iisigroup.android.test.domain.model.FlightStatus
import idv.fan.iisigroup.android.test.ui.components.FlightShimmerContent
import idv.fan.iisigroup.android.test.ui.state.FlightUiState
import idv.fan.iisigroup.android.test.ui.theme.FlightStatusArrived
import idv.fan.iisigroup.android.test.ui.theme.FlightStatusCancelled
import idv.fan.iisigroup.android.test.ui.theme.FlightStatusDefault
import idv.fan.iisigroup.android.test.ui.theme.FlightStatusDelayed
import idv.fan.iisigroup.android.test.ui.theme.FlightStatusDeparted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightScreen(
    uiState: FlightUiState,
    onRetry: () -> Unit,
    onFilterToggle: (FlightFilterOption) -> Unit,
    onFlightClick: (Flight) -> Unit,
    onPullToRefresh: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(contentPadding)) {
        FlightUpdateInfoSection(uiState = uiState)

        if (uiState is FlightUiState.Success) {
            FlightFilterSection(
                availableFilters = uiState.availableFilters,
                selectedFilters = uiState.selectedFilters,
                onFilterToggle = onFilterToggle,
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            when (uiState) {
                is FlightUiState.Loading -> FlightShimmerContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
                is FlightUiState.Success -> FlightSuccessContent(
                    flights = uiState.flights,
                    isRefreshing = uiState.isRefreshing,
                    onPullToRefresh = onPullToRefresh,
                    onFlightClick = onFlightClick,
                    modifier = Modifier.fillMaxSize(),
                )
                is FlightUiState.Error -> FlightErrorContent(
                    message = uiState.message,
                    onRetry = onRetry,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun FlightUpdateInfoSection(uiState: FlightUiState) {
    when (uiState) {
        is FlightUiState.Loading -> FlightUpdateInfoText(stringResource(R.string.common_no_data))
        is FlightUiState.Error -> FlightUpdateInfoError(uiState.message)
        is FlightUiState.Success -> when {
            uiState.isRefreshing -> FlightRefreshingBanner()
            uiState.refreshError != null -> FlightUpdateInfoError(uiState.refreshError)
            else -> FlightUpdateInfoText(stringResource(R.string.common_last_update, uiState.lastRefreshTime))
        }
    }
}

@Composable
private fun FlightUpdateInfoText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun FlightRefreshingBanner() {
    Column(modifier = Modifier.fillMaxWidth()) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        Text(
            text = stringResource(R.string.common_refreshing),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun FlightUpdateInfoError(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun FlightFilterSection(
    availableFilters: List<FlightFilterOption>,
    selectedFilters: Set<FlightFilterOption>,
    onFilterToggle: (FlightFilterOption) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        availableFilters.forEach { filter ->
            FilterChip(
                selected = filter in selectedFilters,
                onClick = { onFilterToggle(filter) },
                label = { Text(filter.toLabel()) },
            )
        }
    }
}

@Composable
private fun FlightFilterOption.toLabel(): String = when (this) {
    is FlightFilterOption.Arrived -> stringResource(R.string.flight_filter_arrived)
    is FlightFilterOption.Region -> airportName
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FlightSuccessContent(
    flights: List<Flight>,
    isRefreshing: Boolean,
    onPullToRefresh: () -> Unit,
    onFlightClick: (Flight) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val columns = if (isLandscape) GridCells.Fixed(2) else GridCells.Fixed(1)

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onPullToRefresh,
        modifier = modifier,
    ) {
        if (flights.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.flight_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyVerticalGrid(
                columns = columns,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(flights) { flight ->
                    FlightListItem(
                        flight = flight,
                        onClick = { onFlightClick(flight) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FlightErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) { Text(stringResource(R.string.common_retry)) }
    }
}

@Composable
private fun FlightListItem(
    flight: Flight,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = flight.airLineLogo,
                contentDescription = flight.airLineName,
                modifier = Modifier.size(40.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${flight.airLineNum.orEmpty()} - ${flight.airLineName.orEmpty()}",
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = "${flight.upAirportName.orEmpty()} (${flight.upAirportCode.orEmpty()})",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row {
                    flight.expectTime?.let {
                        Text(
                            text = stringResource(R.string.flight_expect_time, it),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    flight.realTime?.let {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.flight_actual_time, it),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                flight.airBoardingGate?.takeIf { it.isNotEmpty() }?.let {
                    Text(
                        text = stringResource(R.string.flight_boarding_gate, it),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            FlightStatusChip(status = flight.airFlyStatus, modifier = Modifier.align(Alignment.Top))
        }
    }
}

@Composable
private fun FlightStatusChip(status: FlightStatus, modifier: Modifier = Modifier) {
    if (status == FlightStatus.UNKNOWN) return
    val containerColor = when (status) {
        FlightStatus.ARRIVED -> FlightStatusArrived
        FlightStatus.DELAYED -> FlightStatusDelayed
        FlightStatus.CANCELLED -> FlightStatusCancelled
        FlightStatus.DEPARTED -> FlightStatusDeparted
        FlightStatus.UNKNOWN -> FlightStatusDefault
    }
    val label = when (status) {
        FlightStatus.ARRIVED -> stringResource(R.string.flight_status_arrived)
        FlightStatus.DELAYED -> stringResource(R.string.flight_status_delayed)
        FlightStatus.CANCELLED -> stringResource(R.string.flight_status_cancelled)
        FlightStatus.DEPARTED -> stringResource(R.string.flight_status_departed)
        FlightStatus.UNKNOWN -> ""
    }
    Surface(
        modifier = modifier,
        color = containerColor,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
