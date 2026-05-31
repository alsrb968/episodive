package io.jacob.episodive.core.data.widget

import io.jacob.episodive.core.common.EpisodivePlayers
import io.jacob.episodive.core.common.Player
import io.jacob.episodive.core.domain.repository.PlayerRepository
import io.jacob.episodive.core.domain.usecase.player.GetNowPlayingUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetUserRecentPodcastsUseCase
import io.jacob.episodive.core.domain.widget.NowPlayingSnapshot
import io.jacob.episodive.core.domain.widget.PodcastSnapshot
import io.jacob.episodive.core.domain.widget.WidgetDataReader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetDataReaderImpl @Inject constructor(
    private val getNowPlaying: GetNowPlayingUseCase,
    private val getUserRecentPodcasts: GetUserRecentPodcastsUseCase,
    @param:Player(EpisodivePlayers.Main) private val playerRepository: PlayerRepository,
) : WidgetDataReader {

    override suspend fun snapshotNowPlaying(): NowPlayingSnapshot? {
        val episode = getNowPlaying().first() ?: return null
        val isPlaying = playerRepository.isPlaying.first()
        return NowPlayingSnapshot(
            episodeId = episode.id,
            podcastId = episode.feedId,
            title = episode.title,
            feedTitle = episode.feedTitle,
            // 에피소드 자체 image 가 비어있으면 feed(팟캐스트) 이미지로 fallback.
            imageUrl = episode.image.ifBlank { episode.feedImage }.ifBlank { null },
            isPlaying = isPlaying,
        )
    }

    override fun nowPlayingFlow(): Flow<NowPlayingSnapshot?> =
        combine(getNowPlaying(), playerRepository.isPlaying) { episode, isPlaying ->
            episode?.let {
                NowPlayingSnapshot(
                    episodeId = it.id,
                    podcastId = it.feedId,
                    title = it.title,
                    feedTitle = it.feedTitle,
                    imageUrl = it.image.ifBlank { it.feedImage }.ifBlank { null },
                    isPlaying = isPlaying,
                )
            }
        }.distinctUntilChanged { old, new ->
            // 재생 위치(progress) 재방출 폭주 차단: 에피소드/재생상태가 같으면 무시.
            old?.episodeId == new?.episodeId && old?.isPlaying == new?.isPlaying
        }

    override fun userRecentPodcastsFlow(max: Int): Flow<List<PodcastSnapshot>> =
        getUserRecentPodcasts(max = max)
            .map { podcasts ->
                podcasts.map { podcast ->
                    PodcastSnapshot(
                        id = podcast.id,
                        title = podcast.title,
                        imageUrl = podcast.image.ifBlank { podcast.artwork }.ifBlank { null },
                    )
                }
            }
            .distinctUntilChanged()
}
