package io.jacob.episodive.core.data.opml

import android.util.Xml
import io.jacob.episodive.core.model.opml.OpmlOutline
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.InputStream
import java.net.URI

/**
 * 다른 앱이 만든 OPML 문서를 읽어 [OpmlOutline] 목록으로 돌려준다.
 *
 * OPML 은 폴더를 중첩 `<outline>` 으로 표현하므로 트리를 재귀로 훑는다. `xmlUrl` 이 없는
 * 노드는 폴더로 보고 자기 자신은 건너뛰되, 그 안에 실제 구독이 숨어 있을 수 있으므로
 * 자식은 계속 순회한다.
 */
object OpmlReader {
    /** [collectOutlines] 의 재귀 깊이 상한. 이 아래는 [OpmlReader] 밖에서 쓸 일이 없다. */
    private const val MAX_DEPTH = 64

    // 깨진 XML 은 XmlPullParserException 이 그대로 올라가게 둔다 — 여기서 잡지 않는다.
    fun read(inputStream: InputStream): List<OpmlOutline> {
        val parser = Xml.newPullParser()
        // 네임스페이스를 켜면 podcast:guid 같은 접두사 붙은 속성 조회가 번거로워진다.
        // 어차피 아래 attributeValueIgnoreCase 가 대소문자와 접두사를 함께 무시하므로 꺼 둔다.
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)

        val outlines = mutableListOf<OpmlOutline>()
        var foundBody = false
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG && parser.name.equals("body", ignoreCase = true)) {
                foundBody = true
                // body 를 닫지 못한 채 문서가 끝났다면 파일이 잘린 것이다.
                if (!collectOutlines(parser, outlines, depth = 1)) {
                    throw XmlPullParserException("OPML 문서가 body 를 닫기 전에 끝났습니다")
                }
                break
            }
            event = parser.next()
        }

        // 파일 선택기를 arrayOf("*/*") 로 열기 때문에 사용자가 OPML 이 아닌 파일을 고를 수
        // 있다. 그것이 마침 잘 만들어진 XML 이면 파싱은 통과하고 body 만 없는데, 그대로
        // 빈 목록을 돌려주면 화면에는 "추가 0 · 실패 0" 이 떠서 **성공한 것처럼 보인다.**
        // 잘못 고른 파일과 구독이 하나도 없는 OPML 은 사용자가 취할 행동이 다르므로 나눈다.
        if (!foundBody) {
            throw XmlPullParserException("OPML 문서에 body 가 없습니다")
        }

        // 파일마다 xmlUrl 대소문자 표기가 달라도 정규화 후에는 같은 주소가 겹칠 수 있다.
        return outlines.distinctBy { it.xmlUrl }
    }

    /**
     * [parser] 가 방금 부모 태그(`<body>` 또는 상위 `<outline>`)의 START_TAG 를 반환한
     * 직후라고 가정한다. 자식을 모두 훑고 부모의 END_TAG 까지 소비한 뒤 반환한다.
     *
     * **부모의 END_TAG 를 실제로 만났으면 true**, 그 전에 문서가 끝났으면 false 다.
     * 이 구분이 없으면 요소 경계에서 잘린 파일이 정상적으로 읽힌 것과 똑같이 보인다 —
     * 200건짜리 목록이 3건만 남아도 화면은 "추가 3 · 실패 0" 을 띄운다.
     */
    private fun collectOutlines(
        parser: XmlPullParser,
        out: MutableList<OpmlOutline>,
        depth: Int,
    ): Boolean {
        var event = parser.next()
        while (event != XmlPullParser.END_TAG) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    if (parser.name.equals("outline", ignoreCase = true)) {
                        val rawXmlUrl = parser.attributeValueIgnoreCase("xmlUrl")?.trim()
                        if (!rawXmlUrl.isNullOrBlank()) {
                            val title = parser.attributeValueIgnoreCase("text")?.trim()
                                ?.ifBlank { null }
                                ?: parser.attributeValueIgnoreCase("title")?.trim()?.ifBlank { null }
                                ?: rawXmlUrl
                            out += OpmlOutline(
                                title = title,
                                xmlUrl = normalizeXmlUrl(rawXmlUrl),
                                htmlUrl = parser.attributeValueIgnoreCase("htmlUrl")?.trim()?.ifBlank { null },
                                guid = parser.attributeValueIgnoreCase("guid")?.trim()?.ifBlank { null },
                            )
                        }
                        // xmlUrl 유무와 무관하게 자식을 계속 훑는다 — 폴더도, 자식을 함께
                        // 들고 있는 구독 항목도 이 한 경로로 처리된다.
                        //
                        // 깊이에 상한을 두는 것은 우리 파일을 위해서가 아니라 남의 파일을
                        // 위해서다. 중첩이 끝없이 이어지는 문서를 만나면 이 재귀가 스택을
                        // 태우는데, 그건 잡을 수 없는 오류(StackOverflowError)로 끝난다.
                        if (depth >= MAX_DEPTH) {
                            skip(parser)
                        } else if (!collectOutlines(parser, out, depth + 1)) {
                            return false
                        }
                    } else {
                        skip(parser)
                    }
                }
                XmlPullParser.END_DOCUMENT -> return false
            }
            event = parser.next()
        }
        return true
    }

    private fun skip(parser: XmlPullParser) {
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.START_TAG -> depth++
                XmlPullParser.END_TAG -> depth--
            }
        }
    }

    /**
     * 속성 이름을 대소문자와 **네임스페이스 접두사를 모두 무시하고** 찾는다.
     *
     * 대소문자를 무시하는 것은 파일마다 `xmlUrl`/`xmlurl`/`XMLURL` 이 제각각이기 때문이고,
     * 접두사를 무시하는 것은 우리가 내보내는 `podcast:guid` 때문이다. 네임스페이스 처리를
     * 꺼 둔 파서는 속성 이름을 `"podcast:guid"` 그대로 주므로, 접두사를 떼지 않으면
     * **우리가 만든 파일을 우리가 다시 읽을 때 guid 를 못 찾는다.**
     */
    private fun XmlPullParser.attributeValueIgnoreCase(name: String): String? {
        for (i in 0 until attributeCount) {
            val attribute = getAttributeName(i).substringAfterLast(':')
            if (attribute.equals(name, ignoreCase = true)) return getAttributeValue(i)
        }
        return null
    }

    /**
     * 스킴/호스트만 소문자로 맞춘다. 경로·쿼리는 서버에 따라 대소문자가 의미를 가지므로
     * 손대지 않는다 — 여기서 건드리면 정상 주소가 깨진다.
     */
    private fun normalizeXmlUrl(raw: String): String {
        return runCatching {
            val uri = URI(raw)
            val scheme = uri.scheme?.lowercase() ?: return raw
            val host = uri.host?.lowercase() ?: return raw

            buildString {
                append(scheme).append("://").append(host)
                if (uri.port != -1) append(":").append(uri.port)
                append(uri.rawPath ?: "")
                uri.rawQuery?.let { append("?").append(it) }
                uri.rawFragment?.let { append("#").append(it) }
            }
        }.getOrDefault(raw)
    }
}
