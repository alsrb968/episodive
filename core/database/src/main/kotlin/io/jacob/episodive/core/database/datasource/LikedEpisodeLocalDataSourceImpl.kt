package io.jacob.episodive.core.database.datasource

import androidx.room.withTransaction
import io.jacob.episodive.core.database.EpisodiveDatabase
import io.jacob.episodive.core.database.dao.LikedEpisodeDao
import io.jacob.episodive.core.database.model.LikedEpisodeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.time.Instant

class LikedEpisodeLocalDataSourceImpl @Inject constructor(
    private val database: EpisodiveDatabase,
    private val likedEpisodeDao: LikedEpisodeDao
) : LikedEpisodeLocalDataSource {
    override suspend fun addLike(likedEpisode: LikedEpisodeEntity) {
        likedEpisodeDao.addLike(likedEpisode)
    }

    override suspend fun removeLike(id: Long) {
        likedEpisodeDao.removeLike(id)
    }

    override fun isLiked(id: Long): Flow<Boolean> {
        return likedEpisodeDao.isLiked(id)
    }

    override fun getLikedEpisodeIds(): Flow<List<Long>> {
        return likedEpisodeDao.getLikedEpisodeIds()
    }

    override fun getLikedCount(): Flow<Int> {
        return likedEpisodeDao.getLikedCount()
    }

    override suspend fun toggleLike(id: Long, likedAt: Instant) {
        database.withTransaction {
            if (isLiked(id).first()) {
                removeLike(id)
            } else {
                addLike(LikedEpisodeEntity(id, likedAt))
            }
        }
    }
}