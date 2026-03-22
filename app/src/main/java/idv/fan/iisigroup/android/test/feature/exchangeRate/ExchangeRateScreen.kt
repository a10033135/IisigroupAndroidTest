package idv.fan.iisigroup.android.test.feature.exchangeRate

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import idv.fan.iisigroup.android.test.R
import idv.fan.iisigroup.android.test.core.formatNumber
import idv.fan.iisigroup.android.test.domain.model.Currency
import idv.fan.iisigroup.android.test.domain.model.ExchangeRate
import idv.fan.iisigroup.android.test.ui.components.CurrencyPickerDialog
import idv.fan.iisigroup.android.test.ui.components.ExchangeRateShimmerContent
import idv.fan.iisigroup.android.test.ui.state.ExchangeRateUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeRateScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    uiState: ExchangeRateUiState,
    onRetry: () -> Unit,
    onBaseCurrencyClick: () -> Unit,
    onCurrencyPickerDismiss: () -> Unit,
    onBaseCurrencySelected: (Currency) -> Unit,
    onPullToRefresh: () -> Unit,
    onCalculatorClick: () -> Unit,
    onCalculatorDismiss: () -> Unit,
    onCalculatorConfirm: (Double) -> Unit,
) {
    Column(modifier = modifier.padding(contentPadding)) {
        ExchangeRateUpdateInfoSection(
            uiState = uiState,
            onBaseCurrencyClick = onBaseCurrencyClick,
        )

        Box(modifier = Modifier.weight(1f)) {
            when (uiState) {
                is ExchangeRateUiState.Loading -> ExchangeRateShimmerContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )

                is ExchangeRateUiState.Success -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExchangeRateCalculatorBanner(
                            amount = uiState.calculatorState.amount,
                            baseCurrencyCode = uiState.apiState.baseCurrency.code,
                            onClick = onCalculatorClick,
                        )
                        ExchangeRateSuccessContent(
                            uiState = uiState,
                            calculatorAmount = uiState.calculatorState.amount,
                            onPullToRefresh = onPullToRefresh,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }

                is ExchangeRateUiState.Error -> ExchangeRateErrorContent(
                    message = uiState.message,
                    onRetry = onRetry,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    if (uiState is ExchangeRateUiState.Success && uiState.showCurrencyPicker) {
        CurrencyPickerDialog(
            title = stringResource(R.string.exchange_rate_picker_title),
            currentCurrency = uiState.apiState.baseCurrency,
            onDismiss = onCurrencyPickerDismiss,
            onSelected = onBaseCurrencySelected,
        )
    }

    if (uiState is ExchangeRateUiState.Success && uiState.calculatorState.showCalculator) {
        CalculatorBottomSheet(
            onDismiss = onCalculatorDismiss,
            onConfirm = onCalculatorConfirm,
        )
    }

}

@Composable
private fun ExchangeRateUpdateInfoSection(
    uiState: ExchangeRateUiState,
    onBaseCurrencyClick: () -> Unit,
) {
    when (uiState) {
        is ExchangeRateUiState.Loading -> ExchangeRateInfoText(stringResource(R.string.common_no_data))
        is ExchangeRateUiState.Error -> ExchangeRateInfoError(uiState.message)
        is ExchangeRateUiState.Success -> when {
            uiState.apiState.isRefreshing -> ExchangeRateRefreshingBanner()
            uiState.apiState.refreshError != null -> ExchangeRateInfoError(uiState.apiState.refreshError)
            else -> ExchangeRateSuccessInfo(
                baseCurrency = uiState.apiState.baseCurrency,
                lastRefreshTime = uiState.apiState.lastRefreshTime,
                onClick = onBaseCurrencyClick,
            )
        }
    }
}

@Composable
private fun ExchangeRateInfoText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun ExchangeRateRefreshingBanner() {
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
private fun ExchangeRateInfoError(message: String) {
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
private fun ExchangeRateCalculatorBanner(
    amount: Double? = null,
    baseCurrencyCode: String,
    onClick: () -> Unit,
) {
    val isCalculatorActive = amount != null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = if (isCalculatorActive) {
                "${formatNumber(amount)} $baseCurrencyCode"
            } else {
                stringResource(R.string.exchange_rate_calculator_welcome)
            },
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primaryContainer
        )
        Icon(
            imageVector = Icons.Default.Create,
            contentDescription = stringResource(R.string.exchange_rate_calculator),
            tint = MaterialTheme.colorScheme.primaryContainer,
        )
    }
}

@Composable
private fun ExchangeRateSuccessInfo(
    baseCurrency: Currency,
    lastRefreshTime: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.common_last_update, lastRefreshTime),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.exchange_rate_base, baseCurrency.code),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(R.string.exchange_rate_switch_currency),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExchangeRateSuccessContent(
    uiState: ExchangeRateUiState.Success,
    calculatorAmount: Double?,
    onPullToRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val columns = if (isLandscape) GridCells.Fixed(2) else GridCells.Fixed(1)

    PullToRefreshBox(
        isRefreshing = uiState.apiState.isRefreshing,
        onRefresh = onPullToRefresh,
        modifier = modifier,
    ) {
        if (uiState.apiState.rates.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.exchange_rate_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyVerticalGrid(
                columns = columns,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(uiState.apiState.rates) { rate ->
                    ExchangeRateItem(
                        rate = rate,
                        baseCurrency = uiState.apiState.baseCurrency,
                        calculatorAmount = calculatorAmount,
                    )
                }
            }
        }
    }
}

@Composable
private fun ExchangeRateErrorContent(
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
private fun ExchangeRateItem(
    rate: ExchangeRate,
    baseCurrency: Currency,
    calculatorAmount: Double?,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = rate.currency.code,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Column(horizontalAlignment = Alignment.End) {

                calculatorAmount?.let {
                    val finalAmount = "%.4f".format(it * rate.rate)
                    Text(
                        text = "$finalAmount ${rate.currency.code}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                Text(
                    text = "1 ${baseCurrency.code} 等於 ${formatNumber(rate.rate)} ${rate.currency.code}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

