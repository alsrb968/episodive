package io.jacob.episodive.core.database.datasource

import androidx.paging.PagingSource
import io.jacob.episodive.core.database.model.EpisodeEntity
import kotlinx.coroutines.flow.Flow

interface EpisodeLocalDataSource {
    suspend fun upsertEpisode(episode: EpisodeEntity)
    suspend fun upsertEpisodes(episodes: List<EpisodeEntity>)
    fun getEpisodes(): Flow<List<EpisodeEntity>>
    fun getEpisodesPaging(): PagingSource<Int, EpisodeEntity>
    suspend fun deleteEpisodes()
}