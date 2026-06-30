package io.jacob.episodive.core.domain.usecase.episode

import io.jacob.episodive.core.domain.download.EpisodeDownloader
import io.jacob.episodive.core.domain.repository.EpisodeRepository
import io.jacob.episodive.core.model.Episode
import javax.inject.Inject

class SaveEpisodeUseCase @Inject constructor(
    private val episodeRepository: EpisodeRepository,
    private val episodeDownloader: EpisodeDownloader,
) {
    suspend operator fun invoke(episode: Episode): Boolean {
        episodeRepository.upsertEpisode(episode)
        val isSavedNow = episodeRepository.toggleSavedEpisode(episode)

        if (isSavedNow) {
            val ext = episode.enclosureType.substringAfterLast("/", "mp3")
            val filePath = "${episode.feedId}/${episode.id}.$ext"
            episodeDownloader.downloadEpisode(episode, filePath)
        } else {
            // 진행 중 다운로드가 있으면 먼저 취소해 작업이 파일을 다시 만들지 않게 한 뒤 삭제한다.
            episodeDownloader.cancelDownloadForEpisode(episode.id)
            episode.filePath?.let { episodeDownloader.deleteDownloadedFile(it) }
        }

        return isSavedNow
    }
}
