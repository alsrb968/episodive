package io.jacob.episodive.core.player.di

import androidx.media3.exoplayer.ExoPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.jacob.episodive.core.common.EpisodivePlayers
import io.jacob.episodive.core.common.Player
import io.jacob.episodive.core.domain.download.EpisodeDownloader
import io.jacob.episodive.core.player.audio.PlaybackSpectrumMonitor
import io.jacob.episodive.core.player.datasource.PlayerDataSource
import io.jacob.episodive.core.player.datasource.PlayerDataSourceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {
    @Provides
    @Singleton
    @Player(EpisodivePlayers.Main)
    fun provideMainPlayerDataSource(
        @Player(EpisodivePlayers.Main) exoPlayer: ExoPlayer,
        episodeDownloader: EpisodeDownloader,
    ): PlayerDataSource {
        return PlayerDataSourceImpl(
            player = exoPlayer,
            episodeDownloader = episodeDownloader,
        )
    }

    @Provides
    @Singleton
    @Player(EpisodivePlayers.Clip)
    fun provideClipPlayerDataSource(
        @Player(EpisodivePlayers.Clip) exoPlayer: ExoPlayer,
        episodeDownloader: EpisodeDownloader,
        // PlayerModule 이 같은 인스턴스를 클립 플레이어의 오디오 체인에 끼워 둔다.
        spectrumMonitor: PlaybackSpectrumMonitor,
    ): PlayerDataSource {
        return PlayerDataSourceImpl(
            player = exoPlayer,
            episodeDownloader = episodeDownloader,
            spectrumMonitor = spectrumMonitor,
        )
    }
}