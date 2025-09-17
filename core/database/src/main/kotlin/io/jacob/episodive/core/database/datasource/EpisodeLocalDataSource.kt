package io.jacob.episodive.core.database.datasource

import androidx.paging.PagingSource
import io.jacob.episodive.core.database.model.EpisodeEntity
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock
import kotlin.time.Instant

interface EpisodeLocalDataSource {
    suspend fun upsertEpisode(episode: EpisodeEntity)
    suspend fun upsertEpisodes(episodes: List<EpisodeEntity>)
    fun getEpisode(id: Long): Flow<EpisodeEntity?>
    fun getEpisodes(): Flow<List<EpisodeEntity>>
    fun getEpisodesPaging(): PagingSource<Int, EpisodeEntity>
    fun getLikedEpisodes(): Flow<List<EpisodeEntity>>
    fun isLiked(id: Long): Flow<Boolean>
    suspend fun toggleLike(id: Long, likedAt: Instant = Clock.System.now())
    suspend fun deleteEpisode(id: Long)
    suspend fun deleteEpisodes()
    fun getEpisodeCount(): Flow<Int>
    fun getLikedEpisodeCount(): Flow<Int>
}