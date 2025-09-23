package io.jacob.episodive.core.domain.repository

import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.LikedEpisode
import io.jacob.episodive.core.model.PlayedEpisode
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration
import kotlin.time.Instant

interface EpisodeRepository {
    fun searchEpisodesByPerson(
        person: String,
        max: Int? = null,
    ): Flow<List<Episode>>

    fun getEpisodesByFeedId(
        feedId: Long,
        max: Int? = null,
        since: Instant? = null,
    ): Flow<List<Episode>>

    fun getEpisodesByFeedUrl(
        feedUrl: String,
        max: Int? = null,
        since: Instant? = null,
    ): Flow<List<Episode>>

    fun getEpisodesByPodcastGuid(
        guid: String,
        max: Int? = null,
        since: Instant? = null,
    ): Flow<List<Episode>>

    fun getEpisodeById(id: Long): Flow<Episode?>

    fun getLiveEpisodes(max: Int? = null): Flow<List<Episode>>

    fun getRandomEpisodes(
        max: Int? = null,
        language: String? = null,
        includeCategories: List<Category> = emptyList(),
        excludeCategories: List<Category> = emptyList(),
    ): Flow<List<Episode>>

    fun getRecentEpisodes(
        max: Int? = null,
        excludeString: String? = null,
    ): Flow<List<Episode>>

    fun getLikedEpisodes(query: String? = null): Flow<List<LikedEpisode>>

    fun getPlayingEpisodes(query: String? = null): Flow<List<PlayedEpisode>>

    fun getPlayedEpisodes(query: String? = null): Flow<List<PlayedEpisode>>

    suspend fun toggleLiked(id: Long): Boolean

    suspend fun updatePlayed(id: Long, position: Duration, isCompleted: Boolean)
}