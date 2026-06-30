package io.jacob.episodive.core.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.jacob.episodive.core.common.Dispatcher
import io.jacob.episodive.core.common.EpisodiveDispatchers
import io.jacob.episodive.core.data.download.EpisodeDownloaderImpl
import io.jacob.episodive.core.domain.download.EpisodeDownloader
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DownloadModule {
    @Provides
    @Singleton
    fun provideEpisodeDownloaderImpl(
        @ApplicationContext context: Context,
        @Dispatcher(EpisodiveDispatchers.IO) ioDispatcher: CoroutineDispatcher,
    ): EpisodeDownloaderImpl {
        return EpisodeDownloaderImpl(context = context, ioDispatcher = ioDispatcher)
    }

    @Provides
    @Singleton
    fun provideEpisodeDownloader(
        impl: EpisodeDownloaderImpl,
    ): EpisodeDownloader {
        return impl
    }
}
