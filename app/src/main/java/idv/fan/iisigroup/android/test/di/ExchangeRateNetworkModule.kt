package idv.fan.iisigroup.android.test.di

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import idv.fan.iisigroup.android.test.BuildConfig
import idv.fan.iisigroup.android.test.addFlipperNetworkInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import idv.fan.iisigroup.android.test.data.remote.api.ExchangeRateApiService
import idv.fan.iisigroup.android.test.network.ApiKeyInterceptor
import idv.fan.iisigroup.android.test.network.NetworkErrorInterceptor
import android.content.Context
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
    fun provideExchangeRateOkHttpClient(@ApplicationContext context: Context): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(NetworkErrorInterceptor(context))
            .addInterceptor(ApiKeyInterceptor(BuildConfig.EXCHANGE_RATE_API_KEY))
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) Level.BODY else Level.NONE
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
