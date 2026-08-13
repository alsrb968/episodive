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

    // --- 피드가 준 클립 길이를 어디까지 믿는가 ---

    @Test
    fun `clipPlaybackDuration falls back to the episode duration when the clip length is zero`() {
        // 길이 0 짜리 사운드바이트를 그대로 쓰면 클립이 올라가자마자 끝나고, 그 ENDED 가
        // 다음 페이지로 넘기는 것을 연쇄시켜 목록을 소리 없이 훑고 지나간다.
        val zeroLength = episode(
            duration = 60.minutes,
            clipStartTime = Instant.fromEpochSeconds(100),
            clipDuration = Duration.ZERO,
        )

        assertEquals(60.minutes, zeroLength.clipPlaybackDuration)
    }

    @Test
    fun `clipPlaybackDuration keeps a clip that overruns the episode end`() {
        // 클립이 에피소드 끝을 넘겨도 손대지 않는다. media3 는 실제 미디어 길이로 자르는데
        // 여기서 쓸 수 있는 것은 피드가 말한 duration 뿐이고, 그 둘이 어긋나면(피드가 실제보다
        // 짧게 말하는 일이 흔하다) 상한이 실제보다 더 깎아 표시가 도로 늘어난다.
        val overrunning = episode(
            duration = 60.minutes,
            clipStartTime = Instant.fromEpochSeconds(59 * 60 + 30),
            clipDuration = 60.seconds,
        )

        assertEquals(60.seconds, overrunning.clipPlaybackDuration)
    }

    @Test
    fun `clipPlaybackDuration is zero when neither duration is known`() {
        // 길이를 알 수 없을 때 null 을 흘려보내면 화면이 빈 시간을 그리게 되므로 0 으로 굳힌다.
        val unknown = episode()

        assertEquals(Duration.ZERO, unknown.clipPlaybackDuration)
    }
}
