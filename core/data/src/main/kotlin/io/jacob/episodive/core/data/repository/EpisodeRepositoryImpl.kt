package io.jacob.episodive.core.data.repository

import androidx.room.Transaction
import io.jacob.episodive.core.data.util.EpisodeQuery
import io.jacob.episodive.core.data.util.Cacher
import io.jacob.episodive.core.data.util.EpisodeRemoteUpdater
import io.jacob.episodive.core.database.datasource.EpisodeLocalDataSource
import io.jacob.episodive.core.database.mapper.toEpisodes
import io.jacob.episodive.core.database.mapper.toLikedEpisode
import io.jacob.episodive.core.database.mapper.toPlayedEpisode
import io.jacob.episodive.core.database.model.LikedEpisodeEntity
import io.jacob.episodive.core.database.model.PlayedEpisodeEntity
import io.jacob.episodive.core.domain.repository.EpisodeRepository
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.LikedEpisode
import io.jacob.episodive.core.model.PlayedEpisode
import io.jacob.episodive.core.model.mapper.toCommaString
import io.jacob.episodive.core.network.datasource.EpisodeRemoteDataSource
import io.jacob.episodive.core.network.mapper.toEpisode
import io.jacob.episodive.core.network.mapper.toEpisodes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

class EpisodeRepositoryImpl @Inject constructor(
    private val localDataSource: EpisodeLocalDataSource,
    private val remoteDataSource: EpisodeRemoteDataSource,
    private val remoteUpdater: EpisodeRemoteUpdater.Factory,
) : EpisodeRepository {

    override fun searchEpisodesByPerson(
        person: String,
        max: Int?
    ): Flow<List<Episode>> {
        val query = EpisodeQuery.Person(person)

        return Cacher(
            remoteUpdater = remoteUpdater.create(query),
            sourceFactory = {
                localDataSource.getEpisodesByCacheKey(query.key)
            }
        ).flow.map { it.toEpisodes() }
    }

    override fun getEpisodesByFeedId(
        feedId: Long,
        max: Int?,
        since: Instant?
    ): Flow<List<Episode>> {
        val query = EpisodeQuery.FeedId(feedId)

        return Cacher(
            remoteUpdater = remoteUpdater.create(query),
            sourceFactory = {
                localDataSource.getEpisodesByCacheKey(query.key)
            }
        ).flow.map { it.toEpisodes() }
    }

    override fun getEpisodesByFeedUrl(
        feedUrl: String,
        max: Int?,
        since: Instant?
    ): Flow<List<Episode>> {
        val query = EpisodeQuery.FeedUrl(feedUrl)

        return Cacher(
            remoteUpdater = remoteUpdater.create(query),
            sourceFactory = {
                localDataSource.getEpisodesByCacheKey(query.key)
            }
        ).flow.map { it.toEpisodes() }
    }

    override fun getEpisodesByPodcastGuid(
        guid: String,
        max: Int?,
        since: Instant?
    ): Flow<List<Episode>> {
        val query = EpisodeQuery.PodcastGuid(guid)

        return Cacher(
            remoteUpdater = remoteUpdater.create(query),
            sourceFactory = {
                localDataSource.getEpisodesByCacheKey(query.key)
            }
        ).flow.map { it.toEpisodes() }
    }

    override fun getEpisodeById(id: Long): Flow<Episode?> = flow {
        val episode = remoteDataSource.getEpisodeById(
            id = id
        )?.toEpisode()

        emit(episode)
    }

    override fun getLiveEpisodes(max: Int?): Flow<List<Episode>> {
        val query = EpisodeQuery.Live

        return Cacher(
            remoteUpdater = remoteUpdater.create(query),
            sourceFactory = {
                localDataSource.getEpisodesByCacheKey(query.key)
            }
        ).flow.map { it.toEpisodes() }
    }

    override fun getRandomEpisodes(
        max: Int?,
        language: String?,
        includeCategories: List<Category>,
        excludeCategories: List<Category>
    ): Flow<List<Episode>> = flow {
        val episodes = remoteDataSource.getRandomEpisodes(
            max = max,
            language = language,
            includeCategories = includeCategories.toCommaString(),
            excludeCategories = excludeCategories.toCommaString()
        ).toEpisodes()

        emit(episodes)
    }

    override fun getRecentEpisodes(
        max: Int?,
        excludeString: String?
    ): Flow<List<Episode>> {
        val query = EpisodeQuery.Recent

        return Cacher(
            remoteUpdater = remoteUpdater.create(query),
            sourceFactory = {
                localDataSource.getEpisodesByCacheKey(query.key)
            }
        ).flow.map { it.toEpisodes() }
    }

    override fun getLikedEpisodes(): Flow<List<LikedEpisode>> {
        return localDataSource.getLikedEpisodes().map { dtos ->
            dtos.map { it.toLikedEpisode() }
        }
    }

    override fun getPlayingEpisodes(): Flow<List<PlayedEpisode>> {
        return localDataSource.getPlayingEpisodes().map { dtos ->
            dtos.map { it.toPlayedEpisode() }
        }
    }

    override fun getPlayedEpisodes(): Flow<List<PlayedEpisode>> {
        return localDataSource.getPlayedEpisodes().map { dtos ->
            dtos.map { it.toPlayedEpisode() }
        }
    }

    @Transaction
    override suspend fun toggleLiked(id: Long): Boolean {
        return if (localDataSource.isLiked(id).first()) {
            localDataSource.removeLiked(id)
            false
        } else {
            localDataSource.addLiked(
                LikedEpisodeEntity(
                    id = id,
                    likedAt = Clock.System.now()
                )
            )
            true
        }
    }

    override suspend fun updatePlayed(
        id: Long,
        position: Duration,
        isCompleted: Boolean
    ) {
        localDataSource.upsertPlayed(
            PlayedEpisodeEntity(
                id = id,
                playedAt = Clock.System.now(),
                position = position,
                isCompleted = isCompleted,
            )
        )
    }
}