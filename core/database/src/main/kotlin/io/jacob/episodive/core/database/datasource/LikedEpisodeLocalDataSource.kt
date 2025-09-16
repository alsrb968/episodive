package io.jacob.episodive.core.database.datasource

import kotlinx.coroutines.flow.Flow

interface LikedEpisodeLocalDataSource {
    fun isLiked(id: Long): Flow<Boolean>
    fun getLikedEpisodeIds(): Flow<List<Long>>
    fun getLikedCount(): Flow<Int>
    suspend fun toggleLike(id: Long)
}