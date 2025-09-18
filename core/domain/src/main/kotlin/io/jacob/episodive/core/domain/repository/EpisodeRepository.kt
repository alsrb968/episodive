package io.jacob.episodive.core.domain.repository

import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.Episode
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

interface EpisodeRepository {
    suspend fun searchEpisodesByPerson(
        person: String,
        max: Int? = null,
    ): Flow<List<Episode>>

    suspend fun getEpisodesByFeedId(
        feedId: Long,
        max: Int? = null,
        since: Instant? = null,
    ): Flow<List<Episode>>

    suspend fun getEpisodesByFeedUrl(
        feedUrl: String,
        max: Int? = null,
        since: Instant? = null,
    ): Flow<List<Episode>>

    suspend fun getEpisodesByPodcastGuid(
        guid: String,
        max: Int? = null,
        since: Instant? = null,
    ): Flow<List<Episode>>

    suspend fun getEpisodeById(id: Long): Flow<Episode>

    suspend fun getLiveEpisodes(max: Int? = null): Flow<List<Episode>>

    suspend fun getRandomEpisodes(
        max: Int? = null,
        language: String? = null,
        includeCategories: List<Category> = emptyList(),
        excludeCategories: List<Category> = emptyList(),
    ): Flow<List<Episode>>

    suspend fun getRecentEpisodes(
        max: Int? = null,
        excludeString: String? = null,
    ): Flow<List<Episode>>
}