package io.jacob.episodive.core.domain.repository

import androidx.paging.PagingData
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.Chapter
import io.jacob.episodive.core.model.Episode
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration
import kotlin.time.Instant

interface EpisodeRepository {
    suspend fun upsertEpisode(episode: Episode)

    fun searchEpisodesByPerson(
        person: String,
        max: Int,
    ): Flow<List<Episode>>

    fun searchEpisodesByPersonPaging(
        person: String,
    ): Flow<PagingData<Episode>>

    fun getEpisodesByFeedId(
        feedId: Long,
        max: Int,
    ): Flow<List<Episode>>

    fun getEpisodesByFeedIdPaging(
        feedId: Long,
    ): Flow<PagingData<Episode>>

    fun getEpisodesByFeedUrl(
        feedUrl: String,
        max: Int,
    ): Flow<List<Episode>>

    fun getEpisodesByFeedUrlPaging(
        feedUrl: String,
    ): Flow<PagingData<Episode>>

    fun getEpisodesByPodcastGuid(
        guid: String,
        max: Int,
    ): Flow<List<Episode>>

    fun getEpisodesByPodcastGuidPaging(
        guid: String,
    ): Flow<PagingData<Episode>>

    fun getLiveEpisodes(max: Int): Flow<List<Episode>>

    /**
     * 라이브 에피소드를 전체 목록용으로 받아 페이징한다.
     *
     * 미리보기와 별도 캐시 그룹을 쓴다. 한 그룹을 공유하면 먼저 캐시를 채운 쪽의 개수에
     * 갇히므로, 여기서 받은 [max] 가 그대로 전체 목록의 상한이 된다.
     */
    fun getLiveEpisodesPaging(max: Int): Flow<PagingData<Episode>>

    fun getRandomEpisodes(
        max: Int,
        language: String? = null,
        includeCategories: List<Category> = emptyList(),
    ): Flow<List<Episode>>

    /** 랜덤 에피소드의 전체 목록판. 캐시 분리는 [getLiveEpisodesPaging] 과 같다. */
    fun getRandomEpisodesPaging(
        max: Int,
        language: String? = null,
        includeCategories: List<Category> = emptyList(),
    ): Flow<PagingData<Episode>>

    fun getRecentEpisodes(max: Int): Flow<List<Episode>>

    fun getSoundbiteEpisodesPaging(max: Int): Flow<PagingData<Episode>>

    fun getEpisodeById(id: Long): Flow<Episode?>

    fun getEpisodesByIds(ids: List<Long>): Flow<List<Episode>>

    fun getLikedEpisodes(query: String? = null, max: Int): Flow<List<Episode>>

    fun getLikedEpisodesPaging(query: String? = null): Flow<PagingData<Episode>>

    fun getPlayedEpisodes(
        isCompleted: Boolean? = null,
        query: String? = null,
        max: Int,
    ): Flow<List<Episode>>

    fun getPlayedEpisodesPaging(
        isCompleted: Boolean? = null,
        query: String? = null,
    ): Flow<PagingData<Episode>>

    fun isLikedEpisode(episode: Episode): Flow<Boolean>

    suspend fun toggleLikedEpisode(episode: Episode): Boolean

    suspend fun updatePlayed(id: Long, position: Duration, isCompleted: Boolean)

    suspend fun updateEpisodeDuration(id: Long, duration: Duration)

    suspend fun replaceEpisodes(episodes: List<Episode>, groupKey: String)

    suspend fun fetchChapters(url: String): List<Chapter>

    suspend fun getEpisodesByGroupKey(groupKey: String): List<Episode>

    fun getSavedEpisodes(query: String? = null, max: Int): Flow<List<Episode>>

    fun getSavedEpisodesPaging(query: String? = null): Flow<PagingData<Episode>>

    suspend fun toggleSavedEpisode(episode: Episode): Boolean

    suspend fun removeSavedEpisode(id: Long)

    suspend fun getLatestEpisodeDatePublished(feedId: Long): Instant?

    suspend fun fetchAndSaveNewEpisodes(feedId: Long, since: Instant): List<Episode>
}
