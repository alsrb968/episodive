package io.jacob.episodive.core.database.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.jacob.episodive.core.database.DatabaseDataSource
import io.jacob.episodive.core.database.DatabaseDataSourceImpl
import io.jacob.episodive.core.database.dao.EpisodeDao
import io.jacob.episodive.core.database.dao.PodcastDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {
    @Provides
    @Singleton
    fun provideDatabaseDataSource(
        podcastDao: PodcastDao,
        episodeDao: EpisodeDao
    ): DatabaseDataSource {
        return DatabaseDataSourceImpl(
            podcastDao = podcastDao,
            episodeDao = episodeDao,
        )
    }
}