package io.jacob.episodive.core.domain.usecase.player

import io.jacob.episodive.core.common.Dispatcher
import io.jacob.episodive.core.common.EpisodiveDispatchers
import io.jacob.episodive.core.common.EpisodivePlayers
import io.jacob.episodive.core.common.Player
import io.jacob.episodive.core.domain.download.EpisodeDownloader
import io.jacob.episodive.core.domain.repository.EpisodeRepository
import io.jacob.episodive.core.domain.repository.PlayerRepository
import io.jacob.episodive.core.model.DownloadProgress
import io.jacob.episodive.core.model.DownloadStatus
import io.jacob.episodive.core.model.Episode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetNowPlayingUseCase @Inject constructor(
    @param:Player(EpisodivePlayers.Main) private val playerRepository: PlayerRepository,
    private val episodeRepository: EpisodeRepository,
    private val episodeDownloader: EpisodeDownloader,
    @param:Dispatcher(EpisodiveDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(): Flow<Episode?> {
        return playerRepository.nowPlaying.flatMapLatest { episode ->
            if (episode == null) {
                flowOf(null)
            } else {
                combine(
                    episodeRepository.getEpisodeById(episode.id),
                    // 전역 활성-다운로드 맵에서 현재 에피소드 항목만 추려 구독한다.
                    // 다른 에피소드의 진행 변화로 불필요하게 재계산/파일 IO 하지 않도록 distinct 적용.
                    episodeDownloader.observeActiveDownloads()
                        .map { it[episode.id] }
                        .distinctUntilChanged(),
                ) { current, activeProgress ->
                    current?.withDownloadState(activeProgress)
                }
            }
        }.flowOn(ioDispatcher)
    }

    /**
     * 다운로드 상태의 진실의 원천:
     * - DownloadManager가 진행/일시정지/실패를 보고하면 그 상태를 그대로 반영(실패는 더 이상 은폐되지 않음)
     * - 그 외에는 실제 파일 존재 여부로 완료 판정 (broadcast/DB에 의존하지 않음)
     */
    private fun Episode.withDownloadState(active: DownloadProgress?): Episode = when {
        active != null -> copy(
            downloadStatus = active.status,
            downloadProgress = active.progress,
        )

        filePath?.let { episodeDownloader.isFileDownloaded(it) } == true -> copy(
            downloadStatus = DownloadStatus.COMPLETED,
            downloadProgress = 1f,
        )

        // 저장 직후 DownloadManager가 아직 잡기 전(enqueue 직후)의 짧은 구간만 커버한다.
        // 실패/취소는 위 active 분기(FAILED)나 파일 부재로 처리되므로 여기서 은폐되지 않는다.
        // 이 폴백이 없으면 isSaved=true만 반영되어 완료 아이콘이 잠깐 깜빡인다.
        isSaved -> copy(
            downloadStatus = DownloadStatus.PENDING,
            downloadProgress = 0f,
        )

        else -> copy(
            downloadStatus = null,
            downloadProgress = 0f,
        )
    }
}
