package io.jacob.episodive.core.designsystem.theme

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * v2 모서리 반경.
 *
 * 디자인은 커버 아트 크기에 반경을 비례시킨다 — 38px 커버는 10~11, 54px은 15,
 * 118px은 22, 150px은 40. M3의 5단계는 그 사다리의 대표값을 담고, 사다리에
 * 들어가지 않는 고정 반경(검색바 20, 미니플레이어 18 등)은 [EpisodiveShapes]에 둔다.
 */
val EpisodiveShapeScheme = Shapes(
    extraSmall = RoundedCornerShape(8.dp),    // 사각 필터 칩
    small = RoundedCornerShape(12.dp),        // 작은 커버(38~48px), 태그
    medium = RoundedCornerShape(16.dp),       // 리스트 커버(54~56px), 진행 카드
    large = RoundedCornerShape(22.dp),        // 그리드 카드, 캐러셀 커버(118px)
    extraLarge = RoundedCornerShape(28.dp),   // 히어로 카드, 바텀시트
)

/** M3 5단계로 표현되지 않는 v2 고정 반경. */
object EpisodiveShapes {
    /** 입력 필드·계정 행·스낵바 (14px) */
    val field = RoundedCornerShape(14.dp)

    /** 미니플레이어, 큰 리스트 썸네일 62~66px (18px) */
    val miniPlayer = RoundedCornerShape(18.dp)

    /** 검색바 (20px) */
    val searchBar = RoundedCornerShape(20.dp)

    /** 컴포넌트/설정 카드 (24px) */
    val card = RoundedCornerShape(24.dp)

    /** 바텀시트 — 상단만 둥글다 (28px) */
    val bottomSheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

    /** 플레이어 대형 커버 아트 (36px) */
    val playerCover = RoundedCornerShape(36.dp)

    /** 팟캐스트 상세 히어로 커버 (40px) */
    val heroCover = RoundedCornerShape(40.dp)

    /** 버튼·칩·아바타 등 완전한 pill */
    val pill = RoundedCornerShape(percent = 50)

    /** 커버 아트 크기에 맞춰 반경을 고르는 헬퍼. 디자인의 비례를 따른다. */
    fun coverForSize(sizeDp: Int): RoundedCornerShape = when {
        sizeDp <= 44 -> RoundedCornerShape(11.dp)
        sizeDp <= 52 -> RoundedCornerShape(12.dp)
        sizeDp <= 60 -> RoundedCornerShape(15.dp)
        // 히어로 커버 72px 이 여기 들어온다 (원본 줄 181). 경계가 70이면 22dp 로 넘어간다.
        sizeDp <= 72 -> RoundedCornerShape(18.dp)
        sizeDp <= 130 -> RoundedCornerShape(22.dp)
        else -> RoundedCornerShape(CornerSize(28.dp))
    }
}
