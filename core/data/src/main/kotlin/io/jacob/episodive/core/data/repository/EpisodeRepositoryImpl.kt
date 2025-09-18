package io.jacob.episodive.core.data.repository

import io.jacob.episodive.core.data.model.CacheKey
import io.jacob.episodive.core.database.datasource.EpisodeLocalDataSource
import io.jacob.episodive.core.database.mapper.toEpisodeEntities
import io.jacob.episodive.core.database.mapper.toEpisodes
import io.jacob.episodive.core.data.model.isExpired
import io.jacob.episodive.core.domain.repository.EpisodeRepository
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.network.datasource.EpisodeRemoteDataSource
import io.jacob.episodive.core.network.mapper.toCommaString
import io.jacob.episodive.core.network.mapper.toEpisode
import io.jacob.episodive.core.network.mapper.toEpisodes
import io.jacob.episodive.core.network.mapper.toLong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class EpisodeRepositoryImpl @Inject constructor(
    private val localDataSource: EpisodeLocalDataSource,
    private val remoteDataSource: EpisodeRemoteDataSource,
) : EpisodeRepository {
    override suspend fun searchEpisodesByPerson(
        person: String,
        max: Int?
    ): Flow<List<Episode>> = flow {
        val key = CacheKey.Person(person).key

        val cachedEpisodes = localDataSource.getEpisodesByCacheKey(key).first()
        if (cachedEpisodes.isNotEmpty() && !cachedEpisodes.isExpired(30.minutes)) {
            emit(cachedEpisodes.toEpisodes())
        } else {
            try {
                val episodes = remoteDataSource.searchEpisodesByPerson(
                    person = person,
                    max = max,
                ).toEpisodes()

                emit(episodes)
                localDataSource.upsertEpisodes(episodes.toEpisodeEntities(cacheKey = key))
            } catch (e: Exception) {
                if (cachedEpisodes.isNotEmpty()) {
                    emit(cachedEpisodes.toEpisodes())
                } else {
                    throw e
                }
            }
        }
    }

    override suspend fun getEpisodesByFeedId(
        feedId: Long,
        max: Int?,
        since: Instant?
    ): Flow<List<Episode>> = flow {
        val key = CacheKey.FeedId(feedId).key

        val cachedEpisodes = localDataSource.getEpisodesByCacheKey(key).first()
        if (cachedEpisodes.isNotEmpty() && !cachedEpisodes.isExpired()) {
            emit(cachedEpisodes.toEpisodes())
        } else {
            try {
                val (live, normal) = remoteDataSource.getEpisodesByFeedId(
                    feedId = feedId,
                    max = max,
                    since = since?.toLong(),
                )
                val episodes = (live?.plus(normal) ?: normal).toEpisodes()

                emit(episodes)
                localDataSource.upsertEpisodes(episodes.toEpisodeEntities(cacheKey = key))
            } catch (e: Exception) {
                if (cachedEpisodes.isNotEmpty()) {
                    emit(cachedEpisodes.toEpisodes())
                } else {
                    throw e
                }
            }
        }
    }

    override suspend fun getEpisodesByFeedUrl(
        feedUrl: String,
        max: Int?,
        since: Instant?
    ): Flow<List<Episode>> = flow {
        val key = CacheKey.FeedUrl(feedUrl).key

        val cachedEpisodes = localDataSource.getEpisodesByCacheKey(key).first()
        if (cachedEpisodes.isNotEmpty() && !cachedEpisodes.isExpired()) {
            emit(cachedEpisodes.toEpisodes())
        } else {
            try {
                val episodes = remoteDataSource.getEpisodesByFeedUrl(
                    feedUrl = feedUrl,
                    max = max,
                    since = since?.toLong(),
                ).toEpisodes()

                emit(episodes)
                localDataSource.upsertEpisodes(episodes.toEpisodeEntities(cacheKey = key))
            } catch (e: Exception) {
                if (cachedEpisodes.isNotEmpty()) {
                    emit(cachedEpisodes.toEpisodes())
                } else {
                    throw e
                }
            }
        }
    }

    override suspend fun getEpisodesByPodcastGuid(
        guid: String,
        max: Int?,
        since: Instant?
    ): Flow<List<Episode>> = flow {
        val key = CacheKey.PodcastGuid(guid).key

        val cachedEpisodes = localDataSource.getEpisodesByCacheKey(key).first()
        if (cachedEpisodes.isNotEmpty() && !cachedEpisodes.isExpired()) {
            emit(cachedEpisodes.toEpisodes())
        } else {
            try {
                val episodes = remoteDataSource.getEpisodesByPodcastGuid(
                    guid = guid,
                    max = max,
                    since = since?.toLong(),
                ).toEpisodes()

                emit(episodes)
                localDataSource.upsertEpisodes(episodes.toEpisodeEntities(cacheKey = key))
            } catch (e: Exception) {
                if (cachedEpisodes.isNotEmpty()) {
                    emit(cachedEpisodes.toEpisodes())
                } else {
                    throw e
                }
            }
        }
    }

    override suspend fun getEpisodeById(id: Long): Flow<Episode> = flow {
        val episode = remoteDataSource.getEpisodeById(
            id = id
        )?.toEpisode()

        if (episode != null) {
            emit(episode)
        } else {
            throw NoSuchElementException("No episode found with id: $id")
        }
    }

    override suspend fun getLiveEpisodes(max: Int?): Flow<List<Episode>> = flow {
        val episodes = remoteDataSource.getLiveEpisodes(
            max = max
        ).toEpisodes()

        emit(episodes)
    }

    override suspend fun getRandomEpisodes(
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

    override suspend fun getRecentEpisodes(
        max: Int?,
        excludeString: String?
    ): Flow<List<Episode>> = flow {
        val episodes = remoteDataSource.getRecentEpisodes(
            max = max,
            excludeString = excludeString
        ).toEpisodes()

        emit(episodes)
    }
}