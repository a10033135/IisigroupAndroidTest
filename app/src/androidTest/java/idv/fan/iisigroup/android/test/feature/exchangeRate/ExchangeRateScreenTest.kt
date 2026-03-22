package idv.fan.iisigroup.android.test.feature.exchangeRate

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import idv.fan.iisigroup.android.test.domain.model.Currency
import idv.fan.iisigroup.android.test.domain.model.ExchangeRate
import idv.fan.iisigroup.android.test.ui.state.CalculatorState
import idv.fan.iisigroup.android.test.ui.state.ExchangeApiState
import idv.fan.iisigroup.android.test.ui.state.ExchangeRateUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExchangeRateScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun makeApiState(
        rates: List<ExchangeRate> = emptyList(),
        baseCurrency: Currency = Currency.USD,
        lastRefreshTime: String = "2024-01-01 00:00",
        isRefreshing: Boolean = false,
        refreshError: String? = null,
    ) = ExchangeApiState(
        rates = rates,
        baseCurrency = baseCurrency,
        lastRefreshTime = lastRefreshTime,
        isRefreshing = isRefreshing,
        refreshError = refreshError,
    )

    private fun makeSuccessState(
        rates: List<ExchangeRate> = emptyList(),
        baseCurrency: Currency = Currency.USD,
        lastRefreshTime: String = "2024-01-01 00:00",
        isRefreshing: Boolean = false,
        refreshError: String? = null,
        calculatorAmount: Double? = null,
        showCalculator: Boolean = false,
        showCurrencyPicker: Boolean = false,
    ) = ExchangeRateUiState.Success(
        apiState = makeApiState(
            rates = rates,
            baseCurrency = baseCurrency,
            lastRefreshTime = lastRefreshTime,
            isRefreshing = isRefreshing,
            refreshError = refreshError,
        ),
        calculatorState = CalculatorState(amount = calculatorAmount, showCalculator = showCalculator),
        showCurrencyPicker = showCurrencyPicker,
    )

    @Test
    fun loadingState_doesNotShowSuccessOrErrorContent() {
        composeTestRule.setContent {
            MaterialTheme {
                ExchangeRateScreen(
                    uiState = ExchangeRateUiState.Loading,
                    onRetry = {},
                    onBaseCurrencyClick = {},
                    onCurrencyPickerDismiss = {},
                    onBaseCurrencySelected = {},
                    onPullToRefresh = {},
                    onCalculatorClick = {},
                    onCalculatorDismiss = {},
                    onCalculatorConfirm = {},
                    contentPadding = PaddingValues(),
                )
            }
        }

        composeTestRule.onNodeWithText("重新嘗試").assertDoesNotExist()
        composeTestRule.onNodeWithText("目前無任何匯率資訊").assertDoesNotExist()
    }

    @Test
    fun errorState_showsMessageAndRetry() {
        composeTestRule.setContent {
            MaterialTheme {
                ExchangeRateScreen(
                    uiState = ExchangeRateUiState.Error("error msg"),
                    onRetry = {},
                    onBaseCurrencyClick = {},
                    onCurrencyPickerDismiss = {},
                    onBaseCurrencySelected = {},
                    onPullToRefresh = {},
                    onCalculatorClick = {},
                    onCalculatorDismiss = {},
                    onCalculatorConfirm = {},
                    contentPadding = PaddingValues(),
                )
            }
        }

        composeTestRule.onNodeWithText("error msg").assertIsDisplayed()
        composeTestRule.onNodeWithText("重新嘗試").assertIsDisplayed()
    }

    @Test
    fun successWithEmptyRates_showsEmptyMessage() {
        composeTestRule.setContent {
            MaterialTheme {
                ExchangeRateScreen(
                    uiState = makeSuccessState(rates = emptyList()),
                    onRetry = {},
                    onBaseCurrencyClick = {},
                    onCurrencyPickerDismiss = {},
                    onBaseCurrencySelected = {},
                    onPullToRefresh = {},
                    onCalculatorClick = {},
                    onCalculatorDismiss = {},
                    onCalculatorConfirm = {},
                    contentPadding = PaddingValues(),
                )
            }
        }

        composeTestRule.onNodeWithText("目前無任何匯率資訊").assertIsDisplayed()
    }

    @Test
    fun successWithRates_showsCurrencyItems() {
        composeTestRule.setContent {
            MaterialTheme {
                ExchangeRateScreen(
                    uiState = makeSuccessState(rates = listOf(ExchangeRate(Currency.JPY, 150.0))),
                    onRetry = {},
                    onBaseCurrencyClick = {},
                    onCurrencyPickerDismiss = {},
                    onBaseCurrencySelected = {},
                    onPullToRefresh = {},
                    onCalculatorClick = {},
                    onCalculatorDismiss = {},
                    onCalculatorConfirm = {},
                    contentPadding = PaddingValues(),
                )
            }
        }

        composeTestRule.onNodeWithText("JPY").assertIsDisplayed()
    }

    @Test
    fun calculatorBanner_showsWelcomeTextWhenNoAmount() {
        composeTestRule.setContent {
            MaterialTheme {
                ExchangeRateScreen(
                    uiState = makeSuccessState(calculatorAmount = null),
                    onRetry = {},
                    onBaseCurrencyClick = {},
                    onCurrencyPickerDismiss = {},
                    onBaseCurrencySelected = {},
                    onPullToRefresh = {},
                    onCalculatorClick = {},
                    onCalculatorDismiss = {},
                    onCalculatorConfirm = {},
                    contentPadding = PaddingValues(),
                )
            }
        }

        composeTestRule.onNodeWithText("歡迎使用計算機").assertIsDisplayed()
    }

    @Test
    fun calculatorBanner_showsAmountWhenSet() {
        composeTestRule.setContent {
            MaterialTheme {
                ExchangeRateScreen(
                    uiState = makeSuccessState(calculatorAmount = 100.0, baseCurrency = Currency.USD),
                    onRetry = {},
                    onBaseCurrencyClick = {},
                    onCurrencyPickerDismiss = {},
                    onBaseCurrencySelected = {},
                    onPullToRefresh = {},
                    onCalculatorClick = {},
                    onCalculatorDismiss = {},
                    onCalculatorConfirm = {},
                    contentPadding = PaddingValues(),
                )
            }
        }

        composeTestRule.onNodeWithText("100 USD", substring = true).assertIsDisplayed()
    }

    @Test
    fun refreshing_showsProgressIndicatorText() {
        composeTestRule.setContent {
            MaterialTheme {
                ExchangeRateScreen(
                    uiState = makeSuccessState(isRefreshing = true),
                    onRetry = {},
                    onBaseCurrencyClick = {},
                    onCurrencyPickerDismiss = {},
                    onBaseCurrencySelected = {},
                    onPullToRefresh = {},
                    onCalculatorClick = {},
                    onCalculatorDismiss = {},
                    onCalculatorConfirm = {},
                    contentPadding = PaddingValues(),
                )
            }
        }

        composeTestRule.onNodeWithText("刷新中").assertIsDisplayed()
    }

    @Test
    fun showCurrencyPicker_showsDialog() {
        composeTestRule.setContent {
            MaterialTheme {
                ExchangeRateScreen(
                    uiState = makeSuccessState(showCurrencyPicker = true),
                    onRetry = {},
                    onBaseCurrencyClick = {},
                    onCurrencyPickerDismiss = {},
                    onBaseCurrencySelected = {},
                    onPullToRefresh = {},
                    onCalculatorClick = {},
                    onCalculatorDismiss = {},
                    onCalculatorConfirm = {},
                    contentPadding = PaddingValues(),
                )
            }
        }

        composeTestRule.onNodeWithText("選擇基準貨幣").assertIsDisplayed()
    }

    @Test
    fun showCalculator_showsBottomSheet() {
        composeTestRule.setContent {
            MaterialTheme {
                ExchangeRateScreen(
                    uiState = makeSuccessState(showCalculator = true),
                    onRetry = {},
                    onBaseCurrencyClick = {},
                    onCurrencyPickerDismiss = {},
                    onBaseCurrencySelected = {},
                    onPullToRefresh = {},
                    onCalculatorClick = {},
                    onCalculatorDismiss = {},
                    onCalculatorConfirm = {},
                    contentPadding = PaddingValues(),
                )
            }
        }

        composeTestRule.onNodeWithText("輸入").assertIsDisplayed()
    }

    @Test
    fun bannerClick_triggersOnCalculatorClick() {
        var calculatorClicked = false
        composeTestRule.setContent {
            MaterialTheme {
                ExchangeRateScreen(
                    uiState = makeSuccessState(calculatorAmount = null),
                    onRetry = {},
                    onBaseCurrencyClick = {},
                    onCurrencyPickerDismiss = {},
                    onBaseCurrencySelected = {},
                    onPullToRefresh = {},
                    onCalculatorClick = { calculatorClicked = true },
                    onCalculatorDismiss = {},
                    onCalculatorConfirm = {},
                    contentPadding = PaddingValues(),
                )
            }
        }

        composeTestRule.onNodeWithText("歡迎使用計算機").performClick()
        assertTrue(calculatorClicked)
    }

    @Test
    fun updateInfoRowClick_triggersOnBaseCurrencyClick() {
        var baseCurrencyClicked = false
        composeTestRule.setContent {
            MaterialTheme {
                ExchangeRateScreen(
                    uiState = makeSuccessState(
                        calculatorAmount = null,
                        lastRefreshTime = "2024-01-01 00:00",
                    ),
                    onRetry = {},
                    onBaseCurrencyClick = { baseCurrencyClicked = true },
                    onCurrencyPickerDismiss = {},
                    onBaseCurrencySelected = {},
                    onPullToRefresh = {},
                    onCalculatorClick = {},
                    onCalculatorDismiss = {},
                    onCalculatorConfirm = {},
                    contentPadding = PaddingValues(),
                )
            }
        }

        composeTestRule.onNodeWithText("2024-01-01 00:00", substring = true).performClick()
        assertTrue(baseCurrencyClicked)
    }
}
