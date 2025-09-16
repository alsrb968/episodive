package io.jacob.episodive.core.database.datasource

import io.jacob.episodive.core.database.dao.LikedEpisodeDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LikedEpisodeLocalDataSourceImpl @Inject constructor(
    private val likedEpisodeDao: LikedEpisodeDao
) : LikedEpisodeLocalDataSource {
    override fun isLiked(id: Long): Flow<Boolean> {
        return likedEpisodeDao.isLiked(id)
    }

    override fun getLikedEpisodeIds(): Flow<List<Long>> {
        return likedEpisodeDao.getLikedEpisodeIds()
    }

    override fun getLikedCount(): Flow<Int> {
        return likedEpisodeDao.getLikedCount()
    }

    override suspend fun toggleLike(id: Long) {
        return likedEpisodeDao.toggleLike(id)
    }
}