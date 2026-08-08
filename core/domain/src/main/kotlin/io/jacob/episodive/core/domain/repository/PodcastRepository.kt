package io.jacob.episodive.core.domain.repository

import androidx.paging.PagingData
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.Channel
import io.jacob.episodive.core.model.Podcast
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

interface PodcastRepository {
    fun searchPodcasts(
        query: String,
        max: Int,
    ): Flow<List<Podcast>>

    fun getPodcastByFeedId(feedId: Long): Flow<Podcast?>

    fun getPodcastByFeedUrl(feedUrl: String): Flow<Podcast?>

    fun getPodcastByGuid(guid: String): Flow<Podcast?>

    fun getPodcastsByMedium(
        medium: String,
        max: Int,
    ): Flow<List<Podcast>>

    fun getPodcastsByMediumPaging(medium: String): Flow<PagingData<Podcast>>

    fun getPodcastsByChannel(channel: Channel): Flow<List<Podcast>>

    fun getPodcastsByChannelPaging(channel: Channel): Flow<PagingData<Podcast>>

    fun getTrendingPodcasts(
        max: Int,
        language: String? = null,
        includeCategories: List<Category> = emptyList(),
    ): Flow<List<Podcast>>

    /**
     * 같은 조건의 트렌딩을 전체 목록용으로 받아 페이징한다.
     *
     * 미리보기와 별도 캐시 그룹을 쓴다. 한 그룹을 공유하면 먼저 캐시를 채운 쪽의 개수에
     * 갇히므로, 여기서 받은 [max] 가 그대로 전체 목록의 상한이 된다.
     */
    fun getTrendingPodcastsPaging(
        max: Int,
        language: String? = null,
        includeCategories: List<Category> = emptyList(),
    ): Flow<PagingData<Podcast>>

    fun getRecentPodcasts(
        max: Int,
        language: String? = null,
        includeCategories: List<Category> = emptyList(),
    ): Flow<List<Podcast>>

    /** 최근 발행 팟캐스트의 전체 목록판. 캐시 분리는 [getTrendingPodcastsPaging] 과 같다. */
    fun getRecentPodcastsPaging(
        max: Int,
        language: String? = null,
        includeCategories: List<Category> = emptyList(),
    ): Flow<PagingData<Podcast>>

    fun getRecentNewPodcasts(
        max: Int,
    ): Flow<List<Podcast>>

    fun getRecommendedPodcasts(
        max: Int,
        language: String? = null,
        includeCategories: List<Category> = emptyList(),
    ): Flow<List<Podcast>>

    fun getRecommendedPodcastsPaging(
        max: Int,
        language: String? = null,
        includeCategories: List<Category> = emptyList(),
    ): Flow<PagingData<Podcast>>

    fun getFollowedPodcasts(
        query: String? = null,
        max: Int,
    ): Flow<List<Podcast>>

    fun getFollowedPodcastsPaging(query: String? = null): Flow<PagingData<Podcast>>

    suspend fun toggleFollowed(id: Long): Boolean

    suspend fun getFollowedPodcastsToSync(): Map<Long, Instant>
}