package io.jacob.episodive.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.jacob.episodive.core.network.api.PodcastIndexApi
import io.jacob.episodive.core.network.PodcastIndexClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Provides
    @Singleton
    fun providePodcastIndexApi(): PodcastIndexApi {
        return PodcastIndexClient.retrofit.create(PodcastIndexApi::class.java)
    }
}