package io.jacob.episodive.core.data.util.paging

import androidx.paging.PagingConfig

object PagingDefaults {
    val DEFAULT_CONFIG = PagingConfig(
        pageSize = 20,
        prefetchDistance = 5,
        enablePlaceholders = false,
    )

    /**
     * [FeedWindowPodcastPagingSource] 전용 설정.
     *
     * `initialLoadSize` 를 페이지 크기와 같게 못 박는 것이 핵심이다. Paging 의 기본값은
     * 페이지 크기의 3배라, 첫 화면이 화면에 보이지도 않는 60개의 상세 요청을 기다리게 된다 —
     * 이 PagingSource 를 만든 이유가 바로 그 대기를 없애는 것이다.
     */
    val FEED_WINDOW_CONFIG = PagingConfig(
        pageSize = 10,
        prefetchDistance = 5,
        initialLoadSize = 10,
        enablePlaceholders = false,
    )
}
