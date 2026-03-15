package idv.fan.iisigroup.android.test.feature.flight

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightRoute(
    modifier: Modifier = Modifier,
    viewModel: FlightViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is FlightEvent.OpenUrl -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.url))
                    context.startActivity(intent)
                }
                is FlightEvent.ShowToast ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("航班資訊") }) },
        modifier = modifier,
    ) { innerPadding ->
        FlightScreen(
            uiState = uiState,
            onRetry = viewModel::loadFlights,
                        onFilterToggle = viewModel::onFilterToggle,
            onFlightClick = viewModel::onFlightClick,
            onPullToRefresh = viewModel::pullToRefresh,
            contentPadding = innerPadding,
        )
    }
}
