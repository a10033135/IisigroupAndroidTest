package idv.fan.iisigroup.android.test.ui.state

import idv.fan.iisigroup.android.test.core.IssConstants
import idv.fan.iisigroup.android.test.domain.model.Currency
import idv.fan.iisigroup.android.test.domain.model.ExchangeRate

sealed class ExchangeRateUiState {
    data object Loading : ExchangeRateUiState()
    data class Success(
        val rates: List<ExchangeRate>,
        val baseCurrency: Currency,
        val lastRefreshTime: String,
        val showCurrencyPicker: Boolean = false,
        val isRefreshing: Boolean = false,
        val refreshError: String? = null,
        val showCalculator: Boolean = false,
        val calculatorAmount: Double = IssConstants.ExchangeRate.DEFAULT_CALCULATOR_AMOUNT,
    ) : ExchangeRateUiState()
    data class Error(val message: String) : ExchangeRateUiState()
}
