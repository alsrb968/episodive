package io.jacob.episodive.core.data.opml

import io.jacob.episodive.core.model.opml.OpmlOutline
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * android.util.Xml (Xml.newPullParser) 이 실제 안드로이드 프레임워크 구현이라
 * Robolectric 이 필요하다.
 */
@RunWith(RobolectricTestRunner::class)
class OpmlReaderTest {

    private fun read(xml: String) = OpmlReader.read(ByteArrayInputStream(xml.toByteArray(Charsets.UTF_8)))

    @Test
    fun `Given flat outline list, when reading, then all outlines are returned`() {
        // Given
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <opml version="2.0">
              <head><title>Test</title></head>
              <body>
                <outline text="Podcast A" xmlUrl="https://a.example.com/feed" />
                <outline text="Podcast B" xmlUrl="https://b.example.com/feed" />
              </body>
            </opml>
        """.trimIndent()

        // When
        val outlines = read(xml)

        // Then
        assertEquals(2, outlines.size)
        assertEquals(listOf("https://a.example.com/feed", "https://b.example.com/feed"), outlines.map { it.xmlUrl })
    }

    @Test
    fun `Given nested folder without xmlUrl, when reading, then folder itself is skipped but children are returned`() {
        // Given
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <opml version="2.0">
              <head><title>Test</title></head>
              <body>
                <outline text="뉴스">
                  <outline text="Child A" xmlUrl="https://child-a.example.com/feed" />
                </outline>
              </body>
            </opml>
        """.trimIndent()

        // When
        val outlines = read(xml)

        // Then
        assertEquals(1, outlines.size)
        assertEquals("Child A", outlines.single().title)
        assertTrue(outlines.none { it.title == "뉴스" })
    }

    @Test
    fun `Given outline without xmlUrl at leaf level, when reading, then it is skipped`() {
        // Given
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <opml version="2.0">
              <head><title>Test</title></head>
              <body>
                <outline text="No feed here" />
                <outline text="Has feed" xmlUrl="https://has-feed.example.com/feed" />
              </body>
            </opml>
        """.trimIndent()

        // When
        val outlines = read(xml)

        // Then
        assertEquals(1, outlines.size)
        assertEquals("Has feed", outlines.single().title)
    }

    @Test
    fun `Given duplicate xmlUrl entries, when reading, then only one survives`() {
        // Given
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <opml version="2.0">
              <head><title>Test</title></head>
              <body>
                <outline text="First" xmlUrl="https://dup.example.com/feed" />
                <outline text="Second" xmlUrl="https://dup.example.com/feed" />
              </body>
            </opml>
        """.trimIndent()

        // When
        val outlines = read(xml)

        // Then
        assertEquals(1, outlines.size)
    }

    @Test
    fun `Given attribute names with mixed case, when reading, then xmlUrl is still recognized`() {
        // Given
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <opml version="2.0">
              <head><title>Test</title></head>
              <body>
                <outline text="Lower" xmlurl="https://lower.example.com/feed" />
                <outline text="Upper" XMLURL="https://upper.example.com/feed" />
              </body>
            </opml>
        """.trimIndent()

        // When
        val outlines = read(xml)

        // Then
        assertEquals(2, outlines.size)
        assertTrue(outlines.any { it.xmlUrl == "https://lower.example.com/feed" })
        assertTrue(outlines.any { it.xmlUrl == "https://upper.example.com/feed" })
    }

    @Test
    fun `Given no text attribute, when reading, then title falls back to title attribute`() {
        // Given
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <opml version="2.0">
              <head><title>Test</title></head>
              <body>
                <outline title="Fallback Title" xmlUrl="https://fallback.example.com/feed" />
              </body>
            </opml>
        """.trimIndent()

        // When
        val outlines = read(xml)

        // Then
        assertEquals("Fallback Title", outlines.single().title)
    }

    @Test
    fun `Given neither text nor title attribute, when reading, then title falls back to xmlUrl`() {
        // Given
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <opml version="2.0">
              <head><title>Test</title></head>
              <body>
                <outline xmlUrl="https://no-title.example.com/feed" />
              </body>
            </opml>
        """.trimIndent()

        // When
        val outlines = read(xml)

        // Then
        // 정규화를 거치므로 제목도 정규화된 xmlUrl 과 같아야 한다.
        assertEquals(outlines.single().xmlUrl, outlines.single().title)
    }

    @Test
    fun `Given mixed-case scheme and host with mixed-case path and query, when reading, then only scheme and host are lowercased`() {
        // Given
        // 경로/쿼리를 소문자로 바꾸면 대소문자를 구분하는 서버에서 멀쩡한 주소가 깨진다.
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <opml version="2.0">
              <head><title>Test</title></head>
              <body>
                <outline text="Mixed" xmlUrl="HTTPS://Example.COM/Feed/ABC?x=Y" />
              </body>
            </opml>
        """.trimIndent()

        // When
        val outlines = read(xml)

        // Then
        assertEquals("https://example.com/Feed/ABC?x=Y", outlines.single().xmlUrl)
    }

    @Test
    fun `Given malformed XML, when reading, then the parser exception propagates`() {
        // Given
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <opml version="2.0">
              <head><title>Test</title></head>
              <body>
                <outline text="Broken" xmlUrl="https://broken.example.com/feed"
              </body>
            </opml>
        """.trimIndent()

        // When / Then
        // 여기서 조용히 빈 목록을 돌려주면 사용자는 가져오기가 "성공했지만 0개" 라고 오인한다 —
        // 깨진 파일임을 알 수 있도록 예외가 그대로 올라와야 한다.
        assertThrows(Exception::class.java) { read(xml) }
    }

    @Test
    fun `Given OPML written by OpmlWriter, when read back, then podcast namespaced guid round-trips`() {
        // Given
        // 실제로 있었던 버그의 회귀 테스트다: 리더가 속성 이름을 "guid" 문자열과만
        // 완전히 비교하면, 네임스페이스 처리를 꺼 둔 파서가 돌려주는 속성 이름은
        // 접두사가 그대로 붙은 "podcast:guid" 라서 못 찾는다. 그 결과 다른 앱이 만든
        // 파일은 멀쩡히 읽히는데, **우리가 우리 손으로 내보낸 파일을 다시 읽을 때만**
        // guid 가 조용히 사라진다 — 접두사를 떼고 비교해야만 이 경로가 살아난다.
        val outline = OpmlOutline(
            title = "Round Trip Show",
            xmlUrl = "https://round-trip.example.com/feed",
            htmlUrl = "https://round-trip.example.com",
            guid = "abc-123-guid",
        )
        val outputStream = ByteArrayOutputStream()
        OpmlWriter.write(outputStream, listOf(outline), dateCreated = "Tue, 25 Aug 2026 00:00:00 GMT")

        // When
        val outlines = OpmlReader.read(ByteArrayInputStream(outputStream.toByteArray()))

        // Then
        val recovered = outlines.single()
        assertEquals(outline.title, recovered.title)
        assertEquals(outline.xmlUrl, recovered.xmlUrl)
        assertEquals(outline.htmlUrl, recovered.htmlUrl)
        assertEquals(outline.guid, recovered.guid)
    }

    @Test
    fun `Given OPML with no guid attribute, when reading, then guid is null`() {
        // Given
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <opml version="2.0">
              <head><title>Test</title></head>
              <body>
                <outline text="No Guid" xmlUrl="https://no-guid.example.com/feed" />
              </body>
            </opml>
        """.trimIndent()

        // When
        val outlines = read(xml)

        // Then
        assertNull(outlines.single().guid)
    }

    @Test
    fun `Given a document cut off between elements, when reading, then it fails instead of returning a partial list`() {
        // 요소 **경계**에서 잘린 파일은 파서가 예외를 내지 않는다 — 태그 중간이 잘렸을 때만
        // 던진다. 그대로 두면 200건짜리 목록이 3건만 남은 파일도 "추가 3 · 실패 0" 으로 끝나
        // 사용자가 전부 들어온 줄 안다. 부분 결과를 성공으로 위장하지 않는지 본다.
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <opml version="2.0">
              <body>
                <outline type="rss" text="A" xmlUrl="https://a.example.com/feed" />
        """.trimIndent()

        assertThrows(Exception::class.java) { read(xml) }
    }

    @Test
    fun `Given a well-formed xml that is not opml, when reading, then it fails instead of reporting zero`() {
        // 파일 선택기를 arrayOf("*/*") 로 열기 때문에 아무 파일이나 고를 수 있다. 그것이
        // 마침 잘 만들어진 XML 이면 파싱은 통과하고 body 만 없는데, 빈 목록을 돌려주면
        // 화면에는 "추가 0 · 실패 0" 이 떠서 성공한 것처럼 보인다. 잘못 고른 파일과
        // 구독이 없는 OPML 은 사용자가 취할 행동이 다르다.
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <settings><entry key="theme">dark</entry></settings>
        """.trimIndent()

        assertThrows(Exception::class.java) { read(xml) }
    }

    @Test
    fun `Given pathologically deep nesting, when reading, then it neither overflows nor loses shallow entries`() {
        // 남이 만든 파일이 얼마나 깊은지는 우리가 정할 수 없다. 재귀가 스택을 태우면
        // StackOverflowError 라 잡히지도 않는다. 상한을 넘는 깊이는 건너뛰되, 상한 위쪽에
        // 있는 정상 구독은 그대로 읽혀야 한다.
        val depth = 500
        val xml = buildString {
            append("""<?xml version="1.0" encoding="UTF-8"?><opml version="2.0"><body>""")
            append("""<outline type="rss" text="Shallow" xmlUrl="https://shallow.example.com/feed" />""")
            repeat(depth) { append("""<outline text="folder">""") }
            append("""<outline type="rss" text="Deep" xmlUrl="https://deep.example.com/feed" />""")
            repeat(depth) { append("</outline>") }
            append("</body></opml>")
        }

        val outlines = read(xml)

        assertTrue(outlines.any { it.title == "Shallow" })
    }
}
