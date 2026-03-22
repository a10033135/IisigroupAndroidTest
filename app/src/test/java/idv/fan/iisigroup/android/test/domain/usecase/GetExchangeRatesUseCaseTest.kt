package idv.fan.iisigroup.android.test.domain.usecase

import idv.fan.iisigroup.android.test.domain.model.Currency
import idv.fan.iisigroup.android.test.domain.model.ExchangeRate
import idv.fan.iisigroup.android.test.domain.repository.ExchangeRateRepository
import idv.fan.iisigroup.android.test.network.ApiResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetExchangeRatesUseCaseTest {

    private lateinit var repository: ExchangeRateRepository
    private lateinit var useCase: GetExchangeRatesUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetExchangeRatesUseCase(repository)
    }

    // -------------------------------------------------------------------------
    // 1. passes baseCurrency to repository
    // -------------------------------------------------------------------------
    @Test
    fun `passes baseCurrency to repository`() = runTest {
        coEvery { repository.getExchangeRates(Currency.JPY) } returns ApiResult.Success(emptyList())

        useCase(Currency.JPY)

        coVerify(exactly = 1) { repository.getExchangeRates(Currency.JPY) }
    }

    // -------------------------------------------------------------------------
    // 2. returns Success from repository
    // -------------------------------------------------------------------------
    @Test
    fun `returns Success from repository`() = runTest {
        val rates = listOf(ExchangeRate(Currency.JPY, 150.0))
        coEvery { repository.getExchangeRates(Currency.USD) } returns ApiResult.Success(rates)

        val result = useCase(Currency.USD)

        assertTrue(result is ApiResult.Success)
        assertEquals(rates, (result as ApiResult.Success).data)
    }

    // -------------------------------------------------------------------------
    // 3. returns Error from repository
    // -------------------------------------------------------------------------
    @Test
    fun `returns Error from repository`() = runTest {
        coEvery { repository.getExchangeRates(Currency.USD) } returns ApiResult.Error("API error")

        val result = useCase(Currency.USD)

        assertTrue(result is ApiResult.Error)
        assertEquals("API error", (result as ApiResult.Error).message)
    }
}
