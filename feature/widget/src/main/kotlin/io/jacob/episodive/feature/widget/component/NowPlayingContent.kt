package io.jacob.episodive.feature.widget.component

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
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
import io.jacob.episodive.core.domain.widget.PodcastSnapshot
import io.jacob.episodive.feature.widget.EpisodiveWidgetLayout
import io.jacob.episodive.feature.widget.FeedMode
import io.jacob.episodive.feature.widget.PlaybackControl
import io.jacob.episodive.feature.widget.R

/**
 * 재생 + 나의 최신 피드 단일 위젯 콘텐츠(최상위 조합).
 *
 * - 배경: 썸네일 추출색 솔리드 (16dp 라운드, 그라데이션/스크림 없음)
 * - 1행(now playing): 썸네일 | (제목 1줄 · 팟캐스트명 · 컨트롤). 우상단 브랜드 로고
 * - 2행(피드): [EpisodiveWidgetLayout] 에 따라 STRIP/GRID/없음. 피드 컴포저블은 FeedContent.kt 참고.
 *   피드 영역만 추출색을 더 어둡게([feedBackgroundColor]).
 */
@Composable
internal fun NowPlayingContent(
    snapshot: NowPlayingSnapshot?,
    artwork: Bitmap?,
    backgroundColor: Int,
    feedBackgroundColor: Int,
    feed: List<PodcastSnapshot>,
    feedBitmaps: Map<Long, Bitmap?>,
    layout: EpisodiveWidgetLayout,
) {
    val showFeed = layout.feedMode != FeedMode.NONE && feed.isNotEmpty()
    Box(modifier = GlanceModifier.fillMaxSize().padding(4.dp)) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(backgroundColor)))
                .cornerRadius(16.dp)
                .clickable(actionRunCallback<OpenAppCallback>()),
        ) {
            // 피드가 있으면 now-playing 은 콘텐츠 높이만 차지하고, 피드가 남는 공간을 채운다(Spotify식 · 1:1 아님).
            NowPlayingHeader(
                snapshot = snapshot,
                artwork = artwork,
                modifier = if (showFeed) {
                    GlanceModifier.fillMaxWidth()
                } else {
                    GlanceModifier.fillMaxSize()
                },
            )
            if (showFeed) {
                FeedArea(
                    feed = feed,
                    feedBitmaps = feedBitmaps,
                    feedBackgroundColor = feedBackgroundColor,
                    layout = layout,
                    modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                )
            }
        }
        BrandBadge()
    }
}

@Composable
private fun NowPlayingHeader(
    snapshot: NowPlayingSnapshot?,
    artwork: Bitmap?,
    modifier: GlanceModifier,
) {
    val context = LocalContext.current
    Row(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WidgetThumbnail(
            bitmap = artwork,
            contentDescription = context.getString(
                R.string.feature_widget_now_playing_artwork_desc,
            ),
            sizeDp = HEADER_THUMB_DP,
        )
        Spacer(modifier = GlanceModifier.width(12.dp))
        // 우상단 브랜드 로고와 겹치지 않도록 텍스트 열 오른쪽 여백 확보.
        Column(modifier = GlanceModifier.defaultWeight().padding(end = 24.dp)) {
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
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = it,
                    style = TextStyle(
                        color = ColorProvider(WidgetOnArtworkSecondary),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    maxLines = 1,
                )
            }
            if (snapshot != null) {
                Spacer(modifier = GlanceModifier.height(8.dp))
                NowPlayingControlsRow(snapshot)
            }
        }
    }
}

@Composable
private fun NowPlayingControlsRow(snapshot: NowPlayingSnapshot) {
    val context = LocalContext.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        WidgetPlayPauseButton(
            isPlaying = snapshot.isPlaying,
            contentDescription = context.getString(
                if (snapshot.isPlaying) {
                    R.string.feature_widget_now_playing_pause_desc
                } else {
                    R.string.feature_widget_now_playing_play_desc
                },
            ),
            diameter = 34,
        )
        Spacer(modifier = GlanceModifier.width(10.dp))
        WidgetSeekButton(
            iconRes = R.drawable.feature_widget_ic_rewind,
            contentDescription = context.getString(
                R.string.feature_widget_now_playing_seek_backward_desc,
            ),
            control = PlaybackControl.SEEK_BWD,
            sizeDp = 34,
            paddingDp = 6,
        )
        Spacer(modifier = GlanceModifier.width(4.dp))
        WidgetSeekButton(
            iconRes = R.drawable.feature_widget_ic_fast_forward,
            contentDescription = context.getString(
                R.string.feature_widget_now_playing_seek_forward_desc,
            ),
            control = PlaybackControl.SEEK_FWD,
            sizeDp = 34,
            paddingDp = 6,
        )
    }
}

private const val HEADER_THUMB_DP = 56

/**
 * 빈 상태 / 카드·셀 탭 시 MainActivity 를 연다.
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
