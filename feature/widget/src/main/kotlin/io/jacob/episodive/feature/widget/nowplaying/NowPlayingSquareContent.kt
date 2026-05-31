package io.jacob.episodive.feature.widget.nowplaying

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import io.jacob.episodive.core.domain.widget.NowPlayingSnapshot
import io.jacob.episodive.feature.widget.R

/**
 * 2x2 정사각 NowPlaying 위젯 콘텐츠.
 *
 * - 배경: 썸네일 추출색 솔리드 (16dp 라운드) + 스크림
 * - 상단: 썸네일 / 하단: 제목 + Play (폭이 좁아 컨트롤은 Play 하나로 압축)
 * - 우상단 [BrandBadge]
 */
@Composable
fun NowPlayingSquareContent(
    snapshot: NowPlayingSnapshot?,
    artwork: Bitmap?,
    backgroundColor: Int,
) {
    val context = LocalContext.current
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(4.dp),
    ) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(backgroundColor)))
                .cornerRadius(16.dp)
                .clickable(actionRunCallback<OpenAppCallback>()),
        ) {
            Image(
                provider = ImageProvider(R.drawable.feature_widget_scrim),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(16.dp),
            )
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(12.dp),
            ) {
                WidgetThumbnail(
                    bitmap = artwork,
                    contentDescription = context.getString(
                        R.string.feature_widget_now_playing_artwork_desc,
                    ),
                    sizeDp = 40,
                    cornerDp = 8,
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = snapshot?.title
                            ?: context.getString(R.string.feature_widget_now_playing_empty),
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        maxLines = 2,
                        modifier = GlanceModifier.defaultWeight(),
                    )
                    if (snapshot != null) {
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        WidgetPlayPauseButton(
                            isPlaying = snapshot.isPlaying,
                            contentDescription = context.getString(
                                if (snapshot.isPlaying) {
                                    R.string.feature_widget_now_playing_pause_desc
                                } else {
                                    R.string.feature_widget_now_playing_play_desc
                                },
                            ),
                            diameter = 36,
                        )
                    }
                }
            }
            BrandBadge(size = 16)
        }
    }
}
