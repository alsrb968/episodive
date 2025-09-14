package io.jacob.episodive.core.domain.repository

import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.Podcast
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
interface PodcastRepository {
    suspend fun searchPodcasts(
        query: String,
        max: Int? = null,
    ): List<Podcast>

    suspend fun getPodcastByFeedId(feedId: Long): Podcast?

    suspend fun getPodcastByFeedUrl(feedUrl: String): Podcast?

    suspend fun getPodcastByGuid(guid: String): Podcast?

    suspend fun getPodcastsByMedium(
        medium: String,
        max: Int? = null,
    ): List<Podcast>

    suspend fun getTrendingPodcasts(
        max: Int? = null,
        since: Instant? = null,
        language: String? = null,
        includeCategories: List<Category>? = null,
        excludeCategories: List<Category>? = null,
    ): List<Podcast>
}