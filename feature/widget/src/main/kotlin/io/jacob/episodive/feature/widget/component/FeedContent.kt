package io.jacob.episodive.feature.widget.component

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
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
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import io.jacob.episodive.core.domain.widget.PodcastSnapshot
import io.jacob.episodive.feature.widget.EpisodiveWidgetLayout
import io.jacob.episodive.feature.widget.FeedMode

/**
 * "나의 최신 피드" 영역. STRIP=썸네일만 1행, GRID=썸네일+제목 2행.
 *
 * 최상위 조합은 [NowPlayingContent] 가 담당하고, 여기서는 피드 표현만 다룬다.
 */
@Composable
internal fun FeedArea(
    feed: List<PodcastSnapshot>,
    feedBitmaps: Map<Long, Bitmap?>,
    feedBackgroundColor: Int,
    layout: EpisodiveWidgetLayout,
    modifier: GlanceModifier,
) {
    Box(
        modifier = modifier.background(ColorProvider(Color(feedBackgroundColor))),
    ) {
        when (layout.feedMode) {
            FeedMode.STRIP -> FeedStrip(feed, feedBitmaps)
            FeedMode.GRID -> FeedGrid(feed, feedBitmaps, layout.gridColumns)
            FeedMode.NONE -> Unit
        }
    }
}

@Composable
private fun FeedStrip(
    feed: List<PodcastSnapshot>,
    feedBitmaps: Map<Long, Bitmap?>,
) {
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        feed.forEach { podcast ->
            Box(
                modifier = GlanceModifier.defaultWeight(),
                contentAlignment = Alignment.Center,
            ) {
                FeedCell(podcast, feedBitmaps[podcast.id], showTitle = false, thumbDp = STRIP_THUMB_DP)
            }
        }
    }
}

@Composable
private fun FeedGrid(
    feed: List<PodcastSnapshot>,
    feedBitmaps: Map<Long, Bitmap?>,
    columns: Int,
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        feed.chunked(columns).forEachIndexed { rowIndex, rowItems ->
            if (rowIndex > 0) {
                Spacer(modifier = GlanceModifier.height(6.dp))
            }
            Row(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                rowItems.forEach { podcast ->
                    Box(
                        modifier = GlanceModifier.defaultWeight(),
                        contentAlignment = Alignment.Center,
                    ) {
                        FeedCell(
                            podcast,
                            feedBitmaps[podcast.id],
                            showTitle = true,
                            thumbDp = GRID_THUMB_DP,
                        )
                    }
                }
                // 마지막 행이 모자라면 빈 칸으로 정렬 유지.
                repeat(columns - rowItems.size) {
                    Box(modifier = GlanceModifier.defaultWeight()) {}
                }
            }
        }
    }
}

@Composable
private fun FeedCell(
    podcast: PodcastSnapshot,
    bitmap: Bitmap?,
    showTitle: Boolean,
    thumbDp: Int,
) {
    Column(
        modifier = GlanceModifier.clickable(actionRunCallback<OpenAppCallback>()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        WidgetThumbnail(
            bitmap = bitmap,
            contentDescription = podcast.title,
            sizeDp = thumbDp,
            cornerDp = 8,
        )
        if (showTitle) {
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = podcast.title,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                ),
                maxLines = 1,
            )
        }
    }
}

private const val STRIP_THUMB_DP = 56
private const val GRID_THUMB_DP = 44
