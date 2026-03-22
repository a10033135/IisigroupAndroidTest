package idv.fan.iisigroup.android.test.data.repository

import android.content.Context
import idv.fan.iisigroup.android.test.R
import idv.fan.iisigroup.android.test.data.remote.api.ExchangeRateApiService
import idv.fan.iisigroup.android.test.data.remote.model.ExchangeRateResponse
import idv.fan.iisigroup.android.test.domain.model.Currency
import idv.fan.iisigroup.android.test.network.ApiResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExchangeRateRepositoryImplTest {

    private lateinit var apiService: ExchangeRateApiService
    private lateinit var context: Context
    private lateinit var repository: ExchangeRateRepositoryImpl

    private val unknownErrorMessage = "Unknown error occurred"

    @Before
    fun setUp() {
        apiService = mockk()
        context = mockk()
        every { context.getString(R.string.error_unknown) } returns unknownErrorMessage
        repository = ExchangeRateRepositoryImpl(apiService, context)
    }

    // -------------------------------------------------------------------------
    // 1. getExchangeRates success returns only known Currency codes
    // -------------------------------------------------------------------------
    @Test
    fun `getExchangeRates success returns only known Currency codes`() = runTest {
        coEvery {
            apiService.getLatestRates(
                baseCurrency = "USD",
                currencies = any(),
            )
        } returns ExchangeRateResponse(data = mapOf("JPY" to 150.0, "GBP" to 0.79))

        val result = repository.getExchangeRates(Currency.USD)

        assertTrue(result is ApiResult.Success)
        val rates = (result as ApiResult.Success).data
        val codes = rates.map { it.currency.code }
        assertTrue(codes.contains("JPY"))
        assertTrue(codes.contains("GBP"))
    }

    // -------------------------------------------------------------------------
    // 2. getExchangeRates filters out unknown currency codes
    // -------------------------------------------------------------------------
    @Test
    fun `getExchangeRates filters out unknown currency codes`() = runTest {
        coEvery {
            apiService.getLatestRates(
                baseCurrency = "USD",
                currencies = any(),
            )
        } returns ExchangeRateResponse(data = mapOf("JPY" to 150.0, "XYZ" to 999.0))

        val result = repository.getExchangeRates(Currency.USD)

        assertTrue(result is ApiResult.Success)
        val rates = (result as ApiResult.Success).data
        val codes = rates.map { it.currency.code }
        assertTrue(codes.contains("JPY"))
        assertFalse(codes.contains("XYZ"))
    }

    // -------------------------------------------------------------------------
    // 3. getExchangeRates exception returns Error
    // -------------------------------------------------------------------------
    @Test
    fun `getExchangeRates exception returns Error`() = runTest {
        coEvery {
            apiService.getLatestRates(
                baseCurrency = any(),
                currencies = any(),
            )
        } throws RuntimeException("Connection failed")

        val result = repository.getExchangeRates(Currency.USD)

        assertTrue(result is ApiResult.Error)
        assertEquals("Connection failed", (result as ApiResult.Error).message)
    }

    // -------------------------------------------------------------------------
    // 4. getExchangeRates rate value is correctly mapped
    // -------------------------------------------------------------------------
    @Test
    fun `getExchangeRates rate value is correctly mapped`() = runTest {
        coEvery {
            apiService.getLatestRates(
                baseCurrency = "USD",
                currencies = any(),
            )
        } returns ExchangeRateResponse(data = mapOf("JPY" to 150.0))

        val result = repository.getExchangeRates(Currency.USD) as ApiResult.Success

        val jpyRate = result.data.first { it.currency == Currency.JPY }
        assertEquals(150.0, jpyRate.rate, 0.001)
    }
}
