package io.jacob.episodive.core.data.util.query

import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.GroupKey
import io.jacob.episodive.core.model.mapper.toCommaString
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

sealed interface EpisodeQuery : CacheableQuery {

    data class Person(
        val person: String,
    ) : EpisodeQuery {
        override val key = "${GroupKey.PERSON}:$person"
        override val timeToLive = 30.minutes
    }

    data class FeedId(
        val feedId: Long,
    ) : EpisodeQuery {
        override val key = "${GroupKey.FEED_ID}:$feedId"
        override val timeToLive = 1.days
    }

    data class FeedUrl(
        val feedUrl: String,
    ) : EpisodeQuery {
        override val key = "${GroupKey.FEED_URL}:$feedUrl"
        override val timeToLive = 1.days
    }

    data class PodcastGuid(
        val podcastGuid: String,
    ) : EpisodeQuery {
        override val key = "${GroupKey.PODCAST_GUID}:$podcastGuid"
        override val timeToLive = 1.days
    }

    data class Live(
        val max: Int,
        val scope: QueryScope = QueryScope.PREVIEW,
    ) : EpisodeQuery {
        override val key = "${GroupKey.LIVE}:${scope.value}"
        override val timeToLive = 10.minutes
    }

    data class Random(
        val max: Int,
        val language: String? = null,
        val categories: List<Category> = emptyList(),
        val scope: QueryScope = QueryScope.PREVIEW,
    ) : EpisodeQuery {
        // 언어·카테고리가 키에 들어가야 한다. 이것들은 실제로 원격 요청을 가르는 조건인데
        // (EpisodeRemoteUpdater 의 Random 분기), 키가 그룹 이름 하나로 고정돼 있으면
        // 사용자가 관심 카테고리를 바꿔도 이전 조건으로 받아 둔 캐시를 계속 보게 된다.
        override val key =
            "${GroupKey.RANDOM}:${scope.value}:${language ?: "all"}:${categories.toCommaString()}"
        override val timeToLive = 10.minutes
    }

    data class Recent(val max: Int) : EpisodeQuery {
        override val key = GroupKey.RECENT.toString()
        override val timeToLive = 10.minutes
    }
}