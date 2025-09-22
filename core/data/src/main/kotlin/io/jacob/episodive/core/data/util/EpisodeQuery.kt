package io.jacob.episodive.core.data.util

import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

sealed interface EpisodeQuery : CacheableQuery {

    data class Person(val person: String) : EpisodeQuery {
        override val key = "person:$person"
        override val timeToLive = 30.minutes
    }

    data class FeedId(val feedId: Long) : EpisodeQuery {
        override val key = "feedId:$feedId"
        override val timeToLive = 24.hours
    }

    data class FeedUrl(val feedUrl: String) : EpisodeQuery {
        override val key = "feedUrl:$feedUrl"
        override val timeToLive = 24.hours
    }

    data class PodcastGuid(val podcastGuid: String) : EpisodeQuery {
        override val key = "podcastGuid:$podcastGuid"
        override val timeToLive = 24.hours
    }

    data object Live : EpisodeQuery {
        override val key = "live"
        override val timeToLive = 2.minutes
    }

    data object Recent : EpisodeQuery {
        override val key = "recent"
        override val timeToLive = 10.minutes
    }
}