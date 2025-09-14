package io.jacob.episodive.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.jacob.episodive.core.database.model.EpisodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodeDao {
    @Upsert
    suspend fun upsertEpisode(episode: EpisodeEntity)

    @Upsert
    suspend fun upsertEpisodes(episodes: List<EpisodeEntity>)

    @Query("SELECT * FROM episodes")
    fun getEpisodes(): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes")
    fun getEpisodesPaging(): PagingSource<Int, EpisodeEntity>

    @Query("DELETE FROM episodes")
    suspend fun deleteEpisodes()
}