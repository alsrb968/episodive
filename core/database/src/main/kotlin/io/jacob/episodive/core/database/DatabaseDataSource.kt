package io.jacob.episodive.core.database

import androidx.paging.PagingSource
import io.jacob.episodive.core.database.dao.EpisodeDao
import io.jacob.episodive.core.database.dao.PodcastDao
import io.jacob.episodive.core.database.model.EpisodeEntity
import io.jacob.episodive.core.database.model.PodcastEntity
import kotlinx.coroutines.flow.Flow

interface DatabaseDataSource {
    suspend fun upsertPodcast(podcast: PodcastEntity)
    suspend fun upsertPodcasts(podcasts: List<PodcastEntity>)
    fun getPodcasts(): Flow<List<PodcastEntity>>
    fun getPodcastsPaging(): PagingSource<Int, PodcastEntity>
    suspend fun deletePodcasts()

    suspend fun upsertEpisode(episode: EpisodeEntity)
    suspend fun upsertEpisodes(episodes: List<EpisodeEntity>)
    fun getEpisodes(): Flow<List<EpisodeEntity>>
    fun getEpisodesPaging(): PagingSource<Int, EpisodeEntity>
    suspend fun deleteEpisodes()
}

class DatabaseDataSourceImpl(
    private val podcastDao: PodcastDao,
    private val episodeDao: EpisodeDao
) : DatabaseDataSource {
    override suspend fun upsertPodcast(podcast: PodcastEntity) {
        return podcastDao.upsertPodcast(podcast)
    }

    override suspend fun upsertPodcasts(podcasts: List<PodcastEntity>) {
        return podcastDao.upsertPodcasts(podcasts)
    }

    override fun getPodcasts(): Flow<List<PodcastEntity>> {
        return podcastDao.getPodcasts()
    }

    override fun getPodcastsPaging(): PagingSource<Int, PodcastEntity> {
        return podcastDao.getPodcastsPaging()
    }

    override suspend fun deletePodcasts() {
        return podcastDao.deletePodcasts()
    }

    override suspend fun upsertEpisode(episode: EpisodeEntity) {
        return episodeDao.upsertEpisode(episode)
    }

    override suspend fun upsertEpisodes(episodes: List<EpisodeEntity>) {
        return episodeDao.upsertEpisodes(episodes)
    }

    override fun getEpisodes(): Flow<List<EpisodeEntity>> {
        return episodeDao.getEpisodes()
    }

    override fun getEpisodesPaging(): PagingSource<Int, EpisodeEntity> {
        return episodeDao.getEpisodesPaging()
    }

    override suspend fun deleteEpisodes() {
        return episodeDao.deleteEpisodes()
    }
}