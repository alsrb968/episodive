package io.jacob.episodive.core.model.share

import io.jacob.episodive.core.model.share.EpisodiveDeepLink.Companion.parse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * 딥링크 파싱은 앱 밖에서 오는 문자열을 처음 받는 자리다. 여기서 통과한 것은 곧바로 조회와
 * 재생으로 이어지므로, 잘못된 입력을 조용히 통과시키지 않는지가 정상 입력만큼 중요하다.
 *
 * (`:core:testing` 을 쓰지 않는 사유는 [ShareContentTest] 참고.)
 */
class EpisodiveDeepLinkTest {

    @Test
    fun `parses a podcast link`() {
        assertEquals(EpisodiveDeepLink.Podcast(42L), parse("episodive://podcast/42"))
    }

    @Test
    fun `parses an episode link`() {
        assertEquals(EpisodiveDeepLink.Episode(7L), parse("episodive://episode/7"))
    }

    @Test
    fun `reads the timestamp in seconds and hands it back in millis`() {
        assertEquals(
            EpisodiveDeepLink.Episode(7L, startPositionMs = 83_000L),
            parse("episodive://episode/7?t=83"),
        )
    }

    @Test
    fun `keeps a zero timestamp`() {
        // 0 은 없는 값이 아니라 "맨 앞부터"다. 버리면 링크가 말한 것과 다르게 재생된다.
        assertEquals(0L, (parse("episodive://episode/7?t=0") as EpisodiveDeepLink.Episode).startPositionMs)
    }

    @Test
    fun `parses a clip link with both start and duration`() {
        assertEquals(
            EpisodiveDeepLink.Episode(7L, startPositionMs = 83_000L, clipDurationMs = 45_000L),
            parse("episodive://episode/7?t=83&d=45"),
        )
    }

    @Test
    fun `ignores the scheme casing`() {
        // 스킴은 대소문자를 가리지 않는다(RFC 3986). 메신저가 그대로 넘겨 줄 수 있다.
        assertEquals(EpisodiveDeepLink.Podcast(42L), parse("EPISODIVE://podcast/42"))
    }

    @Test
    fun `rejects links that are not ours`() {
        assertNull(parse("https://example.com/episode/7"))
        assertNull(parse("episodivex://episode/7"))
        assertNull(parse(null))
        assertNull(parse(""))
    }

    @Test
    fun `rejects an unknown host`() {
        assertNull(parse("episodive://bogus/1"))
    }

    @Test
    fun `rejects ids that are not usable`() {
        // 0 과 음수는 조회가 빈손으로 돌아와 "열리긴 했는데 아무것도 없는" 화면이 된다.
        assertNull(parse("episodive://episode/abc"))
        assertNull(parse("episodive://episode/0"))
        assertNull(parse("episodive://episode/-1"))
        assertNull(parse("episodive://episode"))
        assertNull(parse("episodive://episode/7/extra"))
    }

    @Test
    fun `drops a timestamp that cannot be read`() {
        // 링크 자체는 살린다 — 지점만 잃고 에피소드는 열리는 편이 낫다.
        val link = parse("episodive://episode/7?t=abc") as EpisodiveDeepLink.Episode
        assertEquals(7L, link.id)
        assertNull(link.startPositionMs)
    }

    @Test
    fun `drops a timestamp too large to hold in millis`() {
        // 초를 그대로 1000 배 하면 Long 을 넘겨 음수 ms 가 되고, 그것이 seekTo 로 흘러간다.
        val link = parse("episodive://episode/7?t=9223372036854775") as EpisodiveDeepLink.Episode
        assertEquals(7L, link.id)
        assertNull(link.startPositionMs)
    }

    @Test
    fun `drops a negative timestamp`() {
        assertNull((parse("episodive://episode/7?t=-5") as EpisodiveDeepLink.Episode).startPositionMs)
    }
}
