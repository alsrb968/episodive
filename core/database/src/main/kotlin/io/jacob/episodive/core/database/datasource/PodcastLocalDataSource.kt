package io.jacob.episodive.core.database.datasource

import androidx.paging.PagingSource
import io.jacob.episodive.core.database.model.PodcastEntity
import kotlinx.coroutines.flow.Flow

interface PodcastLocalDataSource {
    suspend fun upsertPodcast(podcast: PodcastEntity)
    suspend fun upsertPodcasts(podcasts: List<PodcastEntity>)
    fun getPodcast(id: Long): Flow<PodcastEntity?>
    fun getPodcasts(): Flow<List<PodcastEntity>>
    fun getPodcastsPaging(): PagingSource<Int, PodcastEntity>
    suspend fun deletePodcast(id: Long)
    suspend fun deletePodcasts()
}