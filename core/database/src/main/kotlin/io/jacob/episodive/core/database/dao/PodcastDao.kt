package io.jacob.episodive.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.jacob.episodive.core.database.model.PodcastEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PodcastDao {
    @Upsert
    suspend fun upsertPodcast(podcast: PodcastEntity)

    @Upsert
    suspend fun upsertPodcasts(podcasts: List<PodcastEntity>)

    @Query("SELECT * FROM podcasts")
    fun getPodcasts(): Flow<List<PodcastEntity>>

    @Query("SELECT * FROM podcasts")
    fun getPodcastsPaging(): PagingSource<Int, PodcastEntity>

    @Query("DELETE FROM podcasts")
    suspend fun deletePodcasts()
}