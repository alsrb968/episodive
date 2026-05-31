package io.jacob.episodive.core.domain.widget

/**
 * 위젯 "나의 최신 피드" 그리드용 경량 팟캐스트 스냅샷.
 */
data class PodcastSnapshot(
    val id: Long,
    val title: String,
    val imageUrl: String?,
)
