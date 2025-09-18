package io.jacob.episodive.core.database.datasource

import io.jacob.episodive.core.database.dao.PodcastDao
import io.jacob.episodive.core.database.model.PodcastEntity
import kotlinx.coroutines.flow.Flow

class PodcastLocalDataSourceImpl(
    private val podcastDao: PodcastDao,
) : PodcastLocalDataSource {
    override suspend fun upsertPodcast(podcast: PodcastEntity) {
        podcastDao.upsertPodcast(podcast)
    }

    override suspend fun upsertPodcasts(podcasts: List<PodcastEntity>) {
        podcastDao.upsertPodcasts(podcasts)
    }

    override fun getPodcast(id: Long): Flow<PodcastEntity?> {
        return podcastDao.getPodcast(id)
    }

    override fun getPodcasts(): Flow<List<PodcastEntity>> {
        return podcastDao.getPodcasts()
    }

    override suspend fun deletePodcast(id: Long) {
        podcastDao.deletePodcast(id)
    }

    override suspend fun deletePodcasts() {
        podcastDao.deletePodcasts()
    }
}