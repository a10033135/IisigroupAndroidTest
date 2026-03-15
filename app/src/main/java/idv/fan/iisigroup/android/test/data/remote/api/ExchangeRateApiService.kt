package idv.fan.iisigroup.android.test.data.remote.api

import idv.fan.iisigroup.android.test.BuildConfig
import idv.fan.iisigroup.android.test.data.remote.model.ExchangeRateResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ExchangeRateApiService {

    @GET("latest")
    suspend fun getLatestRates(
        @Query("base_currency") baseCurrency: String,
        @Query("currencies") currencies: String,
        @Query("apikey") apiKey: String = BuildConfig.EXCHANGE_RATE_API_KEY
    ): ExchangeRateResponse
}
