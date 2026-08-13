package io.jacob.episodive.core.data.util.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.room.InvalidationTracker
import androidx.room.RoomDatabase
import io.jacob.episodive.core.database.datasource.EpisodeLocalDataSource
import io.jacob.episodive.core.database.datasource.SoundbiteLocalDataSource
import io.jacob.episodive.core.database.mapper.toEpisodeEntity
import io.jacob.episodive.core.database.mapper.toSoundbiteEntities
import io.jacob.episodive.core.database.model.EpisodeWithExtrasView
import io.jacob.episodive.core.model.GroupKey
import io.jacob.episodive.core.network.datasource.EpisodeRemoteDataSource
import io.jacob.episodive.core.network.datasource.SoundbiteRemoteDataSource
import io.jacob.episodive.core.network.mapper.toEpisode
import io.jacob.episodive.core.network.mapper.toSoundbites
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import timber.log.Timber
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class SoundbiteEpisodePagingSource(
    private val database: RoomDatabase,
    private val episodeLocal: EpisodeLocalDataSource,
    private val episodeRemote: EpisodeRemoteDataSource,
    private val soundbiteLocal: SoundbiteLocalDataSource,
    private val soundbiteRemote: SoundbiteRemoteDataSource,
    private val maxSoundbites: Int = 1000,
    private val timeToLive: Duration = 10.minutes,
) : PagingSource<Int, EpisodeWithExtrasView>() {

    private val observer = object : InvalidationTracker.Observer(arrayOf("liked_episodes")) {
        override fun onInvalidated(tables: Set<String>) {
            invalidate()
        }
    }

    init {
        database.invalidationTracker.addObserver(observer)
        registerInvalidatedCallback {
            database.invalidationTracker.removeObserver(observer)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, EpisodeWithExtrasView>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(state.config.pageSize)
                ?: anchorPage?.nextKey?.minus(state.config.pageSize)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, EpisodeWithExtrasView> {
        return try {
            ensureSoundbitesAreFresh()

            val offset = params.key ?: 0
            val limit = params.loadSize
            Timber.w("offset: $offset, limit: $limit")

            val soundbites = soundbiteLocal.getSoundbitesPagingList(
                offset = offset,
                limit = limit
            )

            if (soundbites.isEmpty()) {
                return LoadResult.Page(
                    data = emptyList(),
                    prevKey = if (offset > 0) offset - limit else null,
                    nextKey = null
                )
            }

            val episodeIds = soundbites.map { it.episodeId }

            fetchMissingEpisodes(episodeIds, concurrency = limit)

            val allEpisodes = episodeLocal.getEpisodesByIdsOnce(episodeIds)
            val orderedEpisodes = episodeIds.mapNotNull { id ->
                allEpisodes.find { it.episode.id == id }
            }

            LoadResult.Page(
                data = orderedEpisodes,
                prevKey = if (offset > 0) offset - limit else null,
                nextKey = if (soundbites.size == limit) offset + limit else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private suspend fun ensureSoundbitesAreFresh() {
        val oldestCreatedAt = soundbiteLocal.getSoundbitesOldestCachedAt()
        val isExpired = oldestCreatedAt?.let {
            Clock.System.now() - it > timeToLive
        } ?: true

        if (isExpired) {
            val soundbiteResponses = soundbiteRemote.getSoundbites(max = maxSoundbites)
                .filterNot {
                    val regex = Regex("\\p{InCJK_UNIFIED_IDEOGRAPHS}")

                    it.title.contains(regex) ||
                            it.episodeTitle.contains(regex) ||
                            it.feedTitle.contains(regex)
                }

            val soundbiteEntities = soundbiteResponses
                .toSoundbites()
                .toSoundbiteEntities()
                // 길이가 0 이하인 행은 아예 들이지 않는다. 재생할 수 없는 항목이라 목록에
                // 올라 봐야 시작=끝인 창이 되어 재생하자마자 끝나고, 그 완료가 다음 클립으로
                // 넘기는 것을 연쇄시킨다. 조회 쪽에도 같은 조건이 걸려 있지만(SoundbiteDao)
                // 그쪽은 이미 캐시에 들어온 옛 행을 막는 뒷문이고, 새로 들이는 길은 여기다.
                .filterNot { it.duration <= Duration.ZERO }

            soundbiteLocal.replaceSoundbites(soundbiteEntities)
            // 기존 soundbite 그룹의 episodes와 groups 정리
            episodeLocal.replaceEpisodes(emptyList(), GROUP_KEY_SOUNDBITE)
            Timber.w("replaceSoundbites size: ${soundbiteEntities.size}")
        }
    }

    private suspend fun fetchMissingEpisodes(episodeIds: List<Long>, concurrency: Int) {
        val cachedEpisodes = episodeLocal.getEpisodesByIdsOnce(episodeIds)
        val cachedEpisodeIds = cachedEpisodes.map { it.episode.id }.toSet()
        val missingEpisodeIds = episodeIds.filterNot { it in cachedEpisodeIds }
        Timber.w("cached size: ${cachedEpisodes.size}, missing size: ${missingEpisodeIds.size}")

        if (missingEpisodeIds.isEmpty()) return

        val episodeEntities = missingEpisodeIds
            .withIndex()
            .asFlow()
            .flatMapMerge(concurrency = concurrency) { (index, episodeId) ->
                flow {
                    try {
                        val episodeResponse = episodeRemote.getEpisodeById(episodeId)
                        episodeResponse?.let {
                            val episode = it.toEpisode()
                            val episodeEntity = episode.toEpisodeEntity()
                            emit(index to episodeEntity)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            .toList()
            .sortedBy { it.first }
            .map { it.second }

        if (episodeEntities.isNotEmpty()) {
            Timber.w("add soundbite episodes size: ${episodeEntities.size}")
            episodeLocal.upsertEpisodesWithGroup(
                episodes = episodeEntities,
                groupKey = GROUP_KEY_SOUNDBITE
            )
        }
    }

    companion object {
        private val GROUP_KEY_SOUNDBITE = GroupKey.SOUNDBITE.toString()
    }
}