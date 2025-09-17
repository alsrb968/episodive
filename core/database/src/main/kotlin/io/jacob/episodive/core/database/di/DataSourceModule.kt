package io.jacob.episodive.core.database.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.jacob.episodive.core.database.EpisodiveDatabase
import io.jacob.episodive.core.database.dao.EpisodeDao
import io.jacob.episodive.core.database.dao.PodcastDao
import io.jacob.episodive.core.database.datasource.EpisodeLocalDataSource
import io.jacob.episodive.core.database.datasource.EpisodeLocalDataSourceImpl
import io.jacob.episodive.core.database.datasource.PodcastLocalDataSource
import io.jacob.episodive.core.database.datasource.PodcastLocalDataSourceImpl
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
        database: EpisodiveDatabase,
        episodeDao: EpisodeDao,
    ): EpisodeLocalDataSource {
        return EpisodeLocalDataSourceImpl(
            database = database,
            episodeDao = episodeDao,
        )
    }
}