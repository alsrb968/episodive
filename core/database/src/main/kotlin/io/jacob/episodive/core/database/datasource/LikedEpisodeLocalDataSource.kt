package io.jacob.episodive.core.database.datasource

import io.jacob.episodive.core.database.model.LikedEpisodeEntity
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock
import kotlin.time.Instant

interface LikedEpisodeLocalDataSource {
    suspend fun addLike(likedEpisode: LikedEpisodeEntity)
    suspend fun removeLike(id: Long)
    fun isLiked(id: Long): Flow<Boolean>
    fun getLikedEpisodeIds(): Flow<List<Long>>
    fun getLikedCount(): Flow<Int>
    suspend fun toggleLike(id: Long, likedAt: Instant = Clock.System.now())
}