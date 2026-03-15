package idv.fan.iisigroup.android.test.feature.exchangeRate

import android.content.res.Configuration
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import idv.fan.iisigroup.android.test.domain.model.Currency
import idv.fan.iisigroup.android.test.domain.model.ExchangeRate
import idv.fan.iisigroup.android.test.ui.components.ExchangeRateShimmerContent
import idv.fan.iisigroup.android.test.ui.state.ExchangeRateUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeRateScreen(
    uiState: ExchangeRateUiState,
    onRetry: () -> Unit,
    onBaseCurrencyClick: () -> Unit,
    onCurrencyPickerDismiss: () -> Unit,
    onBaseCurrencySelected: (Currency) -> Unit,
    onPullToRefresh: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier,
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
                is ExchangeRateUiState.Success -> ExchangeRateSuccessContent(
                    uiState = uiState,
                    onPullToRefresh = onPullToRefresh,
                    modifier = Modifier.fillMaxSize(),
                )
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
            currentCurrency = uiState.baseCurrency,
            onDismiss = onCurrencyPickerDismiss,
            onSelected = onBaseCurrencySelected,
        )
    }
}

// 更新資訊區塊
@Composable
private fun ExchangeRateUpdateInfoSection(
    uiState: ExchangeRateUiState,
    onBaseCurrencyClick: () -> Unit,
) {
    when (uiState) {
        is ExchangeRateUiState.Loading -> ExchangeRateInfoText("尚未取得資料")
        is ExchangeRateUiState.Error -> ExchangeRateInfoError(uiState.message)
        is ExchangeRateUiState.Success -> when {
            uiState.isRefreshing -> ExchangeRateRefreshingBanner()
            uiState.refreshError != null -> ExchangeRateInfoError(uiState.refreshError)
            else -> ExchangeRateSuccessInfo(
                baseCurrency = uiState.baseCurrency,
                lastRefreshTime = uiState.lastRefreshTime,
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
            text = "刷新中",
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
            text = "最後更新：$lastRefreshTime",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "基準：${baseCurrency.code}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "切換貨幣",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

// 內容區塊（Success）
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExchangeRateSuccessContent(
    uiState: ExchangeRateUiState.Success,
    onPullToRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val columns = if (isLandscape) GridCells.Fixed(2) else GridCells.Fixed(1)

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = onPullToRefresh,
        modifier = modifier,
    ) {
        if (uiState.rates.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "目前無任何匯率資訊",
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
                items(uiState.rates) { rate ->
                    ExchangeRateItem(
                        rate = rate,
                        baseCurrency = uiState.baseCurrency,
                    )
                }
            }
        }
    }
}

// 內容區塊（Error）
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
        Button(onClick = onRetry) { Text("重新嘗試") }
    }
}

@Composable
private fun ExchangeRateItem(
    rate: ExchangeRate,
    baseCurrency: Currency,
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
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "%.4f".format(rate.rate),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "/ 1 ${baseCurrency.code}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CurrencyPickerDialog(
    currentCurrency: Currency,
    onDismiss: () -> Unit,
    onSelected: (Currency) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("選擇基準貨幣") },
        text = {
            Column {
                Currency.entries.forEach { currency ->
                    TextButton(
                        onClick = { onSelected(currency) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = currency.code,
                                fontWeight = if (currency == currentCurrency) FontWeight.Bold else FontWeight.Normal,
                                color = if (currency == currentCurrency) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}
