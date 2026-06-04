package io.jacob.episodive.feature.widget.image

import android.graphics.Bitmap
import android.graphics.Color
import androidx.palette.graphics.Palette

/**
 * 썸네일에서 위젯 배경색을 추출한다.
 *
 * Glance 는 런타임 그라데이션을 지원하지 않으므로 단색 배경 + 스크림 조합을 쓴다.
 * 우상단 흰색 로고/하단 흰색 텍스트 대비를 보장하기 위해, dominant 색의
 * HSL lightness 를 어둡게 클램프한다(밝은 앨범아트도 항상 어두운 배경이 되도록).
 */
object WidgetColorExtractor {
    /** 비트맵이 없거나 추출 실패 시 기본 다크 뉴트럴. */
    const val FALLBACK: Int = 0xFF1C1B1F.toInt()

    /** 클램프할 목표 최대 휘도(0~255). 색감을 살리기 위해 비교적 높게 둔다. */
    private const val TARGET_MAX_LUMINANCE = 130.0

    /** "나의 최신 피드" 영역 배경을 만들 때 now-playing 배경에 곱하는 추가 어둡기 계수. */
    private const val FEED_DARKEN_FACTOR = 0.68

    /** now-playing 배경색과 그로부터 파생한 피드 영역 배경색 묶음. */
    data class WidgetBackground(val background: Int, val feed: Int)

    /**
     * 썸네일에서 배경색과 피드 배경색을 한 번에 산출하는 단일 진입점.
     * Palette 추출은 한 번만 수행([backgroundColor])하고, 피드색은 그 결과에서 파생한다.
     * 호출부가 두 함수를 따로 엮을 필요 없이 한 패스로 두 색을 받는다.
     */
    fun colors(bitmap: Bitmap?): WidgetBackground {
        val background = backgroundColor(bitmap)
        return WidgetBackground(background, feedBackgroundColor(background))
    }

    fun backgroundColor(bitmap: Bitmap?): Int {
        if (bitmap == null) return FALLBACK
        val rgb = runCatching {
            Palette.from(bitmap).generate().dominantSwatch?.rgb
        }.getOrNull() ?: return FALLBACK
        return darken(rgb)
    }

    /**
     * now-playing 배경색([backgroundColor])을 한 단계 더 어둡게 한 피드 영역 배경색.
     * 같은 색조를 유지하면서 톤만 낮춰 단일 위젯 내 영역 구분을 준다.
     */
    fun feedBackgroundColor(backgroundColor: Int): Int {
        val r = (Color.red(backgroundColor) * FEED_DARKEN_FACTOR).toInt()
        val g = (Color.green(backgroundColor) * FEED_DARKEN_FACTOR).toInt()
        val b = (Color.blue(backgroundColor) * FEED_DARKEN_FACTOR).toInt()
        return Color.rgb(r, g, b)
    }

    private fun darken(color: Int): Int {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        // 휘도가 목표보다 밝으면 RGB 비율(색감)을 유지한 채 균등 스케일로 낮춘다.
        val luminance = 0.299 * r + 0.587 * g + 0.114 * b
        val factor = if (luminance > TARGET_MAX_LUMINANCE) {
            TARGET_MAX_LUMINANCE / luminance
        } else {
            1.0
        }
        return Color.rgb(
            (r * factor).toInt(),
            (g * factor).toInt(),
            (b * factor).toInt(),
        )
    }
}
