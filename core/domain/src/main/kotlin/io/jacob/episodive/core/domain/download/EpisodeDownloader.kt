package io.jacob.episodive.core.domain.download

import io.jacob.episodive.core.model.DownloadProgress
import io.jacob.episodive.core.model.Episode
import kotlinx.coroutines.flow.Flow

interface EpisodeDownloader {
    fun downloadEpisode(episode: Episode, filePath: String): Long
    fun cancelDownload(downloadId: Long)

    /** 해당 에피소드로 매핑된 진행 중 다운로드를 모두 취소한다(언세이브 시 사용). */
    fun cancelDownloadForEpisode(episodeId: Long)
    fun deleteDownloadedFile(filePath: String)
    fun getDownloadDirectory(): String

    /**
     * 진행 중(대기/실행/일시정지) 및 실패한 다운로드를 episodeId -> 진행 상태로 방출한다.
     * 시스템 DownloadManager를 진실의 원천으로 삼아, 활성 다운로드가 있는 동안에만 폴링하고
     * 다운로드 enqueue/취소 시점에 즉시 재조회한다(idle 구간에는 폴링하지 않는다).
     */
    fun observeActiveDownloads(): Flow<Map<Long, DownloadProgress>>

    /** filePath에 실제 파일이 존재하는지로 다운로드 완료 여부를 판정한다. */
    fun isFileDownloaded(filePath: String): Boolean
}
