package idv.fan.iisigroup.android.test.feature.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import idv.fan.iisigroup.android.test.domain.model.SyncInterval
import idv.fan.iisigroup.android.test.ui.state.SettingUiState

@Composable
fun SettingScreen(
    uiState: SettingUiState,
    onDarkThemeChange: (Boolean) -> Unit,
    onAutoSyncChange: (Boolean) -> Unit,
    onAutoSyncIntervalChange: (SyncInterval) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier,
) {
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
    }
}

@Composable
private fun SettingVersionItem(appVersion: String) {
    ListItem(
        headlineContent = { Text("版本號碼") },
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
        headlineContent = { Text("暗黑模式") },
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
        headlineContent = { Text("自動同步") },
        supportingContent = { Text("自動同步匯率及航班資料") },
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
        headlineContent = { Text("同步間隔") },
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
                        label = { Text(interval.label) },
                    )
                }
            }
        },
    )
}
