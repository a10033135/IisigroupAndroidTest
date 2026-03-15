package idv.fan.iisigroup.android.test.feature.flight

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import idv.fan.iisigroup.android.test.domain.model.Flight
import idv.fan.iisigroup.android.test.ui.state.FlightUiState
import idv.fan.iisigroup.android.test.ui.theme.FlightStatusArrived
import idv.fan.iisigroup.android.test.ui.theme.FlightStatusCancelled
import idv.fan.iisigroup.android.test.ui.theme.FlightStatusDefault
import idv.fan.iisigroup.android.test.ui.theme.FlightStatusDelayed
import idv.fan.iisigroup.android.test.ui.theme.FlightStatusDeparted

@Composable
fun FlightScreen(
    uiState: FlightUiState,
    onRetry: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is FlightUiState.Loading -> FlightLoadingContent(
            modifier = modifier.fillMaxSize().padding(contentPadding),
        )
        is FlightUiState.Success -> FlightSuccessContent(
            uiState = uiState,
            contentPadding = contentPadding,
            modifier = modifier.fillMaxSize(),
        )
        is FlightUiState.Error -> FlightErrorContent(
            message = uiState.message,
            onRetry = onRetry,
            modifier = modifier.fillMaxSize().padding(contentPadding),
        )
    }
}

@Composable
private fun FlightLoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun FlightSuccessContent(
    uiState: FlightUiState.Success,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (uiState.isRefreshing) {
            FlightRefreshingBanner()
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(uiState.flights) { flight ->
                FlightListItem(flight = flight)
            }
        }
    }
}

@Composable
private fun FlightRefreshingBanner(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        Text(
            text = "刷新中",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
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
        Button(onClick = onRetry) {
            Text("重新嘗試")
        }
    }
}

@Composable
private fun FlightListItem(flight: Flight, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
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
                        Text(text = "預計: $it", style = MaterialTheme.typography.bodySmall)
                    }
                    flight.realTime?.let {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "實際: $it", style = MaterialTheme.typography.bodySmall)
                    }
                }
                flight.airBoardingGate?.takeIf { it.isNotEmpty() }?.let {
                    Text(text = "登機門: $it", style = MaterialTheme.typography.bodySmall)
                }
            }
            FlightStatusChip(status = flight.airFlyStatus)
        }
    }
}

@Composable
private fun FlightStatusChip(status: String?, modifier: Modifier = Modifier) {
    if (status.isNullOrEmpty()) return
    val containerColor = when (status) {
        "抵達" -> FlightStatusArrived
        "延誤" -> FlightStatusDelayed
        "取消" -> FlightStatusCancelled
        "起飛" -> FlightStatusDeparted
        else -> FlightStatusDefault
    }
    Surface(
        modifier = modifier,
        color = containerColor,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
