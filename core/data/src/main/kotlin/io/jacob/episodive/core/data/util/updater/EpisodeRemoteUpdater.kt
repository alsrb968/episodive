package io.jacob.episodive.core.data.util.updater

import androidx.paging.PagingSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.jacob.episodive.core.data.util.query.EpisodeQuery
import io.jacob.episodive.core.database.datasource.EpisodeLocalDataSource
import io.jacob.episodive.core.database.datasource.SoundbiteLocalDataSource
import io.jacob.episodive.core.database.mapper.toEpisodeEntities
import io.jacob.episodive.core.database.model.EpisodeEntity
import io.jacob.episodive.core.database.model.EpisodeWithExtrasView
import io.jacob.episodive.core.model.mapper.toCommaString
import io.jacob.episodive.core.network.datasource.EpisodeRemoteDataSource
import io.jacob.episodive.core.network.datasource.SoundbiteRemoteDataSource
import io.jacob.episodive.core.network.mapper.toEpisodes
import io.jacob.episodive.core.network.model.EpisodeResponse
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

class EpisodeRemoteUpdater @AssistedInject constructor(
    private val episodeLocal: EpisodeLocalDataSource,
    private val episodeRemote: EpisodeRemoteDataSource,
    private val soundbiteLocal: SoundbiteLocalDataSource,
    private val soundbiteRemote: SoundbiteRemoteDataSource,
    @Assisted("query") override val query: EpisodeQuery,
    backgroundRefresher: BackgroundRefresher,
) : RemoteUpdater<EpisodeQuery, EpisodeResponse, EpisodeEntity, EpisodeWithExtrasView>(query, backgroundRefresher) {

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("query") query: EpisodeQuery): EpisodeRemoteUpdater
    }

    override suspend fun fetchFromRemote(fetchSize: Int): List<EpisodeResponse> {
        return when (query) {
            is EpisodeQuery.Person -> episodeRemote.searchEpisodesByPerson(
                person = query.person,
                max = fetchSize,
            )

            is EpisodeQuery.FeedId -> episodeRemote.getEpisodesByFeedId(
                feedId = query.feedId,
                max = fetchSize,
            )

            is EpisodeQuery.FeedUrl -> episodeRemote.getEpisodesByFeedUrl(
                feedUrl = query.feedUrl,
                max = fetchSize,
            )

            is EpisodeQuery.PodcastGuid -> episodeRemote.getEpisodesByPodcastGuid(
                guid = query.podcastGuid,
                max = fetchSize,
            )

            is EpisodeQuery.Live -> episodeRemote.getLiveEpisodes(max = query.max)
            is EpisodeQuery.Random -> episodeRemote.getRandomEpisodes(
                max = query.max,
                language = query.language,
                // 빈 목록은 빈 문자열이 아니라 null 로 보낸다. Retrofit 은 @Query 가 null 일
                // 때만 파라미터를 빼므로, 그냥 두면 `cat=` 가 붙어 나간다. 지금 원격은 빈
                // 값을 "필터 없음" 으로 받아 주지만(실측), 그 관대함에 기대면 서버가 판정을
                // 파라미터 존재 여부로 바꾸는 순간 조용히 느린 경로로 돌아간다.
                includeCategories = query.categories.toCommaString().ifEmpty { null },
            )

            is EpisodeQuery.Recent -> episodeRemote.getRecentEpisodes(max = query.max)
        }
    }

    override suspend fun convertToEntity(responses: List<EpisodeResponse>): List<EpisodeEntity> {
        return responses.toEpisodes().toEpisodeEntities()
    }

    override suspend fun replaceToLocal(entities: List<EpisodeEntity>) {
        episodeLocal.replaceEpisodes(entities, query.key)
    }

    override suspend fun getOldestCachedAt(): Instant? =
        episodeLocal.getOldestCreatedAtByGroupKey(query.key)

    override fun getPagingSource(): PagingSource<Int, EpisodeWithExtrasView> {
        return episodeLocal.getEpisodesByGroupKeyPaging(query.key)
    }

    override fun getFlowSource(count: Int): Flow<List<EpisodeWithExtrasView>> {
        return episodeLocal.getEpisodesByGroupKey(query.key, count)
    }
}