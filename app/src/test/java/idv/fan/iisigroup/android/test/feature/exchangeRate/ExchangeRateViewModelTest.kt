package idv.fan.iisigroup.android.test.feature.exchangeRate

import idv.fan.iisigroup.android.test.MainDispatcherRule
import idv.fan.iisigroup.android.test.data.local.datastore.UserPreferencesDataStore
import idv.fan.iisigroup.android.test.domain.model.Currency
import idv.fan.iisigroup.android.test.domain.model.ExchangeRate
import idv.fan.iisigroup.android.test.domain.usecase.GetExchangeRatesUseCase
import idv.fan.iisigroup.android.test.network.ApiResult
import idv.fan.iisigroup.android.test.ui.state.ExchangeRateUiState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExchangeRateViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private lateinit var getExchangeRatesUseCase: GetExchangeRatesUseCase
    private lateinit var userPreferencesDataStore: UserPreferencesDataStore
    private lateinit var viewModel: ExchangeRateViewModel

    private val sampleRates = listOf(
        ExchangeRate(Currency.JPY, 150.0),
        ExchangeRate(Currency.GBP, 0.79),
    )

    @Before
    fun setUp() {
        getExchangeRatesUseCase = mockk()
        userPreferencesDataStore = mockk()

        every { userPreferencesDataStore.autoSyncEnabled } returns flowOf(false)
        every { userPreferencesDataStore.autoSyncIntervalMs } returns flowOf(10_000L)
        every { userPreferencesDataStore.defaultCurrencyCode } returns flowOf("USD")
    }

    private fun createViewModel(): ExchangeRateViewModel =
        ExchangeRateViewModel(getExchangeRatesUseCase, userPreferencesDataStore)

    // -------------------------------------------------------------------------
    // 1. loadRates success transitions to Success state with correct rates
    // -------------------------------------------------------------------------
    @Test
    fun `loadRates success transitions to Success state with correct rates`() = runTest {
        coEvery { getExchangeRatesUseCase(Currency.USD) } returns ApiResult.Success(sampleRates)

        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ExchangeRateUiState.Success)
        val success = state as ExchangeRateUiState.Success
        assertEquals(sampleRates, success.apiState.rates)
        assertEquals(Currency.USD, success.apiState.baseCurrency)
    }

    // -------------------------------------------------------------------------
    // 2. loadRates error transitions to Error state
    // -------------------------------------------------------------------------
    @Test
    fun `loadRates error transitions to Error state`() = runTest {
        coEvery { getExchangeRatesUseCase(Currency.USD) } returns ApiResult.Error("Network error")

        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ExchangeRateUiState.Error)
        assertEquals("Network error", (state as ExchangeRateUiState.Error).message)
    }

    // -------------------------------------------------------------------------
    // 3. pullToRefresh success updates rates and preserves calculatorState
    // -------------------------------------------------------------------------
    @Test
    fun `pullToRefresh success updates rates and preserves calculatorState`() = runTest {
        val updatedRates = listOf(ExchangeRate(Currency.JPY, 155.0))
        coEvery { getExchangeRatesUseCase(Currency.USD) } returnsMany listOf(
            ApiResult.Success(sampleRates),
            ApiResult.Success(updatedRates),
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        // Open calculator and set amount to preserve
        viewModel.onCalculatorOpen()
        viewModel.onCalculatorConfirm(1000.0)

        viewModel.pullToRefresh()
        advanceUntilIdle()

        val state = viewModel.uiState.value as ExchangeRateUiState.Success
        assertEquals(updatedRates, state.apiState.rates)
        assertEquals(1000.0, state.calculatorState.amount)
    }

    // -------------------------------------------------------------------------
    // 4. pullToRefresh failure sets refreshError and preserves calculatorState
    // -------------------------------------------------------------------------
    @Test
    fun `pullToRefresh failure sets refreshError and preserves calculatorState`() = runTest {
        coEvery { getExchangeRatesUseCase(Currency.USD) } returnsMany listOf(
            ApiResult.Success(sampleRates),
            ApiResult.Error("Refresh failed"),
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onCalculatorConfirm(500.0)

        viewModel.pullToRefresh()
        advanceUntilIdle()

        val state = viewModel.uiState.value as ExchangeRateUiState.Success
        assertEquals("Refresh failed", state.apiState.refreshError)
        assertEquals(500.0, state.calculatorState.amount)
    }

    // -------------------------------------------------------------------------
    // 5. auto refresh does not reset calculatorState
    // -------------------------------------------------------------------------
    @Test
    fun `auto refresh does not reset calculatorState`() = runTest {
        every { userPreferencesDataStore.autoSyncEnabled } returns flowOf(true)
        every { userPreferencesDataStore.autoSyncIntervalMs } returns flowOf(10_000L)

        val refreshedRates = listOf(ExchangeRate(Currency.JPY, 160.0))
        coEvery { getExchangeRatesUseCase(Currency.USD) } returnsMany listOf(
            ApiResult.Success(sampleRates),
            ApiResult.Success(refreshedRates),
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onCalculatorConfirm(2000.0)

        advanceTimeBy(10_001L)
        advanceUntilIdle()

        val state = viewModel.uiState.value as ExchangeRateUiState.Success
        // calculatorState amount should be preserved after auto refresh
        assertEquals(2000.0, state.calculatorState.amount)
    }

    // -------------------------------------------------------------------------
    // 6. onCalculatorOpen sets showCalculator to true
    // -------------------------------------------------------------------------
    @Test
    fun `onCalculatorOpen sets showCalculator to true`() = runTest {
        coEvery { getExchangeRatesUseCase(Currency.USD) } returns ApiResult.Success(sampleRates)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onCalculatorOpen()

        val state = viewModel.uiState.value as ExchangeRateUiState.Success
        assertTrue(state.calculatorState.showCalculator)
    }

    // -------------------------------------------------------------------------
    // 7. onCalculatorDismiss sets showCalculator to false
    // -------------------------------------------------------------------------
    @Test
    fun `onCalculatorDismiss sets showCalculator to false`() = runTest {
        coEvery { getExchangeRatesUseCase(Currency.USD) } returns ApiResult.Success(sampleRates)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onCalculatorOpen()
        viewModel.onCalculatorDismiss()

        val state = viewModel.uiState.value as ExchangeRateUiState.Success
        assertFalse(state.calculatorState.showCalculator)
    }

    // -------------------------------------------------------------------------
    // 8. onCalculatorConfirm sets amount and hides calculator
    // -------------------------------------------------------------------------
    @Test
    fun `onCalculatorConfirm sets amount and hides calculator`() = runTest {
        coEvery { getExchangeRatesUseCase(Currency.USD) } returns ApiResult.Success(sampleRates)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onCalculatorOpen()
        viewModel.onCalculatorConfirm(123.45)

        val state = viewModel.uiState.value as ExchangeRateUiState.Success
        assertEquals(123.45, state.calculatorState.amount)
        assertFalse(state.calculatorState.showCalculator)
    }

    // -------------------------------------------------------------------------
    // 9. onBaseCurrencyClick sets showCurrencyPicker to true
    // -------------------------------------------------------------------------
    @Test
    fun `onBaseCurrencyClick sets showCurrencyPicker to true`() = runTest {
        coEvery { getExchangeRatesUseCase(Currency.USD) } returns ApiResult.Success(sampleRates)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onBaseCurrencyClick()

        val state = viewModel.uiState.value as ExchangeRateUiState.Success
        assertTrue(state.showCurrencyPicker)
    }

    // -------------------------------------------------------------------------
    // 10. onCurrencyPickerDismiss sets showCurrencyPicker to false
    // -------------------------------------------------------------------------
    @Test
    fun `onCurrencyPickerDismiss sets showCurrencyPicker to false`() = runTest {
        coEvery { getExchangeRatesUseCase(Currency.USD) } returns ApiResult.Success(sampleRates)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onBaseCurrencyClick()
        viewModel.onCurrencyPickerDismiss()

        val state = viewModel.uiState.value as ExchangeRateUiState.Success
        assertFalse(state.showCurrencyPicker)
    }

    // -------------------------------------------------------------------------
    // 11. onBaseCurrencySelected changes baseCurrency and triggers reload
    // -------------------------------------------------------------------------
    @Test
    fun `onBaseCurrencySelected changes baseCurrency and triggers reload`() = runTest {
        val jpyRates = listOf(ExchangeRate(Currency.USD, 0.0067))
        coEvery { getExchangeRatesUseCase(Currency.USD) } returns ApiResult.Success(sampleRates)
        coEvery { getExchangeRatesUseCase(Currency.JPY) } returns ApiResult.Success(jpyRates)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onBaseCurrencySelected(Currency.JPY)
        advanceUntilIdle()

        val state = viewModel.uiState.value as ExchangeRateUiState.Success
        assertEquals(Currency.JPY, state.apiState.baseCurrency)
        assertEquals(jpyRates, state.apiState.rates)
    }

    // -------------------------------------------------------------------------
    // 12. DataStore defaultCurrencyCode change triggers loadRates
    // -------------------------------------------------------------------------
    @Test
    fun `DataStore defaultCurrencyCode change triggers loadRates`() = runTest {
        val currencyCodeFlow = MutableStateFlow("USD")
        every { userPreferencesDataStore.defaultCurrencyCode } returns currencyCodeFlow

        val jpyRates = listOf(ExchangeRate(Currency.USD, 0.0067))
        coEvery { getExchangeRatesUseCase(Currency.USD) } returns ApiResult.Success(sampleRates)
        coEvery { getExchangeRatesUseCase(Currency.JPY) } returns ApiResult.Success(jpyRates)

        viewModel = createViewModel()
        advanceUntilIdle()

        val stateBeforeChange = viewModel.uiState.value as ExchangeRateUiState.Success
        assertEquals(Currency.USD, stateBeforeChange.apiState.baseCurrency)

        currencyCodeFlow.value = "JPY"
        advanceUntilIdle()

        val stateAfterChange = viewModel.uiState.value as ExchangeRateUiState.Success
        assertEquals(Currency.JPY, stateAfterChange.apiState.baseCurrency)
        assertEquals(jpyRates, stateAfterChange.apiState.rates)
    }
}
