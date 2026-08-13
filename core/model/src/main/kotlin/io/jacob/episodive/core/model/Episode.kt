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
    /**
     * 클립으로 재생할 수 있는가. 플레이어는 이 값이 참일 때만 미디어 아이템을 잘라 올린다.
     *
     * 피드가 주는 값을 그대로 믿지 않고 두 가지를 함께 본다. 표시 길이만 손보는 것으로는
     * 막을 수 없다 — 잘라 올리는 판단 자체가 여기서 갈리기 때문이다.
     *
     * - 길이가 0 이하면 시작=끝인 창이 올라가 재생하자마자 ENDED 가 되고, 그 ENDED 가 다음
     *   페이지로 넘기는 것을 연쇄시켜 목록을 소리 없이 훑고 지나간다.
     * - 시작이 음수면 `ClippingConfiguration.Builder.setStartPositionMs` 가 그 자리에서
     *   IllegalArgumentException 을 던진다(media3 가 `>= 0` 을 단언한다). 즉 크래시다.
     * - 시작이 터무니없이 크면 `시작 + 길이` 가 Long 을 넘겨 끝이 음수가 되고, 이번엔
     *   `setEndPositionMs` 가 같은 이유로 던진다. 끝이 시작보다 **뒤** 여야 한다.
     *
     * 마지막 조건이 `>` 인 것은 넘침만 잡으려는 것이 아니다. 밀리초로 내리면 0 이 되는 길이
     * (예: 500µs)가 `isPositive()` 를 통과해 시작=끝인 창을 만들 수 있는데, 그것이 바로 위의
     * 첫 항목이 막으려는 상태다.
     *
     * 시작이 에피소드 끝을 넘는 경우도 막는다. media3 는 창의 끝을 실제 미디어 길이로 자르는데
     * 시작이 그보다 뒤면 시작 > 끝이 되어 `IllegalClippingException(REASON_START_EXCEEDS_END)`
     * 로 재생이 실패한다. 이때는 [duration] 을 근거로 삼는다 — 피드가 준 길이라 정확하지 않을
     * 수 있지만, 시작이 그 길이를 통째로 넘는 것은 "조금 어긋남" 이 아니라 명백히 잘못된
     * 데이터다. (그래서 [clipPlaybackDuration] 이 상한을 두지 않는 것과 판단이 다르다.)
     */
    val hasClip: Boolean = clipStartTime != null &&
            clipDuration != null &&
            clipDuration.isPositive() &&
            clipStartPositionMs >= 0L &&
            clipEndPositionMs > clipStartPositionMs &&
            (duration == null || clipStartPositionMs < duration.inWholeMilliseconds)

    /**
     * 클립으로 재생할 때 흐르는 길이 — **아직 플레이어에 올리기 전인 화면**이 쓴다.
     *
     * 플레이어는 [hasClip] 일 때만 잘라 올리므로 그 판정을 그대로 따른다. 이 기준이
     * `toMediaItem` 의 클리핑 조건과 어긋나면 카드의 시간과 플레이어가 발행하는 길이가
     * 갈라져, 재생이 준비되는 순간 숫자가 튄다.
     *
     * 올린 **뒤**의 길이는 이 값을 쓰지 않는다. 그쪽은 실제로 걸린 클리핑 창을 직접 재므로
     * (`PlayerDataSourceImpl.playbackDuration`) 추정이 필요 없다. 두 자리가 답하는 질문이
     * 다르다 — 여기는 "올리면 얼마가 될까", 저기는 "올라간 것이 얼마인가".
     *
     * 클립이 에피소드 끝을 넘기는 경우는 손대지 않는다. media3 는 실제 미디어 길이로 자르는데
     * 여기서 쓸 수 있는 것은 피드가 말한 [duration] 뿐이고, 그 둘은 자주 어긋난다. 피드가
     * 실제보다 짧게 말하면(흔하다) 상한이 실제보다 더 깎아, 준비가 끝나는 순간 표시가 도로
     * 늘어난다 — 줄어드는 튐을 늘어나는 튐으로 바꿀 뿐이다.
     */
    val clipPlaybackDuration: Duration
        get() = (if (hasClip) clipDuration else duration) ?: Duration.ZERO
}
