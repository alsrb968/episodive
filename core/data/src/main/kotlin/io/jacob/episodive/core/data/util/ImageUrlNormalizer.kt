package io.jacob.episodive.core.data.util

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * 이미지 URL 경로의 연속 슬래시를 하나로 접는다.
 *
 * 피드가 주는 URL에 `http://host//images/a.jpg` 처럼 슬래시가 겹쳐 오는 경우가 있고, 서버에 따라
 * 이걸 404로 돌려준다(실측: 겹치면 404, 접으면 200). 원인이 데이터 쪽이라 앱이 고칠 수 있는 몇 안 되는
 * 실패다.
 *
 * 문자열을 정규식으로 훑지 않고 [okhttp3.HttpUrl] 로 파싱해 **경로 세그먼트만** 다룬다. 그래야
 * 스킴의 `//`, 쿼리, 프래그먼트, 퍼센트 인코딩된 `%2F` 가 규칙이 아니라 타입으로 보호된다.
 */
fun normalizeImageUrl(raw: String): String {
    // http(s) 가 아니면 손대지 않는다. content://, file://, 리소스 경로, 빈 문자열이 여기서 빠져나간다.
    val url = raw.toHttpUrlOrNull() ?: return raw

    val segments = url.encodedPathSegments

    // 마지막 세그먼트가 비어 있는 것은 "/a/" 의 끝 슬래시이지 중복이 아니다. 그것까지 지우면
    // 서버가 리다이렉트하거나 404를 내므로 판정 대상에서 뺀다.
    if (segments.dropLast(1).none { it.isEmpty() }) return raw

    val keepsTrailingSlash = segments.last().isEmpty()
    val meaningful = segments.filter { it.isNotEmpty() }
    val suffix = if (keepsTrailingSlash && meaningful.isNotEmpty()) "/" else ""

    return url.newBuilder()
        .encodedPath("/" + meaningful.joinToString("/") + suffix)
        .build()
        .toString()
}
