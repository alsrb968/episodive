package io.jacob.episodive.core.data.widget

import io.jacob.episodive.core.common.EpisodivePlayers
import io.jacob.episodive.core.common.Player
import io.jacob.episodive.core.domain.repository.PlayerRepository
import io.jacob.episodive.core.domain.usecase.player.GetNowPlayingUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetPodcastUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetUserRecentPodcastsUseCase
import io.jacob.episodive.core.domain.widget.NowPlayingSnapshot
import io.jacob.episodive.core.domain.widget.PodcastSnapshot
import io.jacob.episodive.core.domain.widget.WidgetDataReader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetDataReaderImpl @Inject constructor(
    private val getNowPlaying: GetNowPlayingUseCase,
    private val getPodcast: GetPodcastUseCase,
    private val getUserRecentPodcasts: GetUserRecentPodcastsUseCase,
    @param:Player(EpisodivePlayers.Main) private val playerRepository: PlayerRepository,
) : WidgetDataReader {

    override suspend fun snapshotNowPlaying(): NowPlayingSnapshot? {
        val episode = getNowPlaying().first() ?: return null
        val isPlaying = playerRepository.isPlaying.first()
        // Episode.feedTitle 이 비어있으면 팟캐스트를 따로 조회해 title 로 대체.
        val podcastName = episode.feedTitle?.takeIf { it.isNotBlank() }
            ?: getPodcast(episode.feedId).first()?.title
        return NowPlayingSnapshot(
            episodeId = episode.id,
            podcastId = episode.feedId,
            title = episode.title,
            feedTitle = podcastName,
            // 에피소드 자체 image 가 비어있으면 feed(팟캐스트) 이미지로 fallback.
            imageUrl = episode.image.ifBlank { episode.feedImage }.ifBlank { null },
            isPlaying = isPlaying,
        )
    }

    override fun nowPlayingFlow(): Flow<NowPlayingSnapshot?> =
        getNowPlaying().flatMapLatest { episode ->
            if (episode == null) {
                flowOf(null)
            } else {
                // 에피소드별로 팟캐스트도 함께 구독해 feedTitle 이 비면 podcast.title 로 대체.
                combine(getPodcast(episode.feedId), playerRepository.isPlaying) { podcast, isPlaying ->
                    NowPlayingSnapshot(
                        episodeId = episode.id,
                        podcastId = episode.feedId,
                        title = episode.title,
                        feedTitle = episode.feedTitle?.takeIf { it.isNotBlank() } ?: podcast?.title,
                        imageUrl = episode.image.ifBlank { episode.feedImage }.ifBlank { null },
                        isPlaying = isPlaying,
                    )
                }
            }
        }.distinctUntilChanged { old, new ->
            // 재생 위치(progress) 재방출 폭주 차단: 에피소드/재생상태/팟캐스트명이 같으면 무시.
            // feedTitle 은 팟캐스트 비동기 로드로 뒤늦게 채워질 수 있으므로 비교에 포함한다.
            old?.episodeId == new?.episodeId &&
                old?.isPlaying == new?.isPlaying &&
                old?.feedTitle == new?.feedTitle
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
