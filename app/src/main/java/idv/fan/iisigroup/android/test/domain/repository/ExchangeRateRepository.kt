package idv.fan.iisigroup.android.test.domain.repository

import idv.fan.iisigroup.android.test.domain.model.Currency
import idv.fan.iisigroup.android.test.domain.model.ExchangeRate
import idv.fan.iisigroup.android.test.network.ApiResult

interface ExchangeRateRepository {
    suspend fun getExchangeRates(baseCurrency: Currency): ApiResult<List<ExchangeRate>>
}
