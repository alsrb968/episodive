package io.jacob.episodive.core.database.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.jacob.episodive.core.database.PodcastIndexDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DaoModule {
    @Provides
    @Singleton
    fun providePodcastDao(database: PodcastIndexDatabase) = database.podcastDao()

    @Provides
    @Singleton
    fun provideEpisodeDao(database: PodcastIndexDatabase) = database.episodeDao()

    @Provides
    @Singleton
    fun provideLikedEpisodeDao(database: PodcastIndexDatabase) = database.likedEpisodeDao()
}