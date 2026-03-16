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
import idv.fan.iisigroup.android.test.data.remote.api.FlightApiService
import idv.fan.iisigroup.android.test.network.FlightJsRedirectInterceptor
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
annotation class FlightRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FlightOkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object FlightNetworkModule {

    @Provides
    @Singleton
    @FlightOkHttpClient
    fun provideFlightOkHttpClient(@ApplicationContext context: Context): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(NetworkErrorInterceptor(context))
            .addInterceptor(FlightJsRedirectInterceptor())
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) Level.BODY else Level.NONE
            })
            .addFlipperNetworkInterceptor()
            .build()

    @Provides
    @Singleton
    @FlightRetrofit
    fun provideFlightRetrofit(@FlightOkHttpClient okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.FLIGHT_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    fun provideFlightApiService(@FlightRetrofit retrofit: Retrofit): FlightApiService =
        retrofit.create(FlightApiService::class.java)
}
