package io.jacob.episodive.feature.widget

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

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
) {
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
        private val HEIGHT_GRID_MIN = 300.dp

        fun forSize(size: DpSize): EpisodiveWidgetLayout {
            val wide = size.width >= WIDTH_WIDE_MIN
            return when {
                size.height >= HEIGHT_GRID_MIN -> {
                    val columns = if (wide) 4 else 3
                    // 3열 6 / 4열 8
                    EpisodiveWidgetLayout(FeedMode.GRID, feedCount = columns * 2, gridColumns = columns)
                }

                size.height >= HEIGHT_STRIP_MIN -> {
                    val count = if (wide) 5 else 4
                    // 3열 4 / 4열 5
                    EpisodiveWidgetLayout(FeedMode.STRIP, feedCount = count, gridColumns = count)
                }

                else -> EpisodiveWidgetLayout(FeedMode.NONE, feedCount = 0, gridColumns = 0)
            }
        }
    }
}
