package io.jacob.episodive.core.model

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * `:core:testing` 의 팩토리를 쓰지 않고 여기서 직접 만든다.
 *
 * 의존 순환 때문이 아니다 — `testImplementation` 방향은 태스크 그래프상 순환이 아니다.
 * 막는 것은 산출물 형식이다: `:core:testing` 은 `episodive.android.library` 라 AAR 을 내놓고,
 * 이 모듈은 `episodive.jvm.library` 라 그것을 소비할 수 없다. 팩토리를 함께 쓰려면 순수 JVM
 * 모듈로 떼어내야 한다.
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
    fun `a soundbite starting before zero is not treated as a clip at all`() {
        // 시작이 음수면 media3 의 setStartPositionMs 가 그 자리에서 IllegalArgumentException
        // 을 던진다. 표시 길이의 문제가 아니라 크래시이므로, 잘라 올릴지 자체를 막아야 한다.
        val negativeStart = episode(
            duration = 60.minutes,
            clipStartTime = Instant.fromEpochSeconds(-5),
            clipDuration = 30.seconds,
        )

        assertEquals(false, negativeStart.hasClip)
    }

    @Test
    fun `a zero length soundbite is not treated as a clip at all`() {
        // 표시 길이만 되돌리는 것으로는 모자라다. hasClip 이 참으로 남으면 플레이어가 시작=끝인
        // 창을 그대로 올려, 재생하자마자 ENDED 가 되고 그것이 다음 페이지로 넘기는 것을
        // 연쇄시켜 목록을 소리 없이 훑고 지나간다. 잘라 올릴지 자체를 여기서 막아야 한다.
        val zeroLength = episode(
            duration = 60.minutes,
            clipStartTime = Instant.fromEpochSeconds(100),
            clipDuration = Duration.ZERO,
        )

        assertEquals(false, zeroLength.hasClip)
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
    fun `a soundbite whose end overflows is not treated as a clip at all`() {
        // 시작이 터무니없이 크면 `시작 + 길이` 가 Long 을 넘겨 끝이 음수가 된다. 그대로
        // 올라가면 크래시다 — 다만 던지는 것은 `setStartPositionMs` 다. msToUs 가 포화 없이
        // 1000 을 곱해 거대한 시작이 마이크로초로 내려가며 음수로 감기기 때문이고, 정작
        // 음수가 된 끝은 다시 감겨 양수가 되어 통과한다(media3 1.8.0 실행 확인).
        // 세 조항 중 이것만 테스트가 없었다.
        val overflowing = episode(
            duration = 60.minutes,
            clipStartTime = Instant.fromEpochMilliseconds(Long.MAX_VALUE),
            clipDuration = 30.seconds,
        )

        assertEquals(false, overflowing.hasClip)
    }

    @Test
    fun `a clip starting past the feed reported duration is still a clip`() {
        // 피드가 실제보다 짧게 말하는 일은 흔하다. 실제 60분짜리를 30분이라 말하고 사운드바이트
        // 는 40분 지점에서 시작하는 경우 — 여기서 hasClip 을 내리면 클립 탭이 에피소드 전체를
        // 처음부터 튼다. 잘라 올릴 수 없는 데이터인지 아닌지는 피드가 말한 길이로 점칠 수 없다.
        val startsPastFeedDuration = episode(
            duration = 30.minutes,
            clipStartTime = Instant.fromEpochSeconds(40 * 60),
            clipDuration = 30.seconds,
        )

        assertEquals(true, startsPastFeedDuration.hasClip)
        assertEquals(30.seconds, startsPastFeedDuration.clipPlaybackDuration)
    }

    @Test
    fun `clipPlaybackDuration is zero when neither duration is known`() {
        // 길이를 알 수 없을 때 null 을 흘려보내면 화면이 빈 시간을 그리게 되므로 0 으로 굳힌다.
        val unknown = episode()

        assertEquals(Duration.ZERO, unknown.clipPlaybackDuration)
    }
}
