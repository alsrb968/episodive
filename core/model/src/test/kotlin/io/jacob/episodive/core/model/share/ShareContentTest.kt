package io.jacob.episodive.core.model.share

import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.Podcast
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
class ShareContentTest {

    private val labels = ShareLabels(
        episodeSubjectFormat = "%1\$s · %2\$s",
        clipLineFormat = "%1\$s 부터",
        positionLineFormat = "%1\$s 지점부터",
    )

    private fun episode(
        link: String = "https://example.com/ep1",
        feedUrl: String? = "https://example.com/rss.xml",
        feedTitle: String? = "팟캐스트",
        duration: Duration? = 60.minutes,
        clipStartTime: Instant? = null,
        clipDuration: Duration? = null,
    ) = Episode(
        id = 1L,
        title = "에피소드 제목",
        link = link,
        guid = "guid",
        datePublished = Instant.fromEpochSeconds(1_758_000_000L),
        dateCrawled = Instant.fromEpochSeconds(1_758_000_000L),
        enclosureUrl = "https://cdn.example.com/audio.mp3",
        enclosureType = "audio/mpeg",
        enclosureLength = 1_000L,
        duration = duration,
        explicit = false,
        image = "https://example.com/image.png",
        feedImage = "https://example.com/feed.png",
        feedId = 10L,
        feedUrl = feedUrl,
        feedTitle = feedTitle,
        feedLanguage = "ko",
        clipStartTime = clipStartTime,
        clipDuration = clipDuration,
    )

    private fun podcast(
        link: String = "https://example.com",
        url: String = "https://example.com/rss.xml",
    ) = Podcast(
        id = 1L,
        podcastGuid = "podcast-guid",
        title = "팟캐스트 제목",
        url = url,
        originalUrl = url,
        link = link,
        description = "설명",
        author = "저자",
        ownerName = "소유자",
        image = "https://example.com/image.png",
        artwork = "https://example.com/artwork.png",
        lastUpdateTime = Instant.fromEpochSeconds(1_758_000_000L),
        lastCrawlTime = Instant.fromEpochSeconds(1_758_000_000L),
        lastParseTime = Instant.fromEpochSeconds(1_758_000_000L),
        lastGoodHttpStatusTime = Instant.fromEpochSeconds(1_758_000_000L),
        lastHttpStatus = 200,
        contentType = "application/rss+xml",
        language = "ko",
        type = 0,
        dead = 0,
        episodeCount = 10,
        crawlErrors = 0,
        parseErrors = 0,
        locked = 0,
    )

    // --- 팟캐스트 ---

    @Test
    fun `podcast share uses the website link`() {
        val content = podcast().toShareContent()

        assertEquals("팟캐스트 제목", content.subject)
        assertEquals("팟캐스트 제목\nhttps://example.com", content.text)
    }

    @Test
    fun `podcast share falls back to the feed url when the website is empty`() {
        // 피드가 웹사이트를 주지 않는 경우가 흔하다. 기존 공유 동작(link.ifEmpty { url })과 같다.
        val content = podcast(link = "").toShareContent()

        assertEquals("팟캐스트 제목\nhttps://example.com/rss.xml", content.text)
    }

    // --- 에피소드 ---

    @Test
    fun `episode subject pairs the episode with its podcast`() {
        val content = episode().toShareContent(labels)

        assertEquals("에피소드 제목 · 팟캐스트", content.subject)
    }

    @Test
    fun `episode subject is the title alone when the podcast title is missing`() {
        val content = episode(feedTitle = null).toShareContent(labels)

        assertEquals("에피소드 제목", content.subject)
    }

    @Test
    fun `episode share falls back to the feed url when the link is empty`() {
        // Episode.link 는 타입상 non-null 이지만 Podcast Index 가 빈 문자열을 흔히 내려 준다.
        val content = episode(link = "").toShareContent(labels)

        assertEquals("에피소드 제목 · 팟캐스트\nhttps://example.com/rss.xml", content.text)
    }

    @Test
    fun `episode share omits the url line when there is no web link at all`() {
        val content = episode(link = "", feedUrl = null).toShareContent(labels)

        assertEquals("에피소드 제목 · 팟캐스트", content.text)
    }

    @Test
    fun `episode share falls back to the podcast link when it has none of its own`() {
        // 실데이터에서 에피소드 link 는 95% 가 비어 있고, 사운드바이트로 들어온 항목은
        // feedUrl 마저 없다. 팟캐스트 링크는 빠짐없이 차 있어 기댈 곳이 된다.
        val content = episode(link = "", feedUrl = null)
            .toShareContent(labels, podcast = podcast())

        assertEquals("에피소드 제목 · 팟캐스트\nhttps://example.com", content.text)
    }

    @Test
    fun `episode share prefers the podcast website over the rss feed url`() {
        // feedUrl 은 에피소드 고유 주소가 아니라 그 팟캐스트의 RSS 다. 받는 사람 브라우저에
        // XML 이 열리므로, 같은 대상을 가리키는 사람이 읽을 링크가 있으면 그쪽이 낫다.
        // 이 순서가 뒤집히면 대부분의 에피소드(link 는 비고 feedUrl 은 찬)에서 팟캐스트
        // 단이 영영 닿지 않는다.
        val content = episode(link = "").toShareContent(labels, podcast = podcast())

        assertEquals("에피소드 제목 · 팟캐스트\nhttps://example.com", content.text)
    }

    @Test
    fun `episode share prefers its own link over the podcast link`() {
        val content = episode().toShareContent(labels, podcast = podcast())

        assertTrue(content.text.endsWith("https://example.com/ep1"))
    }

    @Test
    fun `clip share falls back to the podcast link too`() {
        val clip = episode(
            link = "",
            feedUrl = null,
            clipStartTime = Instant.fromEpochSeconds(83),
            clipDuration = 30.seconds,
        )

        val content = clip.toClipShareContent(labels, podcast = podcast())

        assertEquals("에피소드 제목 · 팟캐스트\n1:23 부터\nhttps://example.com", content.text)
    }

    @Test
    fun `episode share never leaks the audio file url`() {
        // 계약: enclosureUrl 은 폴백에 쓰지 않는다. 오디오 직링크라 받는 쪽 브라우저가 mp3 를
        // 통째로 내려받는다. 링크가 하나도 없을 때조차 이 값으로 메우면 안 된다.
        val bare = episode(link = "", feedUrl = null)

        assertFalse(bare.toShareContent(labels).text.contains("audio.mp3"))
        assertFalse(bare.toClipShareContent(labels).text.contains("audio.mp3"))
    }

    // --- 재생 위치 ---

    @Test
    fun `episode share carries the listening position`() {
        val content = episode().toShareContent(labels, positionMs = 83_000L)

        assertTrue(content.text.contains("1:23 지점부터"))
    }

    @Test
    fun `episode share omits the position when it is not given`() {
        val content = episode().toShareContent(labels)

        assertFalse(content.text.contains("지점부터"))
    }

    @Test
    fun `episode share omits a position that just started`() {
        // 방금 튼 에피소드에 0:03 을 붙이면 받는 쪽에 아무 뜻이 없다.
        val content = episode().toShareContent(labels, positionMs = MIN_SHARE_POSITION_MS - 1)

        assertFalse(content.text.contains("지점부터"))
    }

    @Test
    fun `episode share keeps a position exactly at the threshold`() {
        val content = episode().toShareContent(labels, positionMs = MIN_SHARE_POSITION_MS)

        assertTrue(content.text.contains("0:10 지점부터"))
    }

    // --- 클립 ---

    @Test
    fun `clip share formats the start as an offset, not a wall clock time`() {
        // clipStartTime 은 "에피소드 안에서의 오프셋 초"를 에폭에 실은 값이다. 시각으로 포맷하면
        // 1970년 날짜가 나온다 — 그 함정에 빠지지 않았는지 본다.
        val clip = episode(
            clipStartTime = Instant.fromEpochSeconds(83),
            clipDuration = 30.seconds,
        )

        val content = clip.toClipShareContent(labels)

        assertTrue(content.text.contains("1:23 부터"))
        assertFalse(content.text.contains("1970"))
    }

    @Test
    fun `clip share renders an hour long offset as h mm ss`() {
        val clip = episode(
            clipStartTime = Instant.fromEpochSeconds(3_723),
            clipDuration = 30.seconds,
        )

        assertTrue(clip.toClipShareContent(labels).text.contains("1:02:03 부터"))
    }

    @Test
    fun `clip share omits the start line when the episode has no clip`() {
        // hasClip 이 거짓인 항목(길이 0, 시작만 있음 등)은 잘라 올리지 않으므로 지점도 없다.
        val notAClip = episode(clipStartTime = Instant.fromEpochSeconds(83), clipDuration = null)

        assertEquals(
            "에피소드 제목 · 팟캐스트\nhttps://example.com/ep1",
            notAClip.toClipShareContent(labels).text,
        )
    }
}
