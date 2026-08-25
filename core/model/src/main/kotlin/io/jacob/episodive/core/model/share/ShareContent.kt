package io.jacob.episodive.core.model.share

import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.Podcast
import io.jacob.episodive.core.model.mapper.toMediaTime
import kotlin.time.Duration.Companion.milliseconds

/**
 * 공유 시트에 실을 제목과 본문.
 *
 * 조립 규칙을 여기 한 곳에 모은다. 화면마다 문자열을 이어 붙이면 같은 공유 문구가 화면 수만큼
 * 갈라지고, 그때부터는 어느 것이 옳은지 판정할 근거가 없어진다.
 */
data class ShareContent(
    val subject: String,
    val text: String,
)

/**
 * 화면이 `stringResource` 로 채워 넘기는 문구 조각.
 *
 * 이 모듈은 안드로이드 리소스를 읽을 수 없어(순수 JVM) 형식 문자열을 밖에서 받는다.
 */
data class ShareLabels(
    /** `"%1$s · %2$s"` — 에피소드 제목, 팟캐스트 제목 */
    val episodeSubjectFormat: String,
    /** `"%1$s 부터"` — 클립이 시작하는 지점 */
    val clipLineFormat: String,
    /** `"%1$s 지점부터"` — 듣고 있던 위치 */
    val positionLineFormat: String,
)

/**
 * 재생 위치를 함께 실을 최소 지점.
 *
 * 방금 튼 에피소드에 `0:03` 을 붙이면 받는 쪽에 아무 뜻이 없다. 그 아래는 위치 줄을 뺀다.
 */
const val MIN_SHARE_POSITION_MS = 10_000L

/**
 * 팟캐스트 공유 — 제목과 링크.
 *
 * 링크는 웹사이트가 없으면 RSS 라도 보낸다. 브라우저에서 XML 이 열리는 것은 아쉽지만, 이는
 * 기존 팟캐스트 공유가 이미 하던 동작이라 여기서 바꾸면 회귀가 된다.
 */
fun Podcast.toShareContent(): ShareContent = ShareContent(
    subject = title,
    text = shareText(subject = title, detailLine = null, url = shareWebUrl()),
)

/**
 * 에피소드 공유. [positionMs] 를 주면 듣고 있던 지점을 함께 싣는다
 * ([MIN_SHARE_POSITION_MS] 미만이면 무시).
 *
 * [podcast] 는 에피소드에 웹 링크가 없을 때 쓰는 마지막 폴백이다 — 자세한 사정은
 * [shareWebUrl] 참고. 가진 화면만 주면 되고, 없으면 링크 줄이 빠진다.
 */
fun Episode.toShareContent(
    labels: ShareLabels,
    podcast: Podcast? = null,
    positionMs: Long? = null,
): ShareContent {
    val subject = shareSubject(labels)
    val positionLine = positionMs
        ?.takeIf { it >= MIN_SHARE_POSITION_MS }
        ?.let { labels.positionLineFormat.format(it.milliseconds.toMediaTime()) }

    return ShareContent(
        subject = subject,
        text = shareText(subject, positionLine, shareWebUrl(podcast)),
    )
}

/**
 * 클립(사운드바이트) 공유 — 잘라낸 구간의 시작 지점을 함께 싣는다.
 *
 * **[Episode.clipStartTime] 을 시각으로 포맷하면 안 된다.** 그 값은 벽시계가 아니라 "에피소드
 * 안에서의 오프셋 초"를 에폭에 실어 담은 것이라(`NetworkMapper` 가 `Long.toInstant()` 로 만든다),
 * 날짜로 찍으면 1970년이 나온다. 오프셋으로 되돌린 [Episode.clipStartPositionMs] 를 쓴다.
 *
 * 클립 정보가 없는 에피소드면 지점 줄 없이 일반 공유와 같아진다.
 */
fun Episode.toClipShareContent(
    labels: ShareLabels,
    podcast: Podcast? = null,
): ShareContent {
    val subject = shareSubject(labels)
    val clipLine = clipStartPositionMs
        .takeIf { hasClip }
        ?.let { labels.clipLineFormat.format(it.milliseconds.toMediaTime()) }

    return ShareContent(
        subject = subject,
        text = shareText(subject, clipLine, shareWebUrl(podcast)),
    )
}

/**
 * 공유에 실을 웹 링크. 없으면 null 이고, 그때는 링크 줄 자체를 넣지 않는다.
 *
 * 폴백이 세 단인 이유는 실제 데이터가 그만큼 비어 있기 때문이다. 기기 DB 를 세어 보면
 * 에피소드 384개 중 366개(95%)가 [Episode.link] 가 빈 문자열이고, **사운드바이트로 들어온
 * 항목은 [Episode.feedUrl] 마저 하나도 없다**(`episode_with_extras` 뷰가 `soundbites.feedUrl`
 * 을 흘려보내지 않는다). 반면 팟캐스트의 링크는 빠짐없이 차 있어 마지막 기댈 곳이 된다.
 *
 * **[Episode.enclosureUrl] 로는 폴백하지 않는다.** 오디오 파일 직링크라 받는 쪽이 브라우저에서
 * mp3 를 통째로 내려받게 된다. 링크가 하나도 없을 때조차 이 값으로 메우지 않는다.
 */
internal fun Episode.shareWebUrl(podcast: Podcast? = null): String? =
    link.ifBlank { feedUrl.orEmpty() }
        .ifBlank { podcast?.shareWebUrl().orEmpty() }
        .ifBlank { null }

internal fun Podcast.shareWebUrl(): String? = link.ifBlank { url }.ifBlank { null }

private fun Episode.shareSubject(labels: ShareLabels): String =
    feedTitle?.takeIf { it.isNotBlank() }
        ?.let { labels.episodeSubjectFormat.format(title, it) }
        ?: title

private fun shareText(subject: String, detailLine: String?, url: String?): String =
    listOfNotNull(subject, detailLine, url).joinToString(separator = "\n")
