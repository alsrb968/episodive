package io.jacob.episodive.core.data.util.query

import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

sealed interface PodcastQuery : CacheableQuery {

    data class Search(val query: String) : PodcastQuery {
        override val key = "search:$query"
        override val timeToLive = 30.minutes
    }

    data class Medium(val medium: String) : PodcastQuery {
        override val key = "medium:$medium"
        override val timeToLive = 1.hours
    }
}