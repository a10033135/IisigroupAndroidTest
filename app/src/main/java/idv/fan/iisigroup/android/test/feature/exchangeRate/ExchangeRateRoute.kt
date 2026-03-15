package idv.fan.iisigroup.android.test.feature.exchangeRate

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.res.stringResource
import idv.fan.iisigroup.android.test.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeRateRoute(
    modifier: Modifier = Modifier,
    viewModel: ExchangeRateViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.title_exchange_rate)) }) },
        modifier = modifier,
    ) { innerPadding ->
        ExchangeRateScreen(
            uiState = uiState,
            onRetry = viewModel::loadRates,
            onBaseCurrencyClick = viewModel::onBaseCurrencyClick,
            onCurrencyPickerDismiss = viewModel::onCurrencyPickerDismiss,
            onBaseCurrencySelected = viewModel::onBaseCurrencySelected,
            onPullToRefresh = viewModel::pullToRefresh,
            contentPadding = innerPadding,
        )
    }
}
