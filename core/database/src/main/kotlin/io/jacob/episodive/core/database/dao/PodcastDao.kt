package io.jacob.episodive.core.database.dao

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

    @Query("SELECT * FROM podcasts WHERE id = :id")
    fun getPodcast(id: Long): Flow<PodcastEntity?>

    @Query("SELECT * FROM podcasts")
    fun getPodcasts(): Flow<List<PodcastEntity>>

    @Query("DELETE FROM podcasts WHERE id = :id")
    suspend fun deletePodcast(id: Long)

    @Query("DELETE FROM podcasts")
    suspend fun deletePodcasts()
}