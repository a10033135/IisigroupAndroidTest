package idv.fan.iisigroup.android.test.feature.setting

import idv.fan.iisigroup.android.test.MainDispatcherRule
import idv.fan.iisigroup.android.test.core.IssConstants
import idv.fan.iisigroup.android.test.data.local.datastore.UserPreferencesDataStore
import idv.fan.iisigroup.android.test.domain.model.Currency
import idv.fan.iisigroup.android.test.domain.model.SyncInterval
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private lateinit var userPreferencesDataStore: UserPreferencesDataStore
    private lateinit var viewModel: SettingViewModel

    @Before
    fun setUp() {
        userPreferencesDataStore = mockk()

        every { userPreferencesDataStore.isDarkTheme } returns
            flowOf(IssConstants.UserPreferences.DEFAULT_DARK_THEME)
        every { userPreferencesDataStore.autoSyncEnabled } returns
            flowOf(IssConstants.UserPreferences.DEFAULT_AUTO_SYNC_ENABLED)
        every { userPreferencesDataStore.autoSyncIntervalMs } returns
            flowOf(IssConstants.UserPreferences.DEFAULT_SYNC_INTERVAL.ms)
        every { userPreferencesDataStore.defaultCurrencyCode } returns
            flowOf(IssConstants.UserPreferences.DEFAULT_CURRENCY.code)

        coEvery { userPreferencesDataStore.setDarkTheme(any()) } just runs
        coEvery { userPreferencesDataStore.setAutoSyncEnabled(any()) } just runs
        coEvery { userPreferencesDataStore.setAutoSyncIntervalMs(any()) } just runs
        coEvery { userPreferencesDataStore.setDefaultCurrencyCode(any()) } just runs

        viewModel = SettingViewModel(userPreferencesDataStore)
    }

    // -------------------------------------------------------------------------
    // 1. initial uiState reflects DataStore default values
    // -------------------------------------------------------------------------
    @Test
    fun `initial uiState reflects DataStore default values`() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isDarkTheme)
        assertEquals(IssConstants.UserPreferences.DEFAULT_AUTO_SYNC_ENABLED, state.autoSyncEnabled)
        assertEquals(IssConstants.UserPreferences.DEFAULT_SYNC_INTERVAL, state.autoSyncInterval)
        assertEquals(IssConstants.UserPreferences.DEFAULT_CURRENCY, state.defaultCurrency)
    }

    // -------------------------------------------------------------------------
    // 2. setDarkTheme calls DataStore setDarkTheme
    // -------------------------------------------------------------------------
    @Test
    fun `setDarkTheme calls DataStore setDarkTheme`() = runTest {
        viewModel.setDarkTheme(true)
        advanceUntilIdle()

        coVerify { userPreferencesDataStore.setDarkTheme(true) }
    }

    // -------------------------------------------------------------------------
    // 3. setAutoSyncEnabled calls DataStore setAutoSyncEnabled
    // -------------------------------------------------------------------------
    @Test
    fun `setAutoSyncEnabled calls DataStore setAutoSyncEnabled`() = runTest {
        viewModel.setAutoSyncEnabled(false)
        advanceUntilIdle()

        coVerify { userPreferencesDataStore.setAutoSyncEnabled(false) }
    }

    // -------------------------------------------------------------------------
    // 4. setAutoSyncInterval calls DataStore setAutoSyncIntervalMs with correct ms
    // -------------------------------------------------------------------------
    @Test
    fun `setAutoSyncInterval calls DataStore setAutoSyncIntervalMs with correct ms`() = runTest {
        val interval = SyncInterval.ONE_MINUTE

        viewModel.setAutoSyncInterval(interval)
        advanceUntilIdle()

        coVerify { userPreferencesDataStore.setAutoSyncIntervalMs(interval.ms) }
    }

    // -------------------------------------------------------------------------
    // 5. setDefaultCurrency calls DataStore setDefaultCurrencyCode with correct code
    // -------------------------------------------------------------------------
    @Test
    fun `setDefaultCurrency calls DataStore setDefaultCurrencyCode with correct code`() = runTest {
        val currency = Currency.JPY

        viewModel.setDefaultCurrency(currency)
        advanceUntilIdle()

        coVerify { userPreferencesDataStore.setDefaultCurrencyCode(currency.code) }
    }
}
