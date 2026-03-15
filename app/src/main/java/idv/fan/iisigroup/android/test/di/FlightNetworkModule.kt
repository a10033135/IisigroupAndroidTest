package idv.fan.iisigroup.android.test.di

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import idv.fan.iisigroup.android.test.BuildConfig
import idv.fan.iisigroup.android.test.data.remote.api.FlightApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FlightRetrofit

@Module
@InstallIn(SingletonComponent::class)
object FlightNetworkModule {

    @Provides
    @Singleton
    @FlightRetrofit
    fun provideFlightRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
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
