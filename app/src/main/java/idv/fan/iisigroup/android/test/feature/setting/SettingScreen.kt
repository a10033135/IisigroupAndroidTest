package idv.fan.iisigroup.android.test.feature.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import idv.fan.iisigroup.android.test.R
import idv.fan.iisigroup.android.test.domain.model.Currency
import idv.fan.iisigroup.android.test.domain.model.SyncInterval
import idv.fan.iisigroup.android.test.ui.state.SettingUiState

@Composable
fun SettingScreen(
    uiState: SettingUiState,
    onDarkThemeChange: (Boolean) -> Unit,
    onAutoSyncChange: (Boolean) -> Unit,
    onAutoSyncIntervalChange: (SyncInterval) -> Unit,
    onDefaultCurrencyChange: (Currency) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier,
) {
    var showCurrencyPicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
    ) {
        SettingVersionItem(appVersion = uiState.appVersion)
        HorizontalDivider()
        SettingDarkModeItem(
            isDarkTheme = uiState.isDarkTheme,
            onCheckedChange = onDarkThemeChange,
        )
        HorizontalDivider()
        SettingAutoSyncItem(
            autoSyncEnabled = uiState.autoSyncEnabled,
            onCheckedChange = onAutoSyncChange,
        )
        if (uiState.autoSyncEnabled) {
            SettingAutoSyncIntervalItem(
                selected = uiState.autoSyncInterval,
                onSelected = onAutoSyncIntervalChange,
            )
        }
        HorizontalDivider()
        SettingDefaultCurrencyItem(
            currency = uiState.defaultCurrency,
            onClick = { showCurrencyPicker = true },
        )
    }

    if (showCurrencyPicker) {
        SettingCurrencyPickerDialog(
            current = uiState.defaultCurrency,
            onDismiss = { showCurrencyPicker = false },
            onSelected = { currency ->
                onDefaultCurrencyChange(currency)
                showCurrencyPicker = false
            },
        )
    }
}

@Composable
private fun SettingVersionItem(appVersion: String) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.setting_version)) },
        trailingContent = {
            Text(
                text = appVersion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
}

@Composable
private fun SettingDarkModeItem(
    isDarkTheme: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.setting_dark_mode)) },
        trailingContent = {
            Switch(
                checked = isDarkTheme,
                onCheckedChange = onCheckedChange,
            )
        },
    )
}

@Composable
private fun SettingAutoSyncItem(
    autoSyncEnabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.setting_auto_sync)) },
        supportingContent = { Text(stringResource(R.string.setting_auto_sync_desc)) },
        trailingContent = {
            Switch(
                checked = autoSyncEnabled,
                onCheckedChange = onCheckedChange,
            )
        },
    )
}

@Composable
private fun SettingAutoSyncIntervalItem(
    selected: SyncInterval,
    onSelected: (SyncInterval) -> Unit,
) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.setting_sync_interval)) },
        supportingContent = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SyncInterval.entries.forEach { interval ->
                    FilterChip(
                        selected = interval == selected,
                        onClick = { onSelected(interval) },
                        label = { Text(interval.toLabel()) },
                    )
                }
            }
        },
    )
}

@Composable
private fun SyncInterval.toLabel(): String = when (this) {
    SyncInterval.TEN_SECONDS -> stringResource(R.string.sync_interval_10s)
    SyncInterval.ONE_MINUTE -> stringResource(R.string.sync_interval_1m)
    SyncInterval.FIVE_MINUTES -> stringResource(R.string.sync_interval_5m)
}

@Composable
private fun SettingDefaultCurrencyItem(
    currency: Currency,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.setting_default_currency)) },
        supportingContent = { Text(stringResource(R.string.setting_default_currency_desc)) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = currency.code,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.setting_currency_select_desc),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        },
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
private fun SettingCurrencyPickerDialog(
    current: Currency,
    onDismiss: () -> Unit,
    onSelected: (Currency) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.setting_currency_picker_title)) },
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
                                fontWeight = if (currency == current) FontWeight.Bold else FontWeight.Normal,
                                color = if (currency == current) {
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
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        },
    )
}
