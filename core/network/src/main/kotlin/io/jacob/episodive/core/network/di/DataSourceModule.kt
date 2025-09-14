package io.jacob.episodive.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.jacob.episodive.core.network.NetworkDataSource
import io.jacob.episodive.core.network.NetworkDataSourceImpl
import io.jacob.episodive.core.network.api.PodcastIndexApi
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {
    @Provides
    @Singleton
    fun provideNetworkDataSource(
        api: PodcastIndexApi
    ): NetworkDataSource {
        return NetworkDataSourceImpl(api)
    }
}