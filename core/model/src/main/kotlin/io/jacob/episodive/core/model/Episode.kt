package io.jacob.episodive.core.model

import io.jacob.episodive.core.model.mapper.toIntSeconds
import kotlin.time.Duration
import kotlin.time.Instant

data class Episode(
    val id: Long,
    val title: String,
    val link: String,
    val description: String? = null,
    val guid: String,
    val datePublished: Instant,
    val dateCrawled: Instant,
    val enclosureUrl: String,
    val enclosureType: String,
    val enclosureLength: Long, // in bytes, 0 is live
    val startTime: Instant? = null, // for live episodes
    val endTime: Instant? = null, // for live episodes
    val status: String? = null, // for live episodes
    val contentLink: String? = null, // for live episodes
    val duration: Duration? = null,
    val explicit: Boolean,
    val episode: Int? = null,
    val episodeType: EpisodeType? = null,
    val season: Int? = null,
    val image: String,
    val feedItunesId: Long? = null,
    val feedImage: String,
    val feedId: Long,
    val feedUrl: String? = null,
    val feedAuthor: String? = null,
    val feedTitle: String? = null,
    val feedLanguage: String,
    val categories: List<Category> = emptyList(),
    val chaptersUrl: String? = null,
    val transcriptUrl: String? = null,
    val likedAt: Instant? = null,
    val playedAt: Instant? = null,
    val position: Duration = Duration.ZERO,
    val isCompleted: Boolean = false,
    val clipStartTime: Instant? = null, // for soundbite
    val clipDuration: Duration? = null, // for soundbite
    val savedAt: Instant? = null,
    val filePath: String? = null,
    val downloadStatus: DownloadStatus? = null,
    val downloadProgress: Float = 0f,
) {
    val isLive: Boolean
        get() = enclosureLength == 0L ||
                startTime != null ||
                endTime != null ||
                status != null ||
                contentLink != null

    val isLiked: Boolean = likedAt != null

    val isSaved: Boolean = savedAt != null

    val isDownloading: Boolean
        get() = downloadStatus == DownloadStatus.PENDING ||
                downloadStatus == DownloadStatus.DOWNLOADING ||
                downloadStatus == DownloadStatus.PAUSED

    val isDownloaded: Boolean = downloadStatus == DownloadStatus.COMPLETED

    val progress: Float = duration?.let {
        if (it == Duration.ZERO) 0f
        else position.toIntSeconds().toFloat() / it.toIntSeconds()
    } ?: 0f

    val remain: Duration? = duration?.let {
        if (it == Duration.ZERO) null
        else it - position
    }

    val clipStartPositionMs: Long = clipStartTime?.toEpochMilliseconds() ?: 0L
    val clipEndPositionMs: Long = clipStartPositionMs + (clipDuration?.inWholeMilliseconds ?: 0L)
    val hasClip: Boolean = clipStartTime != null && clipDuration != null

    /**
     * 클립으로 재생할 때 실제로 흐르는 길이.
     *
     * 플레이어는 [hasClip] 일 때만 미디어 아이템을 잘라 올리므로 그 판정과 같은 기준을 쓴다.
     * 클립 화면의 남은 시간과 플레이어가 발행하는 길이가 이 값 하나로 맞춰져야, 재생이
     * 준비되는 순간 화면의 숫자가 에피소드 전체 길이에서 클립 길이로 튀지 않는다.
     */
    val clipPlaybackDuration: Duration =
        (if (hasClip) clipDuration else duration) ?: Duration.ZERO
}
