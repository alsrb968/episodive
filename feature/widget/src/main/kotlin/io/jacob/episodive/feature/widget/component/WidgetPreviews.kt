@file:OptIn(ExperimentalGlancePreviewApi::class)

package io.jacob.episodive.feature.widget.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import io.jacob.episodive.core.domain.widget.NowPlayingSnapshot
import io.jacob.episodive.core.domain.widget.PodcastSnapshot
import io.jacob.episodive.feature.widget.EpisodiveWidgetLayout
import io.jacob.episodive.feature.widget.theme.EpisodiveGlanceTheme

/**
 * [EpisodiveWidget] 콘텐츠 프리뷰.
 *
 * Glance 컴포저블은 일반 Compose `@Preview` 로는 렌더되지 않으므로
 * `androidx.glance.preview.Preview`(실험적)를 사용한다.
 * [NowPlayingContent] 는 [androidx.glance.LocalSize] 를 직접 읽지 않고 [EpisodiveWidgetLayout] 을
 * 파라미터로 받으므로, 각 프리뷰의 `widthDp`/`heightDp` 는 넘기는 레이아웃과 동일한 크기로 맞춘다.
 * 실측 행 높이(density 480): 1행≈110 / 2행≈236 / 3행≈362dp, 가로 경계 330dp(3↔4열).
 */
private val SAMPLE_SNAPSHOT = NowPlayingSnapshot(
    episodeId = 1L,
    podcastId = 1L,
    title = "에피소드 제목 미리보기",
    feedTitle = "팟캐스트 이름",
    imageUrl = null,
    isPlaying = true,
)

private val SAMPLE_BG = 0xFF3A2D4A.toInt()
private val SAMPLE_FEED_BG = 0xFF2A1F38.toInt()

private fun sampleFeed(count: Int): List<PodcastSnapshot> =
    List(count) { i ->
        PodcastSnapshot(id = i.toLong(), title = "팟캐스트 ${i + 1}", imageUrl = null)
    }

/**
 * 넘긴 크기로 [EpisodiveWidgetLayout] 을 산출하고, 그 [EpisodiveWidgetLayout.feedCount] 만큼
 * 샘플 피드를 채워 실제 위젯과 동일한 구성으로 그린다. 썸네일은 비트맵 없이 placeholder.
 */
@Composable
private fun WidgetPreview(
    widthDp: Int,
    heightDp: Int,
    snapshot: NowPlayingSnapshot? = SAMPLE_SNAPSHOT,
) {
    val layout = EpisodiveWidgetLayout.forSize(DpSize(widthDp.dp, heightDp.dp))
    EpisodiveGlanceTheme {
        NowPlayingContent(
            snapshot = snapshot,
            artwork = null,
            backgroundColor = SAMPLE_BG,
            feedBackgroundColor = SAMPLE_FEED_BG,
            feed = sampleFeed(layout.feedCount),
            feedBitmaps = emptyMap(),
            layout = layout,
        )
    }
}

/** 1행: now-playing 만(피드 없음), 재생 중. */
@Preview(widthDp = 280, heightDp = 110)
@Composable
private fun WidgetNonePreview() {
    WidgetPreview(widthDp = 280, heightDp = 110)
}

/** 1행: 재생 항목 없음(빈 상태 안내). */
@Preview(widthDp = 280, heightDp = 110)
@Composable
private fun WidgetEmptyPreview() {
    WidgetPreview(widthDp = 280, heightDp = 110, snapshot = null)
}

/** 2행: now-playing + STRIP(썸네일 1행, 3열=4개). */
@Preview(widthDp = 280, heightDp = 236)
@Composable
private fun WidgetStripPreview() {
    WidgetPreview(widthDp = 280, heightDp = 236)
}

/** 3행: now-playing + GRID 3열(2×3=6개). */
@Preview(widthDp = 280, heightDp = 362)
@Composable
private fun WidgetGrid3ColPreview() {
    WidgetPreview(widthDp = 280, heightDp = 362)
}

/** 3행·가로 확장: now-playing + GRID 4열(2×4=8개). */
@Preview(widthDp = 380, heightDp = 362)
@Composable
private fun WidgetGrid4ColPreview() {
    WidgetPreview(widthDp = 380, heightDp = 362)
}
