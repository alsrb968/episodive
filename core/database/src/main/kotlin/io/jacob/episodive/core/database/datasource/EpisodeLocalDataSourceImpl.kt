package io.jacob.episodive.core.database.datasource

import androidx.paging.PagingSource
import androidx.room.Transaction
import io.jacob.episodive.core.database.dao.EpisodeDao
import io.jacob.episodive.core.database.model.EpisodeEntity
import io.jacob.episodive.core.database.model.LikedEpisodeDto
import io.jacob.episodive.core.database.model.LikedEpisodeEntity
import io.jacob.episodive.core.database.model.PlayedEpisodeDto
import io.jacob.episodive.core.database.model.PlayedEpisodeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.time.Instant

class EpisodeLocalDataSourceImpl @Inject constructor(
    private val episodeDao: EpisodeDao,
) : EpisodeLocalDataSource {
    override suspend fun upsertEpisode(episode: EpisodeEntity) {
        episodeDao.upsertEpisode(episode)
    }

    override suspend fun upsertEpisodes(episodes: List<EpisodeEntity>) {
        episodeDao.upsertEpisodes(episodes)
    }

    override suspend fun deleteEpisode(id: Long) {
        episodeDao.deleteEpisode(id)
    }

    override suspend fun deleteEpisodes() {
        episodeDao.deleteEpisodes()
    }

    @Transaction
    override suspend fun toggleLiked(id: Long, likedAt: Instant) {
        val isLiked = episodeDao.isLiked(id).first()
        if (isLiked) {
            episodeDao.removeLiked(id)
        } else {
            episodeDao.addLiked(LikedEpisodeEntity(id, likedAt))
        }
    }

    override suspend fun upsertPlayed(playedEpisode: PlayedEpisodeEntity) {
        episodeDao.upsertPlayed(playedEpisode)
    }

    override suspend fun removePlayed(id: Long) {
        episodeDao.removePlayed(id)
    }

    override fun getEpisode(id: Long): Flow<EpisodeEntity?> {
        return episodeDao.getEpisode(id)
    }

    override fun getEpisodes(): Flow<List<EpisodeEntity>> {
        return episodeDao.getEpisodes()
    }

    override fun getEpisodesPaging(): PagingSource<Int, EpisodeEntity> {
        return episodeDao.getEpisodesPaging()
    }

    override fun getLikedEpisodes(): Flow<List<LikedEpisodeDto>> {
        return episodeDao.getLikedEpisodes()
    }

    override fun getPlayingEpisodes(): Flow<List<PlayedEpisodeDto>> {
        return episodeDao.getPlayingEpisodes()
    }

    override fun getPlayedEpisodes(): Flow<List<PlayedEpisodeDto>> {
        return episodeDao.getPlayedEpisodes()
    }

    override fun isLiked(id: Long): Flow<Boolean> {
        return episodeDao.isLiked(id)
    }

    override fun getEpisodeCount(): Flow<Int> {
        return episodeDao.getEpisodeCount()
    }

    override fun getLikedEpisodeCount(): Flow<Int> {
        return episodeDao.getLikedEpisodeCount()
    }

    override fun getPlayingEpisodeCount(): Flow<Int> {
        return episodeDao.getPlayingEpisodeCount()
    }

    override fun getPlayedEpisodeCount(): Flow<Int> {
        return episodeDao.getPlayedEpisodeCount()
    }
}