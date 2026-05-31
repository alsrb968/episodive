package io.jacob.episodive.core.domain.widget

import kotlinx.coroutines.flow.Flow

interface WidgetDataReader {
    suspend fun snapshotNowPlaying(): NowPlayingSnapshot?

    /**
     * 재생 중 정보를 reactive 하게 방출하는 Flow.
     *
     * Glance 위젯은 `provideContent` 컴포지션 안에서 이 Flow 를 `collectAsState` 로 구독해야
     * 재생 변경 시 갱신된다. (provideGlance 본문은 세션당 1회만 실행되므로 거기서 읽으면 stale.)
     * 재생 위치(progress) 재방출로 인한 폭주를 막기 위해 episodeId + isPlaying 기준으로 dedupe 한다.
     */
    fun nowPlayingFlow(): Flow<NowPlayingSnapshot?>

    suspend fun snapshotRecentEpisodes(limit: Int = 5): List<EpisodeSnapshot>
}
