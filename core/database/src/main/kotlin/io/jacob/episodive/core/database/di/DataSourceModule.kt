package io.jacob.episodive.core.database.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.jacob.episodive.core.database.EpisodiveDatabase
import io.jacob.episodive.core.database.datasource.PodcastLocalDataSourceImpl
import io.jacob.episodive.core.database.dao.EpisodeDao
import io.jacob.episodive.core.database.dao.LikedEpisodeDao
import io.jacob.episodive.core.database.dao.PodcastDao
import io.jacob.episodive.core.database.datasource.EpisodeLocalDataSource
import io.jacob.episodive.core.database.datasource.EpisodeLocalDataSourceImpl
import io.jacob.episodive.core.database.datasource.LikedEpisodeLocalDataSource
import io.jacob.episodive.core.database.datasource.LikedEpisodeLocalDataSourceImpl
import io.jacob.episodive.core.database.datasource.PodcastLocalDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {
    @Provides
    @Singleton
    fun providePodcastLocalDataSource(
        podcastDao: PodcastDao,
    ): PodcastLocalDataSource {
        return PodcastLocalDataSourceImpl(
            podcastDao = podcastDao,
        )
    }

    @Provides
    @Singleton
    fun provideEpisodeLocalDataSource(
        episodeDao: EpisodeDao,
    ): EpisodeLocalDataSource {
        return EpisodeLocalDataSourceImpl(
            episodeDao = episodeDao,
        )
    }

    @Provides
    @Singleton
    fun provideLikedEpisodeLocalDataSource(
        database: EpisodiveDatabase,
        likedEpisodeDao: LikedEpisodeDao,
    ): LikedEpisodeLocalDataSource {
        return LikedEpisodeLocalDataSourceImpl(
            database = database,
            likedEpisodeDao = likedEpisodeDao,
        )
    }
}