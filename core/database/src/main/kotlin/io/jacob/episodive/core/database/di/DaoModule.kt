package io.jacob.episodive.core.database.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.jacob.episodive.core.database.PodcastIndexDatabase
import io.jacob.episodive.core.database.dao.EpisodeDao
import io.jacob.episodive.core.database.dao.PodcastDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DaoModule {
    @Provides
    @Singleton
    fun providePodcastDao(
        database: PodcastIndexDatabase
    ): PodcastDao {
        return database.podcastDao()
    }

    @Provides
    @Singleton
    fun provideEpisodeDao(
        database: PodcastIndexDatabase
    ): EpisodeDao {
        return database.episodeDao()
    }
}