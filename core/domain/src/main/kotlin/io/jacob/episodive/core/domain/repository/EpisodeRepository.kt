package io.jacob.episodive.core.domain.repository

import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.Episode
import kotlin.time.Instant

interface EpisodeRepository {
    suspend fun searchEpisodesByPerson(
        person: String,
        max: Int? = null,
    ): List<Episode>

    suspend fun getEpisodesByFeedId(
        feedId: Long,
        max: Int? = null,
        since: Instant? = null,
    ): Pair<List<Episode>?, List<Episode>>

    suspend fun getEpisodesByFeedUrl(
        feedUrl: String,
        max: Int? = null,
        since: Instant? = null,
    ): List<Episode>

    suspend fun getEpisodesByPodcastGuid(
        guid: String,
        max: Int? = null,
        since: Instant? = null,
    ): List<Episode>

    suspend fun getEpisodeById(id: Long): Episode?

    suspend fun getLiveEpisodes(max: Int? = null): List<Episode>

    suspend fun getRandomEpisodes(
        max: Int? = null,
        language: String? = null,
        includeCategories: List<Category>? = null,
        excludeCategories: List<Category>? = null,
    ): List<Episode>

    suspend fun getRecentEpisodes(
        max: Int? = null,
        excludeString: String? = null,
    ): List<Episode>
}