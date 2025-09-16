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
import io.jacob.episodive.core.network.datasource.EpisodeRemoteDataSource
import io.jacob.episodive.core.network.datasource.FeedRemoteDataSource
import io.jacob.episodive.core.network.datasource.PodcastRemoteDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun providePodcastRepository(
        podcastRemoteDataSource: PodcastRemoteDataSource,
    ): PodcastRepository {
        return PodcastRepositoryImpl(
            podcastRemoteDataSource = podcastRemoteDataSource,
        )
    }

    @Provides
    @Singleton
    fun provideEpisodeRepository(
        episodeRemoteDataSource: EpisodeRemoteDataSource,
    ): EpisodeRepository {
        return EpisodeRepositoryImpl(
            episodeRemoteDataSource = episodeRemoteDataSource,
        )
    }

    @Provides
    @Singleton
    fun provideFeedRepository(
        feedRemoteDataSource: FeedRemoteDataSource,
    ): FeedRepository {
        return FeedRepositoryImpl(
            feedRemoteDataSource = feedRemoteDataSource,
        )
    }
}