package idv.fan.iisigroup.android.test.feature.exchangeRate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import idv.fan.iisigroup.android.test.core.IssConstants
import idv.fan.iisigroup.android.test.data.local.datastore.UserPreferencesDataStore
import idv.fan.iisigroup.android.test.domain.model.Currency
import idv.fan.iisigroup.android.test.domain.usecase.GetExchangeRatesUseCase
import idv.fan.iisigroup.android.test.network.ApiResult
import idv.fan.iisigroup.android.test.ui.state.ExchangeApiState
import idv.fan.iisigroup.android.test.ui.state.ExchangeRateUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ExchangeRateViewModel @Inject constructor(
    private val getExchangeRatesUseCase: GetExchangeRatesUseCase,
    private val userPreferencesDataStore: UserPreferencesDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExchangeRateUiState>(ExchangeRateUiState.Loading)
    val uiState: StateFlow<ExchangeRateUiState> = _uiState.asStateFlow()

    private var currentBaseCurrency: Currency = IssConstants.UserPreferences.DEFAULT_CURRENCY

    private var currentAutoSyncEnabled: Boolean = IssConstants.UserPreferences.DEFAULT_AUTO_SYNC_ENABLED
    private var currentAutoSyncIntervalMs: Long = IssConstants.UserPreferences.DEFAULT_SYNC_INTERVAL.ms

    private var loadJob: Job? = null
    private var refreshJob: Job? = null

    init {
        observeAutoSyncSettings()
        observeDefaultCurrency()
    }

    private fun observeAutoSyncSettings() {
        viewModelScope.launch {
            combine(
                userPreferencesDataStore.autoSyncEnabled,
                userPreferencesDataStore.autoSyncIntervalMs,
            ) { enabled, intervalMs -> enabled to intervalMs }
                .collect { (enabled, intervalMs) ->
                    val changed = enabled != currentAutoSyncEnabled || intervalMs != currentAutoSyncIntervalMs
                    currentAutoSyncEnabled = enabled
                    currentAutoSyncIntervalMs = intervalMs
                    if (changed && _uiState.value is ExchangeRateUiState.Success) {
                        refreshJob?.cancel()
                        if (enabled) startAutoRefresh()
                    }
                }
        }
    }

    private fun observeDefaultCurrency() {
        viewModelScope.launch {
            userPreferencesDataStore.defaultCurrencyCode
                .map { code -> Currency.entries.find { it.code == code } ?: Currency.USD }
                .collect { currency ->
                    currentBaseCurrency = currency
                    loadRates()
                }
        }
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
                        apiState = ExchangeApiState(
                            rates = result.data,
                            baseCurrency = currentBaseCurrency,
                            lastRefreshTime = currentTime(),
                        ),
                    )
                    if (currentAutoSyncEnabled) startAutoRefresh()
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
            _uiState.value = current.copy(
                apiState = current.apiState.copy(isRefreshing = true, refreshError = null),
            )
            when (val result = getExchangeRatesUseCase(currentBaseCurrency)) {
                is ApiResult.Success -> {
                    _uiState.value = current.copy(
                        apiState = ExchangeApiState(
                            rates = result.data,
                            baseCurrency = currentBaseCurrency,
                            lastRefreshTime = currentTime(),
                        ),
                    )
                    if (currentAutoSyncEnabled) startAutoRefresh()
                }
                is ApiResult.Error -> {
                    _uiState.value = current.copy(
                        apiState = current.apiState.copy(isRefreshing = false, refreshError = result.message),
                    )
                    if (currentAutoSyncEnabled) startAutoRefresh()
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

    fun onCalculatorOpen() {
        val current = _uiState.value as? ExchangeRateUiState.Success ?: return
        _uiState.value = current.copy(calculatorState = current.calculatorState.copy(showCalculator = true))
    }

    fun onCalculatorDismiss() {
        val current = _uiState.value as? ExchangeRateUiState.Success ?: return
        _uiState.value = current.copy(calculatorState = current.calculatorState.copy(showCalculator = false))
    }

    fun onCalculatorConfirm(amount: Double) {
        val current = _uiState.value as? ExchangeRateUiState.Success ?: return
        _uiState.value = current.copy(
            calculatorState = current.calculatorState.copy(amount = amount, showCalculator = false),
        )
    }

    /**
     * 此為 session 層級的臨時幣別切換，不持久化至 DataStore。
     * 應用重啟或設定頁更改預設幣別時，將會覆蓋此臨時選擇。
     * 若需永久變更，請透過設定頁的「預設貨幣」功能。
     */
    fun onBaseCurrencySelected(currency: Currency) {
        currentBaseCurrency = currency
        val current = _uiState.value as? ExchangeRateUiState.Success
        if (current != null) {
            _uiState.value = current.copy(showCurrencyPicker = false)
        }
        loadRates()
    }

    private fun startAutoRefresh() {
        refreshJob = viewModelScope.launch {
            while (true) {
                delay(currentAutoSyncIntervalMs)
                val current = _uiState.value as? ExchangeRateUiState.Success ?: break
                Timber.d("Auto-refreshing exchange rates")
                _uiState.value = current.copy(
                    apiState = current.apiState.copy(isRefreshing = true, refreshError = null),
                )
                when (val result = getExchangeRatesUseCase(currentBaseCurrency)) {
                    is ApiResult.Success -> {
                        _uiState.value = current.copy(
                            apiState = ExchangeApiState(
                                rates = result.data,
                                baseCurrency = currentBaseCurrency,
                                lastRefreshTime = currentTime(),
                            ),
                        )
                    }
                    is ApiResult.Error -> {
                        _uiState.value = current.copy(
                            apiState = current.apiState.copy(isRefreshing = false, refreshError = result.message),
                        )
                    }
                }
            }
        }
    }

    private fun currentTime(): String =
        LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
}
