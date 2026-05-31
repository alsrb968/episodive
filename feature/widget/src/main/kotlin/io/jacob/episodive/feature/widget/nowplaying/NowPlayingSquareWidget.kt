package io.jacob.episodive.feature.widget.nowplaying

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import dagger.hilt.android.EntryPointAccessors
import io.jacob.episodive.core.domain.widget.WidgetDataReaderEntryPoint
import io.jacob.episodive.feature.widget.image.WidgetColorExtractor
import io.jacob.episodive.feature.widget.image.WidgetImageLoader
import io.jacob.episodive.feature.widget.theme.EpisodiveGlanceTheme

/**
 * 2x2 정사각 NowPlaying 위젯. 데이터 구독 규약은 [NowPlayingWidget] 과 동일하며
 * 콘텐츠만 [NowPlayingSquareContent] 를 쓴다.
 */
class NowPlayingSquareWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val reader = EntryPointAccessors
            .fromApplication(
                context.applicationContext,
                WidgetDataReaderEntryPoint::class.java,
            )
            .widgetDataReader()

        provideContent {
            val snapshot by remember { reader.nowPlayingFlow() }.collectAsState(null)
            val bitmap by produceState<Bitmap?>(null, snapshot?.imageUrl) {
                value = snapshot?.imageUrl?.let { WidgetImageLoader.loadWidgetBitmap(context, it) }
            }
            val backgroundColor = remember(bitmap) { WidgetColorExtractor.backgroundColor(bitmap) }

            EpisodiveGlanceTheme {
                NowPlayingSquareContent(snapshot, bitmap, backgroundColor)
            }
        }
    }
}
