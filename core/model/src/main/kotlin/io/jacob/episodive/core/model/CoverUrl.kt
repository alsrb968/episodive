package io.jacob.episodive.core.model

/**
 * 커버 아트 URL을 고르는 단일 규칙.
 *
 * 예전에는 화면마다 폴백이 제각각이었다 — 어떤 곳은 `artwork`를 먼저 보고, 어떤 곳은 `image`를
 * 먼저 보고, 목록 카드는 아예 폴백 없이 `image`만 봤다. 그래서 `image`가 비고 `artwork`만 있는
 * 팟캐스트의 커버가 화면에 따라 떴다 안 떴다 했다.
 *
 * `image`를 앞에 두는 이유: Podcast Index 응답에서 `artwork`만 있고 `image`가 빈 경우는 실측상
 * 없었고 그 반대는 있었다. 순서를 뒤집으면 실익 없이 기존 캐시 키만 전부 갈린다.
 */
val Podcast.coverUrl: String
    get() = image.ifBlank { artwork }

/**
 * 에피소드는 자체 이미지가 없는 쪽이 오히려 흔하다(피드가 에피소드별 아트를 주지 않는다).
 * 그때는 피드 커버로 대신한다. 둘 다 비면 빈 문자열이고, 그건 StateImage 가 플레이스홀더로 받는다.
 */
val Episode.coverUrl: String
    get() = image.ifBlank { feedImage }
