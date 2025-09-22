package io.jacob.episodive.core.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.jacob.episodive.core.data.repository.EpisodeRepositoryImpl
import io.jacob.episodive.core.data.repository.FeedRepositoryImpl
import io.jacob.episodive.core.data.repository.PodcastRepositoryImpl
import io.jacob.episodive.core.data.util.EpisodeRemoteUpdater
import io.jacob.episodive.core.data.util.PodcastRemoteUpdater
import io.jacob.episodive.core.database.datasource.EpisodeLocalDataSource
import io.jacob.episodive.core.database.datasource.PodcastLocalDataSource
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
        podcastLocalDataSource: PodcastLocalDataSource,
        podcastRemoteDataSource: PodcastRemoteDataSource,
        podcastRemoteUpdater: PodcastRemoteUpdater.Factory,
    ): PodcastRepository {
        return PodcastRepositoryImpl(
            localDataSource = podcastLocalDataSource,
            remoteDataSource = podcastRemoteDataSource,
            remoteUpdater = podcastRemoteUpdater,
        )
    }

    @Provides
    @Singleton
    fun provideEpisodeRepository(
        episodeLocalDataSource: EpisodeLocalDataSource,
        episodeRemoteDataSource: EpisodeRemoteDataSource,
        episodeRemoteUpdater: EpisodeRemoteUpdater.Factory,
    ): EpisodeRepository {
        return EpisodeRepositoryImpl(
            localDataSource = episodeLocalDataSource,
            remoteDataSource = episodeRemoteDataSource,
            remoteUpdater = episodeRemoteUpdater,
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