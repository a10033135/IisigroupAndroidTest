package idv.fan.iisigroup.android.test.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import idv.fan.iisigroup.android.test.data.repository.ExchangeRateRepositoryImpl
import idv.fan.iisigroup.android.test.domain.repository.ExchangeRateRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ExchangeRateRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindExchangeRateRepository(impl: ExchangeRateRepositoryImpl): ExchangeRateRepository
}
