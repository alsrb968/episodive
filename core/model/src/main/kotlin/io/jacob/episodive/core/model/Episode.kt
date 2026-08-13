package io.jacob.episodive.core.model

import io.jacob.episodive.core.model.mapper.toIntSeconds
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
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
     * 재생할 때 실제로 흐르는 길이. [clipped] 는 "잘라 올렸는가" 다.
     *
     * **어느 길이를 고르는지는 여기 한 곳에서만 정한다.** 화면(아직 올리기 전)과 플레이어
     * (이미 올린 뒤)가 각자 셈하면 둘이 어긋나는 순간 숫자가 튀는데, 그게 이 규약이 막으려는
     * 것이다. 부르는 쪽은 [clipped] 를 판정해 넘기기만 한다 — 플레이어는 실제 미디어 아이템에
     * 클리핑이 걸렸는지로, 화면은 [clipPlaybackDuration] 을 통해 [hasClip] 으로.
     */
    fun playbackDuration(clipped: Boolean): Duration {
        if (!clipped) return duration ?: Duration.ZERO
        val clip = clipDuration ?: return duration ?: Duration.ZERO

        // 사운드바이트의 시작+길이는 피드가 준 값이라 에피소드 끝을 넘길 때가 있다. 그러면
        // media3 는 실제 미디어 끝에서 자르므로(ClippingMediaSource) 준비가 끝나는 순간
        // player.duration 이 더 작은 값을 준다 — 표시가 그것을 모르면 숫자가 한 번 줄어들고,
        // 그게 이 규약이 없애려는 바로 그 튐이다. 에피소드 길이를 아는 경우 미리 맞춘다.
        val room = duration?.minus(clipStartPositionMs.milliseconds)
        return if (room != null && room.isPositive()) minOf(clip, room) else clip
    }

    /**
     * 클립으로 재생할 때 흐르는 길이 — 아직 플레이어에 올리기 전인 화면이 쓴다.
     *
     * 플레이어는 [hasClip] 일 때만 잘라 올리므로 그 판정을 그대로 따른다. 이 기준이
     * `toMediaItem` 의 클리핑 조건과 어긋나면 카드의 시간과 플레이어가 발행하는 길이가
     * 갈라져, 재생이 준비되는 순간 숫자가 튄다.
     */
    val clipPlaybackDuration: Duration get() = playbackDuration(clipped = hasClip)
}
