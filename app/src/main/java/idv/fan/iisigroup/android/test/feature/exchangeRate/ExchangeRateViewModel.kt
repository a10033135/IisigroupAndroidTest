package idv.fan.iisigroup.android.test.feature.exchangeRate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import idv.fan.iisigroup.android.test.domain.model.Currency
import idv.fan.iisigroup.android.test.domain.usecase.GetExchangeRatesUseCase
import idv.fan.iisigroup.android.test.network.ApiResult
import idv.fan.iisigroup.android.test.ui.state.ExchangeRateUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ExchangeRateViewModel @Inject constructor(
    private val getExchangeRatesUseCase: GetExchangeRatesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExchangeRateUiState>(ExchangeRateUiState.Loading)
    val uiState: StateFlow<ExchangeRateUiState> = _uiState.asStateFlow()

    private var currentBaseCurrency: Currency = Currency.TWD
    private var loadJob: Job? = null
    private var refreshJob: Job? = null

    init {
        loadRates()
    }

    fun loadRates() {
        loadJob?.cancel()
        refreshJob?.cancel()
        loadJob = viewModelScope.launch {
            Timber.d("Loading exchange rates (base: ${currentBaseCurrency.code})")
            _uiState.value = ExchangeRateUiState.Loading
            when (val result = getExchangeRatesUseCase(currentBaseCurrency)) {
                is ApiResult.Success -> {
                    Timber.d("Loaded ${result.data.size} rates")
                    _uiState.value = ExchangeRateUiState.Success(
                        rates = result.data,
                        baseCurrency = currentBaseCurrency,
                        lastRefreshTime = currentTime(),
                    )
                    startAutoRefresh()
                }
                is ApiResult.Error -> {
                    Timber.e("Load failed: ${result.message}")
                    _uiState.value = ExchangeRateUiState.Error(result.message)
                }
            }
        }
    }

    fun pullToRefresh() {
        val current = _uiState.value as? ExchangeRateUiState.Success ?: return
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _uiState.value = current.copy(isRefreshing = true, refreshError = null)
            when (val result = getExchangeRatesUseCase(currentBaseCurrency)) {
                is ApiResult.Success -> {
                    _uiState.value = ExchangeRateUiState.Success(
                        rates = result.data,
                        baseCurrency = currentBaseCurrency,
                        lastRefreshTime = currentTime(),
                    )
                    startAutoRefresh()
                }
                is ApiResult.Error -> {
                    _uiState.value = current.copy(
                        isRefreshing = false,
                        refreshError = result.message,
                    )
                    startAutoRefresh()
                }
            }
        }
    }

    fun onBaseCurrencyClick() {
        val current = _uiState.value as? ExchangeRateUiState.Success ?: return
        _uiState.value = current.copy(showCurrencyPicker = true)
    }

    fun onCurrencyPickerDismiss() {
        val current = _uiState.value as? ExchangeRateUiState.Success ?: return
        _uiState.value = current.copy(showCurrencyPicker = false)
    }

    fun onBaseCurrencySelected(currency: Currency) {
        currentBaseCurrency = currency
        _uiState.value = (_uiState.value as? ExchangeRateUiState.Success)
            ?.copy(showCurrencyPicker = false)
            ?: _uiState.value
        loadRates()
    }

    private fun startAutoRefresh() {
        refreshJob = viewModelScope.launch {
            while (true) {
                delay(REFRESH_INTERVAL_MS)
                val current = _uiState.value as? ExchangeRateUiState.Success ?: break
                Timber.d("Auto-refreshing exchange rates")
                _uiState.value = current.copy(isRefreshing = true, refreshError = null)
                when (val result = getExchangeRatesUseCase(currentBaseCurrency)) {
                    is ApiResult.Success -> {
                        _uiState.value = ExchangeRateUiState.Success(
                            rates = result.data,
                            baseCurrency = currentBaseCurrency,
                            lastRefreshTime = currentTime(),
                        )
                    }
                    is ApiResult.Error -> {
                        _uiState.value = current.copy(
                            isRefreshing = false,
                            refreshError = result.message,
                        )
                    }
                }
            }
        }
    }

    private fun currentTime(): String =
        LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))

    companion object {
        const val REFRESH_INTERVAL_MS = 10_000L
    }
}
