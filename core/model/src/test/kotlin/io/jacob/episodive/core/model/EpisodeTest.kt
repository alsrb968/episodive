package io.jacob.episodive.core.model

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * `:core:testing` 의 팩토리를 쓰지 않고 여기서 직접 만든다 — 그 모듈이 `:core:model` 에
 * 의존하므로 반대로 끌어오면 순환이 된다.
 */
class EpisodeTest {

    private fun episode(
        duration: Duration? = null,
        clipStartTime: Instant? = null,
        clipDuration: Duration? = null,
    ) = Episode(
        id = 1L,
        title = "제목",
        link = "https://example.com",
        guid = "guid",
        datePublished = Instant.fromEpochSeconds(1_758_000_000L),
        dateCrawled = Instant.fromEpochSeconds(1_758_000_000L),
        enclosureUrl = "https://example.com/audio.mp3",
        enclosureType = "audio/mpeg",
        enclosureLength = 1_000L,
        duration = duration,
        explicit = false,
        image = "https://example.com/image.png",
        feedImage = "https://example.com/feed.png",
        feedId = 10L,
        feedLanguage = "ko",
        clipStartTime = clipStartTime,
        clipDuration = clipDuration,
    )

    @Test
    fun `clipPlaybackDuration is the clip duration when clip metadata exists`() {
        // Given: 1시간짜리 에피소드에서 30초만 잘라낸 클립
        val clip = episode(
            duration = 60.minutes,
            clipStartTime = Instant.fromEpochSeconds(100),
            clipDuration = 30.seconds,
        )

        // Then: 플레이어가 잘라 올리는 길이와 같아야 한다
        assertEquals(30.seconds, clip.clipPlaybackDuration)
    }

    @Test
    fun `clipPlaybackDuration falls back to the episode duration when clip metadata is absent`() {
        // 클립 정보가 없으면 플레이어도 잘라 올리지 않으므로 전체 길이가 실제 재생 길이다.
        val whole = episode(duration = 60.minutes)

        assertEquals(60.minutes, whole.clipPlaybackDuration)
    }

    @Test
    fun `clipPlaybackDuration falls back to the episode duration when only the start time is set`() {
        // hasClip 은 시작 시각과 길이가 모두 있어야 참이다. 하나만 있으면 클리핑을 걸지 않는다.
        val halfClip = episode(
            duration = 60.minutes,
            clipStartTime = Instant.fromEpochSeconds(100),
        )

        assertEquals(false, halfClip.hasClip)
        assertEquals(60.minutes, halfClip.clipPlaybackDuration)
    }

    // --- 클립이 에피소드 끝을 넘길 때 ---
    //
    // 사운드바이트의 시작+길이는 피드가 준 값이라 실제 오디오를 넘길 수 있다. media3 는 실제
    // 미디어 끝에서 자르므로, 표시가 그것을 모르면 준비가 끝나는 순간 숫자가 한 번 줄어든다.

    @Test
    fun `clipPlaybackDuration is capped by what is left of the episode`() {
        // 60분짜리에서 59분 30초부터 60초를 잘라 달라고 하면 실제로는 30초만 남아 있다.
        val overrunning = episode(
            duration = 60.minutes,
            clipStartTime = Instant.fromEpochSeconds(59 * 60 + 30),
            clipDuration = 60.seconds,
        )

        assertEquals(30.seconds, overrunning.clipPlaybackDuration)
    }

    @Test
    fun `clipPlaybackDuration is untouched when the clip fits inside the episode`() {
        // 상한이 정상 클립까지 깎으면 안 된다.
        val fits = episode(
            duration = 60.minutes,
            clipStartTime = Instant.fromEpochSeconds(100),
            clipDuration = 30.seconds,
        )

        assertEquals(30.seconds, fits.clipPlaybackDuration)
    }

    @Test
    fun `clipPlaybackDuration keeps the clip length when the episode length is unknown`() {
        // 상한을 걸 근거가 없으면 피드가 준 클립 길이를 그대로 믿는다.
        val unknownEpisodeLength = episode(
            clipStartTime = Instant.fromEpochSeconds(100),
            clipDuration = 30.seconds,
        )

        assertEquals(30.seconds, unknownEpisodeLength.clipPlaybackDuration)
    }

    @Test
    fun `clipPlaybackDuration keeps the clip length when the clip starts past the episode end`() {
        // 시작점부터 에피소드 밖이면 남은 길이가 음수다. 그 값을 표시할 수는 없으니 클립
        // 길이를 그대로 둔다 — 이 경우 media3 는 IllegalClippingException 으로 재생 자체를
        // 거부하므로, 표시 길이로 메울 수 있는 문제가 아니다.
        val startsPastEnd = episode(
            duration = 10.seconds,
            clipStartTime = Instant.fromEpochSeconds(60),
            clipDuration = 30.seconds,
        )

        assertEquals(30.seconds, startsPastEnd.clipPlaybackDuration)
    }

    @Test
    fun `clipPlaybackDuration is zero when neither duration is known`() {
        // 길이를 알 수 없을 때 null 을 흘려보내면 화면이 빈 시간을 그리게 되므로 0 으로 굳힌다.
        val unknown = episode()

        assertEquals(Duration.ZERO, unknown.clipPlaybackDuration)
    }
}
