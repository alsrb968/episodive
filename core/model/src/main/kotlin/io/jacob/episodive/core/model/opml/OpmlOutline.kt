package io.jacob.episodive.core.model.opml

import io.jacob.episodive.core.model.Podcast

/**
 * OPML `<outline>` 요소 하나를 표현한다. 내보내기/들여오기 양쪽에서 공용으로 쓴다.
 */
data class OpmlOutline(
    val title: String,
    val xmlUrl: String?,
    val htmlUrl: String? = null,
    val guid: String? = null,
)

/**
 * OPML 의 `xmlUrl` 은 정의상 RSS 피드 주소다. `Podcast.link`(웹사이트)를 넣으면
 * 다른 앱이 그 outline 으로 구독을 걸 수 없으므로 반드시 `url`을 매핑한다.
 */
fun Podcast.toOpmlOutline(): OpmlOutline = OpmlOutline(
    title = title,
    xmlUrl = url,
    htmlUrl = link.ifBlank { null },
    guid = podcastGuid.ifBlank { null },
)
