package io.jacob.episodive.feature.widget.nowplaying

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.unit.ColorProvider
import io.jacob.episodive.feature.widget.PlaybackControl
import io.jacob.episodive.feature.widget.R
import io.jacob.episodive.feature.widget.action.WidgetActionCallback

/**
 * NowPlaying 위젯 3종(4x2 / 4x1 / 2x2)이 공유하는 재생 컨트롤·색상.
 *
 * 아트워크 배경 위 전경은 day/night 공통 밝은 톤(하단 스크림이 항상 어둡기 때문).
 */
internal val WidgetOnArtworkSecondary = Color(0xD9FFFFFF)
internal val WidgetOnArtworkControl = Color(0xFF141414)

/**
 * 흰색 불투명 원형 + 다크 아이콘 재생/일시정지 버튼.
 * 아트워크 어느 색 위에서나 도드라지도록 중립 흰색을 쓴다.
 */
@Composable
internal fun WidgetPlayPauseButton(
    isPlaying: Boolean,
    contentDescription: String,
    diameter: Int = 44,
) {
    val iconRes = if (isPlaying) {
        R.drawable.feature_widget_ic_pause
    } else {
        R.drawable.feature_widget_ic_play
    }
    Box(
        modifier = GlanceModifier
            .size(diameter.dp)
            .cornerRadius((diameter / 2).dp)
            .background(ColorProvider(Color.White))
            .clickable(
                actionRunCallback<WidgetActionCallback>(
                    parameters = actionParametersOf(
                        PlaybackControl.KEY to PlaybackControl.PLAY_PAUSE.name,
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            provider = ImageProvider(iconRes),
            contentDescription = contentDescription,
            colorFilter = ColorFilter.tint(ColorProvider(WidgetOnArtworkControl)),
            modifier = GlanceModifier.size((diameter / 2).dp),
        )
    }
}

/**
 * 흰색 틴트 seek 버튼 (-15s / +30s).
 */
@Composable
internal fun WidgetSeekButton(
    iconRes: Int,
    contentDescription: String,
    control: PlaybackControl,
    sizeDp: Int = 40,
    paddingDp: Int = 8,
) {
    Image(
        provider = ImageProvider(iconRes),
        contentDescription = contentDescription,
        colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
        modifier = GlanceModifier
            .size(sizeDp.dp)
            .padding(paddingDp.dp)
            .clickable(
                actionRunCallback<WidgetActionCallback>(
                    parameters = actionParametersOf(
                        PlaybackControl.KEY to control.name,
                    ),
                ),
            ),
    )
}

/**
 * 우상단 Episodive 브랜드 로고 (미디어 알림 small icon 과 동일한 로고를 흰색으로).
 * 원형 배경 없이 로고만 흰색 틴트로 콘텐츠 위 우상단에 겹친다 (Spotify 위젯 로고 식).
 */
@Composable
internal fun BrandBadge(size: Int = 22) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(12.dp),
        contentAlignment = Alignment.TopEnd,
    ) {
        Image(
            provider = ImageProvider(R.drawable.feature_widget_ic_brand),
            contentDescription = null,
            colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
            modifier = GlanceModifier.size(size.dp),
        )
    }
}

/**
 * 추출색 배경 위에 얹는 정사각 썸네일. 비트맵 없으면 placeholder.
 */
@Composable
internal fun WidgetThumbnail(
    bitmap: Bitmap?,
    contentDescription: String,
    sizeDp: Int,
    cornerDp: Int = 10,
) {
    val provider = if (bitmap != null) {
        ImageProvider(bitmap)
    } else {
        ImageProvider(R.drawable.feature_widget_ic_placeholder)
    }
    Image(
        provider = provider,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = GlanceModifier
            .size(sizeDp.dp)
            .cornerRadius(cornerDp.dp),
    )
}
