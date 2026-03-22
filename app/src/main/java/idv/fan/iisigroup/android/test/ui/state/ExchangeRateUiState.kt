package idv.fan.iisigroup.android.test.ui.state

import idv.fan.iisigroup.android.test.core.IssConstants
import idv.fan.iisigroup.android.test.domain.model.Currency
import idv.fan.iisigroup.android.test.domain.model.ExchangeRate

data class ExchangeApiState(
    val rates: List<ExchangeRate>,
    val baseCurrency: Currency,
    val lastRefreshTime: String,
    val isRefreshing: Boolean = false,
    val refreshError: String? = null,
)

data class CalculatorState(
    val amount: Double = IssConstants.ExchangeRate.DEFAULT_CALCULATOR_AMOUNT,
    val showCalculator: Boolean = false,
)

sealed class ExchangeRateUiState {
    data object Loading : ExchangeRateUiState()
    data class Success(
        val apiState: ExchangeApiState,
        val calculatorState: CalculatorState = CalculatorState(),
        val showCurrencyPicker: Boolean = false,
    ) : ExchangeRateUiState()
    data class Error(val message: String) : ExchangeRateUiState()
}
