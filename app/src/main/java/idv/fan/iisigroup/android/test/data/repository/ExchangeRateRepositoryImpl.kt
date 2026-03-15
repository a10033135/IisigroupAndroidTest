package idv.fan.iisigroup.android.test.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import idv.fan.iisigroup.android.test.R
import idv.fan.iisigroup.android.test.data.remote.api.ExchangeRateApiService
import idv.fan.iisigroup.android.test.domain.model.Currency
import idv.fan.iisigroup.android.test.domain.model.ExchangeRate
import idv.fan.iisigroup.android.test.domain.repository.ExchangeRateRepository
import idv.fan.iisigroup.android.test.network.ApiResult
import javax.inject.Inject

class ExchangeRateRepositoryImpl @Inject constructor(
    private val apiService: ExchangeRateApiService,
    @ApplicationContext private val context: Context,
) : ExchangeRateRepository {

    override suspend fun getExchangeRates(baseCurrency: Currency): ApiResult<List<ExchangeRate>> =
        runCatching {
            val currenciesParam = Currency.entries
                .filter { it != baseCurrency }
                .joinToString(",") { it.code }

            val response = apiService.getLatestRates(
                baseCurrency = baseCurrency.code,
                currencies = currenciesParam,
            )

            response.data.mapNotNull { (code, rate) ->
                val currency = Currency.entries.firstOrNull { it.code == code } ?: return@mapNotNull null
                ExchangeRate(currency = currency, rate = rate)
            }
        }.fold(
            onSuccess = { ApiResult.Success(it) },
            onFailure = { ApiResult.Error(it.message ?: context.getString(R.string.error_unknown), it) },
        )
}
