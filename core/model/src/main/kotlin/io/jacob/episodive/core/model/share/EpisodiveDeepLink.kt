package io.jacob.episodive.core.model.share

import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.Podcast

/**
 * 공유한 링크로 앱에 되돌아오는 길.
 *
 * ```
 * episodive://podcast/{feedId}
 * episodive://episode/{episodeId}
 * episodive://episode/{episodeId}?t={초}
 * episodive://episode/{episodeId}?t={초}&d={초}   // 클립
 * ```
 *
 * **클립을 별도 host 로 두지 않는다.** 클립은 "구간이 지정된 에피소드"이지 다른 개체가
 * 아니고, 사운드바이트는 자기 id 를 갖고 있지도 않다(Room 뷰가 에피소드에 흡수시킨다).
 * `d` 유무로 가른다.
 *
 * `android.net.Uri` 를 쓰지 않고 문자열로 직접 판다. 우리 URI 는 숫자 id 와 초뿐이라
 * percent-decoding 이 필요 없고, 그 덕에 이 파일이 순수 JVM 에 남아 Robolectric 없이
 * 테스트된다 — 파싱은 딥링크에서 가장 틀리기 쉬운 자리라 값싸게 많이 돌릴 수 있어야 한다.
 */
sealed interface EpisodiveDeepLink {
    data class Podcast(val id: Long) : EpisodiveDeepLink

    data class Episode(
        val id: Long,
        val startPositionMs: Long? = null,
        /** 클립 길이. 지금은 표시용이며 재생 구간을 자르는 데 쓰지 않는다 — 아래 주석 참고. */
        val clipDurationMs: Long? = null,
    ) : EpisodiveDeepLink

    companion object {
        const val SCHEME = "episodive"

        internal const val HOST_PODCAST = "podcast"
        internal const val HOST_EPISODE = "episode"
        internal const val PARAM_START = "t"
        internal const val PARAM_CLIP_DURATION = "d"

        /**
         * 우리 스킴의 링크면 그 대상을, 아니면 null 을 준다.
         *
         * 나중에 https App Links 를 붙이더라도 이 함수의 host 분기만 늘리면 되도록,
         * 스킴 검사와 host 분기를 나눠 두었다.
         */
        fun parse(uri: String?): EpisodiveDeepLink? {
            val rest = uri?.trim()?.removeSchemePrefix() ?: return null

            val path = rest.substringBefore('?')
            val query = rest.substringAfter('?', missingDelimiterValue = "")

            val segments = path.split('/').filter { it.isNotBlank() }
            if (segments.size != 2) return null

            // 0 과 음수를 함께 거른다. Podcast Index 의 id 는 모두 양수이고, 0 을 통과시키면
            // 조회가 빈손으로 돌아와 "열리긴 했는데 아무것도 없는" 화면이 된다.
            val id = segments[1].toLongOrNull()?.takeIf { it > 0L } ?: return null

            return when (segments[0].lowercase()) {
                HOST_PODCAST -> Podcast(id)
                HOST_EPISODE -> Episode(
                    id = id,
                    startPositionMs = query.secondsParamAsMillis(PARAM_START),
                    clipDurationMs = query.secondsParamAsMillis(PARAM_CLIP_DURATION),
                )

                else -> null
            }
        }
    }
}

fun Podcast.toDeepLink(): String =
    "${EpisodiveDeepLink.SCHEME}://${EpisodiveDeepLink.HOST_PODCAST}/$id"

fun Episode.toDeepLink(positionMs: Long? = null): String = buildString {
    append("${EpisodiveDeepLink.SCHEME}://${EpisodiveDeepLink.HOST_EPISODE}/$id")
    positionMs?.takeIf { it >= MIN_SHARE_POSITION_MS }?.let {
        append("?${EpisodiveDeepLink.PARAM_START}=${it / MILLIS_PER_SECOND}")
    }
}

/**
 * 클립 링크. 시작 지점과 길이를 함께 싣는다.
 *
 * **받는 쪽은 이 길이로 재생 구간을 자르지 않는다.** 클리핑 창을 짓는 곳은
 * `Episode.clippingConfiguration` 하나뿐이고 그 판정 근거는 `Episode.hasClip`(피드가 준
 * 사운드바이트)이다. 링크가 임의 구간을 실어 그 판정을 우회하면 화면이 말하는 길이와 실제로
 * 흐르는 길이가 갈라져 숫자가 튄다. 그 에피소드가 원래 사운드바이트를 가졌다면 클리핑은
 * 저절로 걸리므로 여기서 다시 지정할 이유도 없다.
 */
fun Episode.toClipDeepLink(): String = buildString {
    append("${EpisodiveDeepLink.SCHEME}://${EpisodiveDeepLink.HOST_EPISODE}/$id")
    if (!hasClip) return@buildString

    append("?${EpisodiveDeepLink.PARAM_START}=${clipStartPositionMs / MILLIS_PER_SECOND}")
    clipDuration?.inWholeSeconds?.takeIf { it > 0L }?.let {
        append("&${EpisodiveDeepLink.PARAM_CLIP_DURATION}=$it")
    }
}

private const val MILLIS_PER_SECOND = 1_000L

/**
 * 링크가 실을 수 있는 최대 오프셋(하루).
 *
 * 상한이 필요한 이유는 크기 자체보다 곱셈이다 — 초를 그대로 1000 배 하면 큰 값이 Long 을
 * 넘겨 **음수 ms** 가 되고, 그것이 그대로 `seekTo` 에 실린다. 링크는 앱 밖에서 오는
 * 문자열이라 여기서 걸러야 한다. 하루를 넘는 에피소드는 사실상 없으므로 실용적인 손해도 없다.
 */
private const val MAX_OFFSET_SECONDS = 24L * 60L * 60L

private fun String.removeSchemePrefix(): String? {
    val prefix = "${EpisodiveDeepLink.SCHEME}://"
    // 스킴은 대소문자를 가리지 않는다(RFC 3986). 브라우저·메신저가 그대로 넘겨 주는 값이라
    // 대문자로 올 수 있다.
    return takeIf { it.regionMatches(0, prefix, 0, prefix.length, ignoreCase = true) }
        ?.substring(prefix.length)
}

private fun String.secondsParamAsMillis(key: String): Long? =
    split('&')
        .firstNotNullOfOrNull { part ->
            part.substringAfter('=', missingDelimiterValue = "")
                .takeIf { part.substringBefore('=') == key }
        }
        ?.toLongOrNull()
        ?.takeIf { it in 0L..MAX_OFFSET_SECONDS }
        ?.times(MILLIS_PER_SECOND)
