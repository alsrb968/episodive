package io.jacob.episodive.feature.widget

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.glance.GlanceId
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import dagger.hilt.android.EntryPointAccessors
import io.jacob.episodive.core.domain.widget.WidgetDataReaderEntryPoint
import io.jacob.episodive.feature.widget.component.NowPlayingContent
import io.jacob.episodive.feature.widget.image.WidgetColorExtractor
import io.jacob.episodive.feature.widget.image.WidgetImageLoader
import io.jacob.episodive.feature.widget.theme.EpisodiveGlanceTheme
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * 재생 + 나의 최신 피드 단일 위젯 (리사이즈 3x1~4x3, 폭 3~4열·세로 1~3행).
 *
 * 핵심 규약:
 * - 데이터는 반드시 `provideContent` 컴포지션 *안*에서 `collectAsState` 로 구독한다.
 *   provideGlance 본문은 세션당 1회만 실행되므로 거기서 읽으면 updateAll/리사이즈 시 stale.
 *   재생 정보(`nowPlayingFlow`)와 피드(`userRecentPodcastsFlow`) 모두 동일 규약.
 * - `SizeMode.Exact`: 현재 크기만 컴포즈 → [LocalSize] 높이로 3단계 분기.
 *   (Responsive 는 선언한 모든 사이즈 컴포지션을 한 RemoteViews 에 담아 비트맵
 *    페이로드가 배가되어 ~1MB Binder 한도를 쉽게 초과하므로 쓰지 않는다.)
 * - 보이는 피드 항목만 비트맵 로드 + 작은 px → RemoteViews 1MB 한도 보호.
 */
class EpisodiveWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val reader = EntryPointAccessors
            .fromApplication(
                context.applicationContext,
                WidgetDataReaderEntryPoint::class.java,
            )
            .widgetDataReader()

        provideContent {
            val snapshot by remember { reader.nowPlayingFlow() }.collectAsState(null)
            val podcasts by remember { reader.userRecentPodcastsFlow(FEED_MAX) }
                .collectAsState(emptyList())
            val size = LocalSize.current
            val layout = EpisodiveWidgetLayout.forSize(size)
            // 표시 크기에 맞춘 비트맵 로드 px (밀도 반영). 작게 고정하면 업스케일로 흐려진다.
            val density = context.resources.displayMetrics.density
            val nowPlayingPx = (NOW_PLAYING_THUMB_DP * density).toInt()
            val feedPx = layout.feedThumbPx(density)

            val artwork by produceState<Bitmap?>(null, snapshot?.imageUrl) {
                value = snapshot?.imageUrl?.let {
                    WidgetImageLoader.loadWidgetBitmap(context, it, nowPlayingPx)
                }
            }
            val backgroundColor = remember(artwork) {
                WidgetColorExtractor.backgroundColor(artwork)
            }
            val feedBackgroundColor = remember(backgroundColor) {
                WidgetColorExtractor.feedBackgroundColor(backgroundColor)
            }

            // 현재 크기에서 보이는 만큼만 로드 (최대 4x3 GRID = 8개) → 비트맵 페이로드 상한.
            val visibleFeed = remember(podcasts, layout.feedCount) {
                podcasts.take(layout.feedCount)
            }
            val feedBitmaps by produceState<Map<Long, Bitmap?>>(emptyMap(), visibleFeed, feedPx) {
                value = coroutineScope {
                    visibleFeed
                        .map { snap ->
                            async {
                                snap.id to WidgetImageLoader.loadWidgetBitmap(
                                    context,
                                    snap.imageUrl,
                                    feedPx,
                                )
                            }
                        }
                        .awaitAll()
                        .toMap()
                }
            }

            EpisodiveGlanceTheme {
                NowPlayingContent(
                    snapshot = snapshot,
                    artwork = artwork,
                    backgroundColor = backgroundColor,
                    feedBackgroundColor = feedBackgroundColor,
                    feed = visibleFeed,
                    feedBitmaps = feedBitmaps,
                    layout = layout,
                )
            }
        }
    }

    private companion object {
        /** 가장 큰 4x3 GRID 에서 필요한 그리드 항목 수(2×4=8). */
        const val FEED_MAX = 8

        /** now-playing 썸네일 표시 dp. 밀도를 곱해 로드 px 산출(피드 px 는 [EpisodiveWidgetLayout.feedThumbPx]). */
        const val NOW_PLAYING_THUMB_DP = 56
    }
}
