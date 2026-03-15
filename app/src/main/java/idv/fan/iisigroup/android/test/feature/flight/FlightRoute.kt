package idv.fan.iisigroup.android.test.feature.flight

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun FlightRoute(
    modifier: Modifier = Modifier,
    viewModel: FlightViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    FlightScreen(
        uiState = uiState,
        onRetry = viewModel::loadFlights,
        modifier = modifier,
    )
}
