package io.jacob.episodive.core.designsystem.component

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import android.graphics.Color as AndroidColor

/** 비트맵에서 색을 뽑을 영역. 기본은 이미지 전체([Full])다. */
enum class DominantRegion {
    Top,
    Bottom,
    Left,
    Right,
    Center,
    Full
}

/**
 * 앱 전체가 공유하는 단 하나의 배경색 추출기.
 *
 * 홈 위젯이 쓰던 방식(`WidgetColorExtractor`)을 그대로 기준으로 삼는다. 화면마다 필터·보정을
 * 따로 주면 같은 커버에서 화면마다 다른 색이 나오므로 여기서만 결정한다.
 *
 * - Palette 기본 필터를 유지한다(clearFilters 하지 않는다). 필터를 끄면 새까맣거나 새하얀
 *   색이 dominant 로 뽑혀 배경으로 쓸 수 없다.
 * - dominant swatch 의 휘도가 [TARGET_MAX_LUMINANCE] 보다 밝으면 RGB 비율(색감)을 유지한 채
 *   균등 스케일로 낮춘다. 밝은 커버에서도 위에 얹히는 흰 글씨 대비가 유지된다.
 */
object EpisodiveDominantColor {
    /** 비트맵이 없거나 추출 실패 시 기본 다크 뉴트럴. */
    const val FALLBACK_ARGB: Int = 0xFF1C1B1F.toInt()

    val Fallback: Color = Color(FALLBACK_ARGB)

    /** 클램프할 목표 최대 휘도(0~255). 색감을 살리기 위해 비교적 높게 둔다. */
    private const val TARGET_MAX_LUMINANCE = 130.0

    /** 영역을 잘라낼 때 쓰는 가장자리 비율 (Top/Bottom/Left/Right). */
    private const val EDGE_REGION_RATIO = 0.1f

    /** 가운데 영역을 잘라낼 때 쓰는 비율. */
    private const val CENTER_REGION_RATIO = 0.5f

    /** 추출 실패 시 null. 호출부가 기존 색을 유지할지 [Fallback] 을 쓸지 정한다. */
    fun extractArgb(bitmap: Bitmap, region: DominantRegion = DominantRegion.Full): Int? {
        val rgb = runCatching {
            paletteOf(bitmap, region).dominantSwatch?.rgb
        }.getOrNull() ?: return null

        return darken(rgb)
    }

    fun extract(bitmap: Bitmap, region: DominantRegion = DominantRegion.Full): Color? =
        extractArgb(bitmap, region)?.let(::Color)

    /** 휘도가 목표보다 밝으면 RGB 비율(색감)을 유지한 채 균등 스케일로 낮춘다. */
    fun darken(argb: Int): Int {
        val red = AndroidColor.red(argb)
        val green = AndroidColor.green(argb)
        val blue = AndroidColor.blue(argb)

        val luminance = 0.299 * red + 0.587 * green + 0.114 * blue
        val factor = if (luminance > TARGET_MAX_LUMINANCE) {
            TARGET_MAX_LUMINANCE / luminance
        } else {
            1.0
        }

        return AndroidColor.rgb(
            (red * factor).toInt(),
            (green * factor).toInt(),
            (blue * factor).toInt(),
        )
    }

    private fun paletteOf(bitmap: Bitmap, region: DominantRegion): Palette {
        val builder = Palette.from(bitmap)

        return when (region) {
            DominantRegion.Full -> builder.generate()

            DominantRegion.Top -> {
                val height = (bitmap.height * EDGE_REGION_RATIO).toInt().coerceAtLeast(1)
                builder.setRegion(0, 0, bitmap.width, height).generate()
            }

            DominantRegion.Bottom -> {
                val height = (bitmap.height * EDGE_REGION_RATIO).toInt().coerceAtLeast(1)
                builder.setRegion(0, bitmap.height - height, bitmap.width, bitmap.height).generate()
            }

            DominantRegion.Left -> {
                val width = (bitmap.width * EDGE_REGION_RATIO).toInt().coerceAtLeast(1)
                builder.setRegion(0, 0, width, bitmap.height).generate()
            }

            DominantRegion.Right -> {
                val width = (bitmap.width * EDGE_REGION_RATIO).toInt().coerceAtLeast(1)
                builder.setRegion(bitmap.width - width, 0, bitmap.width, bitmap.height).generate()
            }

            DominantRegion.Center -> {
                val width = (bitmap.width * CENTER_REGION_RATIO).toInt().coerceAtLeast(1)
                val height = (bitmap.height * CENTER_REGION_RATIO).toInt().coerceAtLeast(1)
                val left = (bitmap.width - width) / 2
                val top = (bitmap.height - height) / 2
                builder.setRegion(left, top, left + width, top + height).generate()
            }
        }
    }
}
