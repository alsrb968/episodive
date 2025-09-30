package io.jacob.episodive.core.player.di

import androidx.media3.exoplayer.ExoPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.jacob.episodive.core.player.datasource.PlayerDataSource
import io.jacob.episodive.core.player.datasource.PlayerDataSourceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {
    @Provides
    @Singleton
    fun providePlayerDataSource(
        exoPlayer: ExoPlayer,
    ): PlayerDataSource {
        return PlayerDataSourceImpl(
            player = exoPlayer,
        )
    }
}