package io.jacob.episodive.feature.widget.component

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import io.jacob.episodive.core.domain.widget.PodcastSnapshot
import io.jacob.episodive.feature.widget.EpisodiveWidgetLayout
import io.jacob.episodive.feature.widget.FeedMode
import io.jacob.episodive.feature.widget.GRID_MARGIN_DP
import io.jacob.episodive.feature.widget.action.WIDGET_PODCAST_ID_PARAM
import io.jacob.episodive.feature.widget.action.mainActivityComponent

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
        contentAlignment = Alignment.Center,
    ) {
        when (layout.feedMode) {
            FeedMode.STRIP -> FeedStrip(feed, feedBitmaps, layout.feedThumbDp)
            FeedMode.GRID -> FeedGrid(feed, feedBitmaps, layout.gridColumns, layout.feedThumbDp)
            FeedMode.NONE -> Unit
        }
    }
}

@Composable
private fun FeedStrip(
    feed: List<PodcastSnapshot>,
    feedBitmaps: Map<Long, Bitmap?>,
    thumbDp: Int,
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
                FeedCell(podcast, feedBitmaps[podcast.id], showTitle = false, thumbDp = thumbDp)
            }
        }
    }
}

@Composable
private fun FeedGrid(
    feed: List<PodcastSnapshot>,
    feedBitmaps: Map<Long, Bitmap?>,
    columns: Int,
    thumbDp: Int,
) {
    val margin = GRID_MARGIN_DP.dp
    // 고정 크기 셀 + 셀 사이 균일 Spacer 로 wrap. 바깥 Box(Center)가 이 Column 을
    // 가운데 두므로 상하좌우 마진과 셀 간격이 모두 [GRID_MARGIN_DP] 로 균일해진다.
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        feed.chunked(columns).forEachIndexed { rowIndex, rowItems ->
            if (rowIndex > 0) {
                Spacer(modifier = GlanceModifier.height(margin))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                rowItems.forEachIndexed { colIndex, podcast ->
                    if (colIndex > 0) {
                        Spacer(modifier = GlanceModifier.width(margin))
                    }
                    FeedCell(
                        podcast,
                        feedBitmaps[podcast.id],
                        showTitle = true,
                        thumbDp = thumbDp,
                    )
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
    val context = LocalContext.current
    Column(
        // 셀 탭 → 해당 팟캐스트 화면(podcast_id extra 딥링크).
        modifier = GlanceModifier.clickable(
            actionStartActivity(
                mainActivityComponent(context),
                actionParametersOf(WIDGET_PODCAST_ID_PARAM to podcast.id),
            ),
        ),
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
                // 제목이 썸네일 폭을 넘지 않도록 폭을 썸네일에 맞춰 클램프(셀 폭 균일화).
                modifier = GlanceModifier.width(thumbDp.dp),
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

