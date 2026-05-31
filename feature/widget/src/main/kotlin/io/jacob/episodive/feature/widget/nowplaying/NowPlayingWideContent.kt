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
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import io.jacob.episodive.core.domain.widget.NowPlayingSnapshot
import io.jacob.episodive.feature.widget.PlaybackControl
import io.jacob.episodive.feature.widget.R

/**
 * 4x1 가로 바 NowPlaying 위젯 콘텐츠 (2행 레이아웃).
 *
 * - 배경: 썸네일 추출색 솔리드 (16dp 라운드) + 스크림
 * - 1행: 썸네일 + 제목/피드, 2행: 재생 컨트롤(축소)
 * - 우상단 [BrandBadge], snapshot 없으면 컨트롤 행을 감춤
 *
 * 주의: 1셀 높이(~70dp)에 2행을 담으므로 컨트롤을 축소한다.
 */
@Composable
fun NowPlayingWideContent(
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
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                // 1행: 썸네일 + 제목/피드 (남는 공간)
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    WidgetThumbnail(
                        bitmap = artwork,
                        contentDescription = context.getString(
                            R.string.feature_widget_now_playing_artwork_desc,
                        ),
                        sizeDp = 34,
                        cornerDp = 8,
                    )
                    Spacer(modifier = GlanceModifier.width(10.dp))
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = snapshot?.title
                                ?: context.getString(R.string.feature_widget_now_playing_empty),
                            style = TextStyle(
                                color = ColorProvider(Color.White),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            maxLines = 1,
                        )
                        val subtitle = snapshot?.feedTitle?.takeIf { it.isNotBlank() }
                            ?: context.getString(R.string.feature_widget_now_playing_empty_hint)
                                .takeIf { snapshot == null }
                        subtitle?.let {
                            Spacer(modifier = GlanceModifier.height(2.dp))
                            Text(
                                text = it,
                                style = TextStyle(
                                    color = ColorProvider(WidgetOnArtworkSecondary),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                ),
                                maxLines = 1,
                            )
                        }
                    }
                }
                // 2행: 컨트롤 (snapshot 있을 때만)
                if (snapshot != null) {
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        WidgetPlayPauseButton(
                            isPlaying = snapshot.isPlaying,
                            contentDescription = context.getString(
                                if (snapshot.isPlaying) {
                                    R.string.feature_widget_now_playing_pause_desc
                                } else {
                                    R.string.feature_widget_now_playing_play_desc
                                },
                            ),
                            diameter = 30,
                        )
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        WidgetSeekButton(
                            iconRes = R.drawable.feature_widget_ic_rewind,
                            contentDescription = context.getString(
                                R.string.feature_widget_now_playing_seek_backward_desc,
                            ),
                            control = PlaybackControl.SEEK_BWD,
                            sizeDp = 32,
                            paddingDp = 6,
                        )
                        Spacer(modifier = GlanceModifier.width(2.dp))
                        WidgetSeekButton(
                            iconRes = R.drawable.feature_widget_ic_fast_forward,
                            contentDescription = context.getString(
                                R.string.feature_widget_now_playing_seek_forward_desc,
                            ),
                            control = PlaybackControl.SEEK_FWD,
                            sizeDp = 32,
                            paddingDp = 6,
                        )
                    }
                }
            }
            BrandBadge(size = 16)
        }
    }
}
