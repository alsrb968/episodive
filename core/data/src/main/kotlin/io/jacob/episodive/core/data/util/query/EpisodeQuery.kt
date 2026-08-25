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
        // 원격 요청을 가르는 조건은 키에도 들어가야 한다(EpisodeRemoteUpdater 의 Random
        // 분기). 키가 그룹 이름 하나로 고정돼 있으면 조건을 바꿔도 이전 조건으로 받아 둔
        // 캐시를 계속 보게 된다.
        //
        // 카테고리는 자리만 남아 있고 지금은 늘 비어 있다 — 홈이 이 조건을 넘기지 않기
        // 때문이다(사유는 GetMyRandomEpisodesUseCase). 원격이 고쳐져 다시 넘기게 되면 키가
        // 저절로 갈라지도록 그대로 둔다. 다만 조건이 바뀌는 그 순간, 옛 조건으로 받아 둔
        // 그룹은 replaceEpisodes 가 자기 키만 지우는 탓에 아무도 치우지 않고 DB 에 남는다.
        override val key =
            "${GroupKey.RANDOM}:${scope.value}:${language ?: "all"}:${categories.toCommaString()}"
        override val timeToLive = 10.minutes
    }

    data class Recent(val max: Int) : EpisodeQuery {
        override val key = GroupKey.RECENT.toString()
        override val timeToLive = 10.minutes
    }
}