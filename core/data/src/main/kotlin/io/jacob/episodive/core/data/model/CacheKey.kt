package io.jacob.episodive.core.data.model

sealed interface CacheKey {
    val key: String

    data class Person(val person: String) : CacheKey {
        override val key = "person:$person"
    }

    data class FeedId(val feedId: Long) : CacheKey {
        override val key = "feedId:$feedId"
    }

    data class FeedUrl(val feedUrl: String) : CacheKey {
        override val key = "feedUrl:$feedUrl"
    }

    data class PodcastGuid(val guid: String) : CacheKey {
        override val key = "podcastGuid:$guid"
    }
}