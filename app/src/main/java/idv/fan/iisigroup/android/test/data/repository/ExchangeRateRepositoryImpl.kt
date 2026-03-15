package idv.fan.iisigroup.android.test.data.repository

import idv.fan.iisigroup.android.test.data.remote.api.ExchangeRateApiService
import idv.fan.iisigroup.android.test.domain.model.Currency
import idv.fan.iisigroup.android.test.domain.model.ExchangeRate
import idv.fan.iisigroup.android.test.domain.repository.ExchangeRateRepository
import idv.fan.iisigroup.android.test.network.ApiResult
import java.net.ConnectException
import java.net.SocketException
import java.net.UnknownHostException
import javax.inject.Inject

class ExchangeRateRepositoryImpl @Inject constructor(
    private val apiService: ExchangeRateApiService,
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
            onFailure = { throwable ->
                val message = when (throwable) {
                    is UnknownHostException, is SocketException, is ConnectException ->
                        "網路異常，請確認網路連線後再試"
                    else -> throwable.message ?: "發生未知錯誤"
                }
                ApiResult.Error(message, throwable)
            },
        )
}
