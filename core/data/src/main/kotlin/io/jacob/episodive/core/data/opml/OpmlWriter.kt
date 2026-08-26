package io.jacob.episodive.core.data.opml

import android.util.Xml
import io.jacob.episodive.core.model.opml.OpmlOutline
import java.io.OutputStream

/**
 * 팔로우한 팟캐스트 목록을 OPML 2.0 문서로 직렬화한다.
 *
 * 제목에 `&`·`<`·`"` 가 흔히 섞이는데, 이걸 손으로 이스케이프하다 보면 하나는 꼭 빠뜨린다.
 * `XmlSerializer` 에 맡기면 속성/텍스트 이스케이프를 알아서 해 주므로 그 문제 자체가 없다.
 */
object OpmlWriter {
    // Podcast Index 의 `podcast:guid` 확장 네임스페이스. 표준 OPML 리더는 모르는 속성이라
    // 그냥 무시하므로, 붙여도 다른 앱과의 호환성이 깨지지 않는다.
    private const val PODCAST_NAMESPACE = "https://podcastindex.org/namespace/1.0"

    /**
     * [dateCreated] 는 여기서 만들지 않고 인자로 받는다. 내부에서 현재 시각을 읽으면
     * 이 클래스가 테스트할 때마다 시각에 묶여, 출력을 시각과 무관하게 검증할 수 없다.
     */
    fun write(outputStream: OutputStream, outlines: List<OpmlOutline>, dateCreated: String) {
        val serializer = Xml.newSerializer()
        serializer.setOutput(outputStream, "UTF-8")
        serializer.startDocument("UTF-8", true)

        serializer.startTag(null, "opml")
        serializer.attribute(null, "version", "2.0")
        serializer.attribute(null, "xmlns:podcast", PODCAST_NAMESPACE)

        serializer.startTag(null, "head")
        serializer.startTag(null, "title")
        serializer.text("Episodive")
        serializer.endTag(null, "title")
        serializer.startTag(null, "dateCreated")
        serializer.text(dateCreated)
        serializer.endTag(null, "dateCreated")
        serializer.endTag(null, "head")

        serializer.startTag(null, "body")
        // xmlUrl 이 없으면 구독할 수 없는 항목이라 파일만 더럽힌다 — 아예 쓰지 않는다.
        // filter 가 아니라 mapNotNull 로 값을 꺼내는 것은 XmlSerializer 가 자바 인터페이스라
        // 인자가 플랫폼 타입이기 때문이다 — null 을 넘겨도 컴파일이 막지 않는다.
        outlines.mapNotNull { outline -> outline.xmlUrl?.let { outline to it } }
            .forEach { (outline, xmlUrl) ->
                serializer.startTag(null, "outline")
                serializer.attribute(null, "type", "rss")
                serializer.attribute(null, "text", outline.title)
                serializer.attribute(null, "title", outline.title)
                serializer.attribute(null, "xmlUrl", xmlUrl)
                outline.htmlUrl?.let { serializer.attribute(null, "htmlUrl", it) }
                outline.guid?.let { serializer.attribute(null, "podcast:guid", it) }
                serializer.endTag(null, "outline")
            }
        serializer.endTag(null, "body")

        serializer.endTag(null, "opml")
        serializer.endDocument()
        serializer.flush()
    }
}
