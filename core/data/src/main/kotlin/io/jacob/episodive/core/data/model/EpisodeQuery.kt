package io.jacob.episodive.core.data.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

sealed interface EpisodeQuery {
    val key: String
    val timeToLive: Duration

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

    data class PodcastGuid(val guid: String) : EpisodeQuery {
        override val key = "podcastGuid:$guid"
        override val timeToLive = 24.hours
    }

    object Live : EpisodeQuery {
        override val key = "live"
        override val timeToLive = 2.minutes
    }

    object Recent : EpisodeQuery {
        override val key = "recent"
        override val timeToLive = 10.minutes
    }
}