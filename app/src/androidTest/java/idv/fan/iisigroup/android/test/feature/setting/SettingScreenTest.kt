package idv.fan.iisigroup.android.test.feature.setting

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasRole
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import idv.fan.iisigroup.android.test.domain.model.Currency
import idv.fan.iisigroup.android.test.domain.model.SyncInterval
import idv.fan.iisigroup.android.test.ui.state.SettingUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun makeUiState(
        appVersion: String = "1.0",
        isDarkTheme: Boolean = false,
        autoSyncEnabled: Boolean = true,
        autoSyncInterval: SyncInterval = SyncInterval.TEN_SECONDS,
        defaultCurrency: Currency = Currency.USD,
    ) = SettingUiState(
        appVersion = appVersion,
        isDarkTheme = isDarkTheme,
        autoSyncEnabled = autoSyncEnabled,
        autoSyncInterval = autoSyncInterval,
        defaultCurrency = defaultCurrency,
    )

    @Test
    fun versionNumber_isDisplayed() {
        composeTestRule.setContent {
            MaterialTheme {
                SettingScreen(
                    uiState = makeUiState(appVersion = "1.0"),
                    onDarkThemeChange = {},
                    onAutoSyncChange = {},
                    onAutoSyncIntervalChange = {},
                    onDefaultCurrencyChange = {},
                    contentPadding = PaddingValues(),
                )
            }
        }

        composeTestRule.onNodeWithText("版本號碼").assertIsDisplayed()
        composeTestRule.onNodeWithText("1.0").assertIsDisplayed()
    }

    @Test
    fun autoSyncEnabledTrue_showsSyncInterval() {
        composeTestRule.setContent {
            MaterialTheme {
                SettingScreen(
                    uiState = makeUiState(autoSyncEnabled = true),
                    onDarkThemeChange = {},
                    onAutoSyncChange = {},
                    onAutoSyncIntervalChange = {},
                    onDefaultCurrencyChange = {},
                    contentPadding = PaddingValues(),
                )
            }
        }

        composeTestRule.onNodeWithText("同步間隔").assertIsDisplayed()
    }

    @Test
    fun autoSyncEnabledFalse_hidesSyncInterval() {
        composeTestRule.setContent {
            MaterialTheme {
                SettingScreen(
                    uiState = makeUiState(autoSyncEnabled = false),
                    onDarkThemeChange = {},
                    onAutoSyncChange = {},
                    onAutoSyncIntervalChange = {},
                    onDefaultCurrencyChange = {},
                    contentPadding = PaddingValues(),
                )
            }
        }

        composeTestRule.onNodeWithText("同步間隔").assertDoesNotExist()
    }

    @Test
    fun darkModeSwitchClick_triggersCallback() {
        var darkThemeChanged = false
        composeTestRule.setContent {
            MaterialTheme {
                SettingScreen(
                    uiState = makeUiState(isDarkTheme = false),
                    onDarkThemeChange = { darkThemeChanged = true },
                    onAutoSyncChange = {},
                    onAutoSyncIntervalChange = {},
                    onDefaultCurrencyChange = {},
                    contentPadding = PaddingValues(),
                )
            }
        }

        // The Switch is the trailing content of the "暗黑模式" ListItem
        // We use the Role.Switch semantic to find and click it
        composeTestRule
            .onNodeWithText("暗黑模式")
            .assertIsDisplayed()

        // The dark mode switch is the first Switch role node in the screen
        composeTestRule
            .onAllNodes(hasRole(Role.Switch))[0]
            .performClick()
        assertTrue(darkThemeChanged)
    }

    @Test
    fun currencyItemClick_showsPickerDialog() {
        composeTestRule.setContent {
            MaterialTheme {
                SettingScreen(
                    uiState = makeUiState(),
                    onDarkThemeChange = {},
                    onAutoSyncChange = {},
                    onAutoSyncIntervalChange = {},
                    onDefaultCurrencyChange = {},
                    contentPadding = PaddingValues(),
                )
            }
        }

        composeTestRule.onNodeWithText("預設貨幣").performClick()
        composeTestRule.onNodeWithText("選擇預設貨幣").assertIsDisplayed()
    }

    @Test
    fun selectingCurrencyInPicker_callsCallbackAndCloseDialog() {
        var selectedCurrency: Currency? = null
        composeTestRule.setContent {
            MaterialTheme {
                SettingScreen(
                    uiState = makeUiState(defaultCurrency = Currency.USD),
                    onDarkThemeChange = {},
                    onAutoSyncChange = {},
                    onAutoSyncIntervalChange = {},
                    onDefaultCurrencyChange = { selectedCurrency = it },
                    contentPadding = PaddingValues(),
                )
            }
        }

        // Open the currency picker
        composeTestRule.onNodeWithText("預設貨幣").performClick()
        composeTestRule.onNodeWithText("選擇預設貨幣").assertIsDisplayed()

        // Select JPY from the dialog
        composeTestRule.onNodeWithText("JPY").performClick()

        assertTrue(selectedCurrency != null)
        // Dialog should be dismissed after selection
        composeTestRule.onNodeWithText("選擇預設貨幣").assertDoesNotExist()
    }
}
