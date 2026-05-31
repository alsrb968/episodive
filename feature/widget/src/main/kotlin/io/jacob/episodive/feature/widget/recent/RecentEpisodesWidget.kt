package io.jacob.episodive.feature.widget.recent

import android.content.Context
import android.graphics.Bitmap
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import dagger.hilt.android.EntryPointAccessors
import io.jacob.episodive.core.domain.widget.EpisodeSnapshot
import io.jacob.episodive.core.domain.widget.WidgetDataReaderEntryPoint
import io.jacob.episodive.feature.widget.image.WidgetImageLoader
import io.jacob.episodive.feature.widget.theme.EpisodiveGlanceTheme
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import timber.log.Timber

/**
 * 최근 에피소드 위젯.
 *
 * 갱신은 `EpisodeSyncWorker` 성공 분기 → `WidgetUpdater` → `updateAll()` 경로로 발생한다.
 * (재생 위젯과 달리 저빈도 갱신이라 provideGlance 본문 스냅샷 방식을 유지한다.)
 */
class RecentEpisodesWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val reader = EntryPointAccessors
            .fromApplication(
                context.applicationContext,
                WidgetDataReaderEntryPoint::class.java,
            )
            .widgetDataReader()
        val snapshots = runCatching { reader.snapshotRecentEpisodes(LIMIT) }
            .onFailure { Timber.e(it, "snapshotRecentEpisodes failed") }
            .getOrDefault(emptyList())
        val artworks = loadArtworks(context, snapshots)

        provideContent {
            EpisodiveGlanceTheme {
                RecentEpisodesContent(snapshots, artworks)
            }
        }
    }

    /**
     * 썸네일을 `async` 로 병렬 로드해 cold-start 렌더 지연을 줄인다.
     */
    private suspend fun loadArtworks(
        context: Context,
        snapshots: List<EpisodeSnapshot>,
    ): Map<Long, Bitmap?> = coroutineScope {
        snapshots
            .map { snapshot ->
                async {
                    snapshot.id to WidgetImageLoader.loadWidgetBitmap(
                        context,
                        snapshot.imageUrl,
                        ARTWORK_PX,
                    )
                }
            }
            .awaitAll()
            .toMap()
    }

    private companion object {
        const val LIMIT = 5
        const val ARTWORK_PX = 128
    }
}
