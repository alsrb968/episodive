package io.jacob.episodive.core.database.datasource

import androidx.paging.PagingSource
import io.jacob.episodive.core.database.dao.EpisodeDao
import io.jacob.episodive.core.database.model.EpisodeEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EpisodeLocalDataSourceImpl @Inject constructor(
    private val episodeDao: EpisodeDao
) : EpisodeLocalDataSource {
    override suspend fun upsertEpisode(episode: EpisodeEntity) {
        episodeDao.upsertEpisode(episode)
    }

    override suspend fun upsertEpisodes(episodes: List<EpisodeEntity>) {
        episodeDao.upsertEpisodes(episodes)
    }

    override fun getEpisodes(): Flow<List<EpisodeEntity>> {
        return episodeDao.getEpisodes()
    }

    override fun getEpisodesPaging(): PagingSource<Int, EpisodeEntity> {
        return episodeDao.getEpisodesPaging()
    }

    override suspend fun deleteEpisodes() {
        episodeDao.deleteEpisodes()
    }
}