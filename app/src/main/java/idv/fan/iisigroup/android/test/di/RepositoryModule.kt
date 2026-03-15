package idv.fan.iisigroup.android.test.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import idv.fan.iisigroup.android.test.data.repository.PostRepositoryImpl
import idv.fan.iisigroup.android.test.domain.repository.PostRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPostRepository(impl: PostRepositoryImpl): PostRepository
}
