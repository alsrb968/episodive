package io.jacob.episodive.feature.widget.nowplaying

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.ActionCallback
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
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import io.jacob.episodive.core.domain.widget.NowPlayingSnapshot
import io.jacob.episodive.feature.widget.PlaybackControl
import io.jacob.episodive.feature.widget.R
import io.jacob.episodive.feature.widget.theme.WidgetSurfaceContainerLow

/**
 * 4x2 현재 재생 중 위젯 콘텐츠.
 *
 * - 배경: 썸네일에서 추출한 dominant 색 솔리드 (16dp 라운드) + 하단 가독성 스크림
 * - 상단: 썸네일 + 제목/피드, 하단: 재생 컨트롤 (-15s · Play · +30s)
 * - 전경 흰색, 우상단 [BrandBadge], snapshot 없으면 [EmptyNowPlaying]
 *
 * @param backgroundColor 썸네일에서 추출·어둡게 보정한 ARGB 배경색
 */
@Composable
fun NowPlayingContent(
    snapshot: NowPlayingSnapshot?,
    artwork: Bitmap?,
    backgroundColor: Int,
) {
    if (snapshot == null) {
        EmptyNowPlaying()
    } else {
        FilledNowPlaying(snapshot, artwork, backgroundColor)
    }
}

@Composable
private fun EmptyNowPlaying() {
    val context = LocalContext.current
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(4.dp),
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(WidgetSurfaceContainerLow)
                .cornerRadius(16.dp)
                .padding(14.dp)
                .clickable(actionRunCallback<OpenAppCallback>()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                provider = ImageProvider(R.drawable.feature_widget_ic_placeholder),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = GlanceModifier
                    .size(56.dp)
                    .cornerRadius(10.dp),
            )
            Spacer(modifier = GlanceModifier.height(10.dp))
            Text(
                text = context.getString(R.string.feature_widget_now_playing_empty),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                ),
                maxLines = 1,
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(
                text = context.getString(R.string.feature_widget_now_playing_empty_hint),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 12.sp,
                ),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun FilledNowPlaying(
    snapshot: NowPlayingSnapshot,
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
            // 스크림: 하단 가독성 + 위→아래 의사 그라데이션.
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
                    .padding(14.dp),
            ) {
                // 상단: 썸네일 + 제목/피드
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    WidgetThumbnail(
                        bitmap = artwork,
                        contentDescription = context.getString(
                            R.string.feature_widget_now_playing_artwork_desc,
                        ),
                        sizeDp = 56,
                    )
                    Spacer(modifier = GlanceModifier.width(12.dp))
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = snapshot.title,
                            style = TextStyle(
                                color = ColorProvider(Color.White),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            maxLines = 2,
                        )
                        snapshot.feedTitle?.takeIf { it.isNotBlank() }?.let { feed ->
                            Spacer(modifier = GlanceModifier.height(2.dp))
                            Text(
                                text = feed,
                                style = TextStyle(
                                    color = ColorProvider(WidgetOnArtworkSecondary),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                ),
                                maxLines = 1,
                            )
                        }
                    }
                }
                // 컨트롤은 항상 하단 고정 (남는 공간은 weight spacer).
                Spacer(modifier = GlanceModifier.defaultWeight())
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
                    )
                    Spacer(modifier = GlanceModifier.width(10.dp))
                    WidgetSeekButton(
                        iconRes = R.drawable.feature_widget_ic_rewind,
                        contentDescription = context.getString(
                            R.string.feature_widget_now_playing_seek_backward_desc,
                        ),
                        control = PlaybackControl.SEEK_BWD,
                    )
                    Spacer(modifier = GlanceModifier.width(4.dp))
                    WidgetSeekButton(
                        iconRes = R.drawable.feature_widget_ic_fast_forward,
                        contentDescription = context.getString(
                            R.string.feature_widget_now_playing_seek_forward_desc,
                        ),
                        control = PlaybackControl.SEEK_FWD,
                    )
                }
            }
            BrandBadge()
        }
    }
}

/**
 * 빈 상태 / 아트워크 탭 시 MainActivity 를 연다.
 */
class OpenAppCallback : ActionCallback {
    override suspend fun onAction(
        context: android.content.Context,
        glanceId: androidx.glance.GlanceId,
        parameters: ActionParameters,
    ) {
        io.jacob.episodive.feature.widget.action
            .openAppPendingIntent(context, REQ_OPEN_APP)
            .send()
    }

    companion object {
        private const val REQ_OPEN_APP = 99
    }
}
