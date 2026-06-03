package io.jacob.episodive.feature.widget

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt

/**
 * 위젯 높이(폭 3~4열 가변, 3x1~4x3)에 따른 "나의 최신 피드" 영역 표현 방식.
 */
internal enum class FeedMode {
    /** 1행 높이: 피드 없음, now-playing 만. */
    NONE,

    /** 2행 높이: 썸네일만 1행. */
    STRIP,

    /** 3행 높이(2×3 그리드): 썸네일+제목 2행 그리드. */
    GRID,
}

/**
 * `SizeMode.Exact` 로 측정한 [DpSize] → 레이아웃 파라미터.
 *
 * 항목 수 규칙(요구사항, 폭 3~4열 가변):
 * - STRIP: 3열 4개 / 4열 5개 (한 행, 제목 없음)
 * - GRID : 3열 2×3=6개 / 4열 2×4=8개 (제목 포함)
 */
internal data class EpisodiveWidgetLayout(
    val feedMode: FeedMode,
    val feedCount: Int,
    /** GRID 일 때 행당 열 수. STRIP 은 한 행에 [feedCount] 개를 균등 배치. */
    val gridColumns: Int,
    /** GRID 썸네일 한 변(dp). 가용 폭에서 균일 마진을 빼고 산출한 가변값. STRIP/NONE 은 미사용(0). */
    val feedThumbDp: Int,
    /** now-playing 썸네일 한 변(dp). now-playing 영역 높이에 맞춰 가변하며, 항상 피드 썸네일보다 크다. */
    val nowPlayingThumbDp: Int,
) {
    /**
     * 피드 영역 고정 높이(dp). now-playing 이 나머지 세로 공간을 차지한다.
     * GRID 는 썸네일 크기에 맞춰 가변(상하좌우·셀 간 균일 마진 [GRID_MARGIN_DP] 포함).
     */
    val feedHeightDp: Int
        get() = feedHeightOf(feedMode, feedThumbDp)

    /**
     * 피드 썸네일 비트맵 로드 px. 표시 크기([feedThumbDp])에 밀도를 곱한 값을 쓰되,
     * RemoteViews 전체 비트맵 페이로드(~1MB Binder 한도)를 넘지 않도록 항목 수 기준 상한을 둔다.
     * (예전 고정 72px 는 커진 썸네일에서 업스케일돼 흐릿했음.)
     */
    fun feedThumbPx(density: Float): Int {
        if (feedThumbDp <= 0 || feedCount <= 0) return 0
        val ideal = (feedThumbDp * density).toInt()
        val maxPx = sqrt(FEED_BITMAP_BUDGET_BYTES / feedCount / BITMAP_BYTES_PER_PX.toDouble()).toInt()
        return ideal.coerceAtMost(maxPx).coerceAtLeast(FEED_THUMB_MIN_PX)
    }

    companion object {
        // 임계값은 런처가 위젯에 실제로 보고하는 dp 기준(셀 단위 ≠ 70*n-30).
        // a11y 경계 실측(density 480 = 3.0x, 3열 그리드, 1셀 ≈ 93dp):
        //   세로 1행≈110dp, 2행≈236dp, 3행≈362dp
        // 경계는 인접 행의 중간값 부근으로 둬 리사이즈 시 안정적으로 전환되게 한다.
        // 요구사항 매핑: 세로 1행→NONE, 2행→STRIP(피드 1행), 3행→GRID(피드 2행)
        // 폭은 3~4열 가변(STRIP 3열 4·4열 5, GRID 3열 2×3=6·4열 2×4=8).
        // 가로 경계는 3열≈280dp, 4열≈379dp 사이로 둔다.
        private val WIDTH_WIDE_MIN = 330.dp
        private val HEIGHT_STRIP_MIN = 175.dp
        // GRID 는 가변 썸네일로 피드 밴드가 커지므로 floor 를 높였다. 이 값(355)은 "now-playing 썸네일이
        // 항상 피드보다 큼" 불변식과 묶여 있다: 최악(피드 최대 80dp)에서도 now-playing 영역이
        // now-playing 썸네일(≥피드+여유)을 담을 수 있어야 하므로 GRID_THUMB_MAX_DP 를 바꾸면 재검토 필요.
        // 셀 매핑은 동일: 2행(≈236)→STRIP, 3행(≈362)→GRID.
        private val HEIGHT_GRID_MIN = 355.dp

        private const val GRID_THUMB_MIN_DP = 44
        private const val GRID_THUMB_MAX_DP = 80
        private const val STRIP_THUMB_DP = 56

        /** 피드 밴드 높이(dp). [forSize] 와 [feedHeightDp] 가 공유한다. */
        fun feedHeightOf(mode: FeedMode, feedThumb: Int): Int = when (mode) {
            FeedMode.STRIP -> STRIP_FEED_HEIGHT_DP
            FeedMode.GRID -> 2 * (feedThumb + GRID_TITLE_BLOCK_DP) + 3 * GRID_MARGIN_DP
            FeedMode.NONE -> 0
        }

        /**
         * now-playing 영역(위젯 높이 − 피드 밴드)에 맞춘 now-playing 썸네일 크기.
         * [NP_THUMB_MAX_DP] 는 피드 상한(80)보다 크고, [HEIGHT_GRID_MIN] 이 영역 부족으로 인한
         * 역전을 막으므로 결과는 항상 피드 썸네일보다 크다.
         */
        private fun nowPlayingThumbOf(widgetHeightDp: Float, feedBandDp: Int): Int {
            val area = widgetHeightDp.toInt() - feedBandDp
            return (area - NP_VPAD_DP).coerceIn(NP_THUMB_MIN_DP, NP_THUMB_MAX_DP)
        }

        fun forSize(size: DpSize): EpisodiveWidgetLayout {
            val wide = size.width >= WIDTH_WIDE_MIN
            return when {
                size.height >= HEIGHT_GRID_MIN -> {
                    val columns = if (wide) 4 else 3
                    // 위젯 전체 패딩이 없으므로 카드(=피드 밴드) 폭은 위젯 폭과 같다.
                    // 가용 폭에서 (열+1)개의 균일 마진을 빼고 열 수로 나눠 썸네일 크기 산출 → 가로를 꽉 채움.
                    val feedWidth = size.width.value
                    val thumb = ((feedWidth - (columns + 1) * GRID_MARGIN_DP) / columns)
                        .toInt()
                        .coerceIn(GRID_THUMB_MIN_DP, GRID_THUMB_MAX_DP)
                    // 3열 6 / 4열 8
                    EpisodiveWidgetLayout(
                        feedMode = FeedMode.GRID,
                        feedCount = columns * 2,
                        gridColumns = columns,
                        feedThumbDp = thumb,
                        nowPlayingThumbDp = nowPlayingThumbOf(
                            size.height.value,
                            feedHeightOf(FeedMode.GRID, thumb),
                        ),
                    )
                }

                size.height >= HEIGHT_STRIP_MIN -> {
                    val count = if (wide) 5 else 4
                    // 3열 4 / 4열 5
                    EpisodiveWidgetLayout(
                        feedMode = FeedMode.STRIP,
                        feedCount = count,
                        gridColumns = count,
                        feedThumbDp = STRIP_THUMB_DP,
                        nowPlayingThumbDp = nowPlayingThumbOf(
                            size.height.value,
                            feedHeightOf(FeedMode.STRIP, STRIP_THUMB_DP),
                        ),
                    )
                }

                else -> EpisodiveWidgetLayout(
                    feedMode = FeedMode.NONE,
                    feedCount = 0,
                    gridColumns = 0,
                    feedThumbDp = 0,
                    nowPlayingThumbDp = nowPlayingThumbOf(size.height.value, 0),
                )
            }
        }
    }
}

/** 피드 그리드의 상하좌우·셀 간 균일 마진(dp). */
internal const val GRID_MARGIN_DP = 14

/** GRID 셀의 제목 영역 높이(썸네일 아래 Spacer 4 + 제목 ≈16, 여유 포함). */
private const val GRID_TITLE_BLOCK_DP = 20

/** STRIP 피드 밴드 높이(dp). 썸네일 1행 + 상하 패딩. */
private const val STRIP_FEED_HEIGHT_DP = 84

/** now-playing 헤더의 세로 여백 합(dp). now-playing 썸네일은 (영역 높이 − 이 값) 이하로 둔다. */
private const val NP_VPAD_DP = 20
private const val NP_THUMB_MIN_DP = 56

/** now-playing 썸네일 상한(dp). 피드 상한(GRID_THUMB_MAX_DP=80)보다 크게 둬 "항상 피드보다 큼"을 보장. */
private const val NP_THUMB_MAX_DP = 96

/** 피드 비트맵 총 페이로드 예산(byte). RemoteViews ~1MB Binder 한도 보호용 항목당 px 상한 계산에 사용. */
private const val FEED_BITMAP_BUDGET_BYTES = 640_000
private const val BITMAP_BYTES_PER_PX = 4 // ARGB_8888
private const val FEED_THUMB_MIN_PX = 72
