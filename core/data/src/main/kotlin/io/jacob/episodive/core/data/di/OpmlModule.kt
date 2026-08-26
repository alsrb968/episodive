package io.jacob.episodive.core.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.jacob.episodive.core.common.Dispatcher
import io.jacob.episodive.core.common.EpisodiveDispatchers
import io.jacob.episodive.core.data.opml.OpmlFileDataSourceImpl
import io.jacob.episodive.core.domain.datasource.OpmlFileDataSource
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OpmlModule {
    @Provides
    @Singleton
    fun provideOpmlFileDataSource(
        @ApplicationContext context: Context,
        @Dispatcher(EpisodiveDispatchers.IO) ioDispatcher: CoroutineDispatcher,
    ): OpmlFileDataSource {
        return OpmlFileDataSourceImpl(
            context = context,
            ioDispatcher = ioDispatcher,
        )
    }
}
