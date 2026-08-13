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
     * 재생할 때 실제로 흐르는 길이. [clipped] 는 "잘라 올렸는가" 다.
     *
     * **어느 길이를 고르는지는 여기 한 곳에서만 정한다.** 화면(아직 올리기 전)과 플레이어
     * (이미 올린 뒤)가 각자 셈하면 둘이 어긋나는 순간 숫자가 튀는데, 그게 이 규약이 막으려는
     * 것이다. 부르는 쪽은 [clipped] 를 판정해 넘기기만 한다 — 플레이어는 실제 미디어 아이템에
     * 클리핑이 걸렸는지로, 화면은 [clipPlaybackDuration] 을 통해 [hasClip] 으로.
     */
    fun playbackDuration(clipped: Boolean): Duration {
        if (!clipped) return duration ?: Duration.ZERO

        // 길이가 0 이하인 사운드바이트는 피드 오류다. 그대로 쓰면 클립이 올라가자마자 끝나
        // ENDED 자동 넘김이 연쇄해, 목록을 소리 없이 훑고 지나간다.
        //
        // 반대로 클립이 에피소드 끝을 넘기는 경우는 여기서 손대지 않는다. media3 는 실제
        // 미디어 길이로 자르는데 여기서 쓸 수 있는 것은 피드가 말한 duration 뿐이고, 그 둘은
        // 자주 어긋난다. 피드가 실제보다 짧게 말하면(흔하다) 상한이 실제보다 더 깎아, 준비가
        // 끝나는 순간 표시가 도로 늘어난다 — 줄어드는 튐을 늘어나는 튐으로 바꿀 뿐이다.
        return clipDuration?.takeIf { it.isPositive() } ?: duration ?: Duration.ZERO
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
