package io.jacob.episodive.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * v2 레이아웃 치수.
 *
 * 값은 디자인 원본(design/Episodive-v2.dc.html)에서 그대로 읽었다. 폰 목업은
 * 344 x 746 CSS px 프레임이며 1px = 1dp로 옮긴다.
 */
@Immutable
data class DimensionTheme(
    /** 미니플레이어 높이 — 디자인 62px (기존 70에서 축소) */
    val playerBarHeight: Dp = Dp.Unspecified,
    /** 미니플레이어 좌우 여백 */
    val playerBarMargin: Dp = Dp.Unspecified,
    /** 미니플레이어와 하단 내비 사이 간격 — 좌우보다 좁게 붙인다 */
    val playerBarBottomMargin: Dp = Dp.Unspecified,
    /** 하단 내비게이션 높이 */
    val navigationBarHeight: Dp = Dp.Unspecified,
    /** 내비 활성 pill 크기 (56 x 30, radius 16) */
    val navIndicatorWidth: Dp = Dp.Unspecified,
    val navIndicatorHeight: Dp = Dp.Unspecified,
    /** 리스트·그리드 콘텐츠의 화면 좌우 여백 */
    val screenPadding: Dp = Dp.Unspecified,
    /** 제목·헤더 행의 좌우 여백 (콘텐츠보다 2px 넓다) */
    val headerPadding: Dp = Dp.Unspecified,
    /** 섹션과 섹션 사이 */
    val sectionSpacing: Dp = Dp.Unspecified,
    /** 세로 리스트 항목 간격 */
    val listItemSpacing: Dp = Dp.Unspecified,
    /** 2열 그리드 간격 */
    val gridSpacing: Dp = Dp.Unspecified,
    /** 가로 스크롤 카드 간격 */
    val carouselSpacing: Dp = Dp.Unspecified,
    /** 칩 사이 간격 */
    val chipSpacing: Dp = Dp.Unspecified,
    /** 리스트 썸네일 (54px) */
    val thumbnailSmall: Dp = Dp.Unspecified,
    /** 팔로우 목록·보관함 썸네일 (62~66px) */
    val thumbnailMedium: Dp = Dp.Unspecified,
    /** 가로 캐러셀 커버 (118px) */
    val coverCarousel: Dp = Dp.Unspecified,
    /** 팟캐스트 상세 대형 커버 (150px) */
    val coverHero: Dp = Dp.Unspecified,
    /** 기본 버튼 높이 (56px) */
    val buttonHeight: Dp = Dp.Unspecified,
    /** 보조 버튼 높이 (48~52px) */
    val buttonHeightCompact: Dp = Dp.Unspecified,
    /** 검색바·입력 필드 높이 (56px) */
    val fieldHeight: Dp = Dp.Unspecified,
    /** 원형 아이콘 버튼 지름 (40px) */
    val iconButtonSize: Dp = Dp.Unspecified,
    /** 플레이어 메인 재생 버튼 (74px) */
    val playButtonSize: Dp = Dp.Unspecified,
    /** 시크바·진행바 두께 */
    val progressThickness: Dp = Dp.Unspecified,
    /** 미니플레이어 하단 진행바 두께 */
    val progressThicknessThin: Dp = Dp.Unspecified,
) {
    /**
     * 미니플레이어가 실제로 가리는 세로 공간 (높이 + 내비와의 간격).
     * 스크롤 리스트의 하단 여백을 높이만으로 잡으면 마지막 항목이 가린다.
     */
    val playerBarSpace: Dp get() = playerBarHeight + playerBarBottomMargin
}

val LocalDimensionTheme = staticCompositionLocalOf { DimensionTheme() }

/** 디자인 원본 값을 담은 기본 치수. [EpisodiveTheme]이 제공한다. */
val DefaultDimensionTheme = DimensionTheme(
    playerBarHeight = 62.dp,
    playerBarMargin = 8.dp,
    playerBarBottomMargin = 4.dp,
    navigationBarHeight = 74.dp,
    navIndicatorWidth = 56.dp,
    navIndicatorHeight = 30.dp,
    screenPadding = 20.dp,
    headerPadding = 22.dp,
    sectionSpacing = 22.dp,
    listItemSpacing = 15.dp,
    gridSpacing = 14.dp,
    carouselSpacing = 14.dp,
    chipSpacing = 9.dp,
    thumbnailSmall = 54.dp,
    thumbnailMedium = 62.dp,
    coverCarousel = 118.dp,
    coverHero = 150.dp,
    buttonHeight = 56.dp,
    buttonHeightCompact = 48.dp,
    fieldHeight = 56.dp,
    iconButtonSize = 40.dp,
    playButtonSize = 74.dp,
    progressThickness = 5.dp,
    progressThicknessThin = 3.dp,
)
