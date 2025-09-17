package io.jacob.episodive.core.database.datasource

import androidx.paging.PagingSource
import androidx.room.withTransaction
import io.jacob.episodive.core.database.EpisodiveDatabase
import io.jacob.episodive.core.database.dao.EpisodeDao
import io.jacob.episodive.core.database.model.EpisodeEntity
import io.jacob.episodive.core.database.model.LikedEpisodeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.time.Instant

class EpisodeLocalDataSourceImpl @Inject constructor(
    private val database: EpisodiveDatabase,
    private val episodeDao: EpisodeDao,
) : EpisodeLocalDataSource {
    override suspend fun upsertEpisode(episode: EpisodeEntity) {
        episodeDao.upsertEpisode(episode)
    }

    override suspend fun upsertEpisodes(episodes: List<EpisodeEntity>) {
        episodeDao.upsertEpisodes(episodes)
    }

    override fun getEpisode(id: Long): Flow<EpisodeEntity?> {
        return episodeDao.getEpisode(id)
    }

    override fun getEpisodes(): Flow<List<EpisodeEntity>> {
        return episodeDao.getEpisodes()
    }

    override fun getEpisodesPaging(): PagingSource<Int, EpisodeEntity> {
        return episodeDao.getEpisodesPaging()
    }

    override fun getLikedEpisodes(): Flow<List<EpisodeEntity>> {
        return episodeDao.getLikedEpisodes()
    }

    override fun isLiked(id: Long): Flow<Boolean> {
        return episodeDao.isLiked(id)
    }

    override suspend fun toggleLike(id: Long, likedAt: Instant) {
        val isLiked = episodeDao.isLiked(id).first()
        database.withTransaction {
            if (isLiked) {
                episodeDao.removeLike(id)
            } else {
                episodeDao.addLike(LikedEpisodeEntity(id, likedAt))
            }
        }
    }

    override suspend fun deleteEpisode(id: Long) {
        episodeDao.deleteEpisode(id)
    }

    override suspend fun deleteEpisodes() {
        episodeDao.deleteEpisodes()
    }

    override fun getEpisodeCount(): Flow<Int> {
        return episodeDao.getEpisodeCount()
    }

    override fun getLikedEpisodeCount(): Flow<Int> {
        return episodeDao.getLikedEpisodeCount()
    }
}