package io.jacob.episodive.core.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.jacob.episodive.core.data.repository.EpisodeRepositoryImpl
import io.jacob.episodive.core.data.repository.FeedRepositoryImpl
import io.jacob.episodive.core.data.repository.PodcastRepositoryImpl
import io.jacob.episodive.core.domain.repository.EpisodeRepository
import io.jacob.episodive.core.domain.repository.FeedRepository
import io.jacob.episodive.core.domain.repository.PodcastRepository
import io.jacob.episodive.core.network.NetworkDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun providePodcastRepository(
        networkDataSource: NetworkDataSource
    ): PodcastRepository {
        return PodcastRepositoryImpl(
            networkDataSource = networkDataSource
        )
    }

    @Provides
    @Singleton
    fun provideEpisodeRepository(
        networkDataSource: NetworkDataSource
    ): EpisodeRepository {
        return EpisodeRepositoryImpl(
            networkDataSource = networkDataSource
        )
    }

    @Provides
    @Singleton
    fun provideFeedRepository(
        networkDataSource: NetworkDataSource
    ): FeedRepository {
        return FeedRepositoryImpl(
            networkDataSource = networkDataSource
        )
    }
}