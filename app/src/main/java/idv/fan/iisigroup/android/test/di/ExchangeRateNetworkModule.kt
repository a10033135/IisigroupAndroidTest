package idv.fan.iisigroup.android.test.di

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import idv.fan.iisigroup.android.test.BuildConfig
import idv.fan.iisigroup.android.test.addFlipperNetworkInterceptor
import idv.fan.iisigroup.android.test.data.remote.api.ExchangeRateApiService
import idv.fan.iisigroup.android.test.network.ApiKeyInterceptor
import idv.fan.iisigroup.android.test.network.NetworkErrorInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ExchangeRateRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ExchangeRateOkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object ExchangeRateNetworkModule {

    @Provides
    @Singleton
    @ExchangeRateOkHttpClient
    fun provideExchangeRateOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(NetworkErrorInterceptor())
            .addInterceptor(ApiKeyInterceptor(BuildConfig.EXCHANGE_RATE_API_KEY))
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addFlipperNetworkInterceptor()
            .build()

    @Provides
    @Singleton
    @ExchangeRateRetrofit
    fun provideExchangeRateRetrofit(
        @ExchangeRateOkHttpClient okHttpClient: OkHttpClient,
        moshi: Moshi,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.EXCHANGE_RATE_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    fun provideExchangeRateApiService(@ExchangeRateRetrofit retrofit: Retrofit): ExchangeRateApiService =
        retrofit.create(ExchangeRateApiService::class.java)
}
