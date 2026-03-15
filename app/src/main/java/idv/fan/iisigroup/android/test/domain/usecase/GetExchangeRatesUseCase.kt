package idv.fan.iisigroup.android.test.domain.usecase

import idv.fan.iisigroup.android.test.domain.model.Currency
import idv.fan.iisigroup.android.test.domain.model.ExchangeRate
import idv.fan.iisigroup.android.test.domain.repository.ExchangeRateRepository
import idv.fan.iisigroup.android.test.network.ApiResult
import javax.inject.Inject

class GetExchangeRatesUseCase @Inject constructor(
    private val repository: ExchangeRateRepository,
) {
    suspend operator fun invoke(baseCurrency: Currency): ApiResult<List<ExchangeRate>> =
        repository.getExchangeRates(baseCurrency)
}
