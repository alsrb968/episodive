package io.jacob.episodive.core.model.opml

import io.jacob.episodive.core.model.Podcast
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.Instant

/**
 * `:core:testing` 의 팩토리를 쓰지 않고 여기서 직접 만든다.
 *
 * 의존 순환 때문이 아니다 — `testImplementation` 방향은 태스크 그래프상 순환이 아니다.
 * 막는 것은 산출물 형식이다: `:core:testing` 은 `episodive.android.library` 라 AAR 을 내놓고,
 * 이 모듈은 `episodive.jvm.library` 라 그것을 소비할 수 없다. 팩토리를 함께 쓰려면 순수 JVM
 * 모듈로 떼어내야 한다. (`ShareContentTest` 와 같은 사유, 같은 방식.)
 */
class OpmlOutlineTest {

    private fun podcast(
        title: String = "팟캐스트 제목",
        url: String = "https://feed.example.com/rss.xml",
        link: String = "https://example.com",
        podcastGuid: String = "podcast-guid",
    ) = Podcast(
        id = 1L,
        podcastGuid = podcastGuid,
        title = title,
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

    @Test
    fun `xmlUrl maps the rss feed url, not the website link`() {
        // OPML 의 xmlUrl 은 정의상 피드 주소다. url 과 link 를 서로 다른 값으로 줘서,
        // 값이 우연히 같아 통과하는 일 없이 실제로 url 쪽이 매핑됐는지 확인한다.
        val outline = podcast(
            url = "https://feed.example.com/rss.xml",
            link = "https://example.com/website",
        ).toOpmlOutline()

        assertEquals("https://feed.example.com/rss.xml", outline.xmlUrl)
        assertEquals("https://example.com/website", outline.htmlUrl)
    }

    @Test
    fun `htmlUrl is null when the podcast has no website link`() {
        // 빈 속성을 파일에 그대로 쓰면 htmlUrl="" 같은 무의미한 값이 OPML 에 남는다.
        val outline = podcast(link = "").toOpmlOutline()

        assertNull(outline.htmlUrl)
    }

    @Test
    fun `htmlUrl is null when the link is blank whitespace`() {
        // 구현이 ifEmpty 가 아니라 ifBlank 를 쓴다 — 공백만 있는 값도 "없음"으로 취급해야 한다.
        val outline = podcast(link = "   ").toOpmlOutline()

        assertNull(outline.htmlUrl)
    }

    @Test
    fun `guid is null when podcastGuid is empty`() {
        val outline = podcast(podcastGuid = "").toOpmlOutline()

        assertNull(outline.guid)
    }

    @Test
    fun `normal values are carried over as is`() {
        val outline = podcast(
            title = "실제 팟캐스트",
            url = "https://feed.example.com/show.xml",
            link = "https://example.com/show",
            podcastGuid = "abc-123-guid",
        ).toOpmlOutline()

        assertEquals("실제 팟캐스트", outline.title)
        assertEquals("https://feed.example.com/show.xml", outline.xmlUrl)
        assertEquals("https://example.com/show", outline.htmlUrl)
        assertEquals("abc-123-guid", outline.guid)
    }
}
