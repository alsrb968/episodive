package io.jacob.episodive.core.data.opml

import io.jacob.episodive.core.model.opml.OpmlOutline
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * android.util.Xml (Xml.newSerializer) 이 실제 안드로이드 프레임워크 구현이라
 * Robolectric 이 필요하다.
 */
@RunWith(RobolectricTestRunner::class)
class OpmlWriterTest {

    private fun write(outlines: List<OpmlOutline>, dateCreated: String = "Tue, 25 Aug 2026 00:00:00 GMT"): String {
        val outputStream = ByteArrayOutputStream()
        OpmlWriter.write(outputStream, outlines, dateCreated)
        return outputStream.toString("UTF-8")
    }

    @Test
    fun `Given outlines with xmlUrl, when writing, then each outline count and xmlUrl are preserved`() {
        // Given
        val outlines = listOf(
            OpmlOutline(title = "Podcast A", xmlUrl = "https://a.example.com/feed"),
            OpmlOutline(title = "Podcast B", xmlUrl = "https://b.example.com/feed"),
        )

        // When
        val xml = write(outlines)

        // Then
        assertEquals(2, Regex("<outline\\b").findAll(xml).count())
        assertTrue(xml.contains("https://a.example.com/feed"))
        assertTrue(xml.contains("https://b.example.com/feed"))
    }

    @Test
    fun `Given title with special characters, when writing and reparsing, then title round-trips`() {
        // Given
        // 손으로 이스케이프하면 &, <, " 중 하나는 꼭 빠뜨린다. 문자열에 &amp; 가
        // 있는지 보는 것보다, 실제로 파서로 되읽어 원본 제목이 복원되는지 보는 편이
        // 이스케이프 누락을 훨씬 확실하게 잡아낸다.
        val trickyTitle = """A & B <Show> "Quoted""""
        val outlines = listOf(OpmlOutline(title = trickyTitle, xmlUrl = "https://tricky.example.com/feed"))

        // When
        val xml = write(outlines)
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(ByteArrayInputStream(xml.toByteArray(Charsets.UTF_8)), "UTF-8")

        var recoveredTitle: String? = null
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG && parser.name == "outline") {
                recoveredTitle = parser.getAttributeValue(null, "title")
            }
            event = parser.next()
        }

        // Then
        assertEquals(trickyTitle, recoveredTitle)
    }

    @Test
    fun `Given outline with null htmlUrl, when writing, then htmlUrl attribute is absent`() {
        // Given
        val outlines = listOf(
            OpmlOutline(title = "No Site", xmlUrl = "https://no-site.example.com/feed", htmlUrl = null),
        )

        // When
        val xml = write(outlines)

        // Then
        assertFalse(xml.contains("htmlUrl"))
    }

    @Test
    fun `Given outline with null guid, when writing, then guid attribute is absent`() {
        // Given
        val outlines = listOf(
            OpmlOutline(title = "No Guid", xmlUrl = "https://example.com/feed", guid = null),
        )

        // When
        val xml = write(outlines)

        // Then
        // 속성 이름으로 본다. 그냥 "guid" 를 찾으면 제목이나 주소에 우연히 들어 있는
        // 글자에도 걸려, 속성이 실제로 빠졌는지와 무관하게 판정이 흔들린다.
        assertFalse(xml.contains("podcast:guid"))
    }

    @Test
    fun `Given outline with null xmlUrl, when writing, then that outline is not written at all`() {
        // Given
        // xmlUrl 이 없으면 구독할 수 없는 항목이라 파일만 더럽힌다 — 아예 쓰지 않아야 한다.
        val outlines = listOf(
            OpmlOutline(title = "Unsubscribable", xmlUrl = null),
            OpmlOutline(title = "Subscribable", xmlUrl = "https://subscribable.example.com/feed"),
        )

        // When
        val xml = write(outlines)

        // Then
        assertFalse(xml.contains("Unsubscribable"))
        assertTrue(xml.contains("Subscribable"))
        assertEquals(1, Regex("<outline\\b").findAll(xml).count())
    }

    @Test
    fun `Given dateCreated argument, when writing, then it is embedded verbatim without reading current time`() {
        // Given
        // 내부에서 현재 시각을 읽지 않는다는 계약 — 인자 그대로 들어가야 시각과 무관하게
        // 출력을 검증할 수 있다.
        val fixedDate = "Sat, 01 Jan 2000 00:00:00 GMT"

        // When
        val xml = write(outlines = emptyList(), dateCreated = fixedDate)

        // Then
        assertTrue(xml.contains(fixedDate))
    }
}
