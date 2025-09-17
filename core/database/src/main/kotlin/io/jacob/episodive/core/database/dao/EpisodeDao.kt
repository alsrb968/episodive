package io.jacob.episodive.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import io.jacob.episodive.core.database.model.EpisodeEntity
import io.jacob.episodive.core.database.model.LikedEpisodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodeDao {
    @Upsert
    suspend fun upsertEpisode(episode: EpisodeEntity)

    @Upsert
    suspend fun upsertEpisodes(episodes: List<EpisodeEntity>)

    @Query("SELECT * FROM episodes WHERE id = :id")
    fun getEpisode(id: Long): Flow<EpisodeEntity?>

    @Query("SELECT * FROM episodes")
    fun getEpisodes(): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes")
    fun getEpisodesPaging(): PagingSource<Int, EpisodeEntity>

    @Query(
        """
        SELECT e.* FROM episodes e
        INNER JOIN liked_episodes le ON e.id = le.id
        ORDER BY le.likedAt DESC
    """
    )
    fun getLikedEpisodes(): Flow<List<EpisodeEntity>>

    @Query("""
        SELECT e.* FROM episodes e
        INNER JOIN resume_episodes re ON e.id = re.id
        ORDER BY re.lastPlayedAt DESC
    """)
    fun getResumeEpisodes(): Flow<List<EpisodeEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addLike(likedEpisode: LikedEpisodeEntity)

    @Query("DELETE FROM liked_episodes WHERE id = :id")
    suspend fun removeLike(id: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM liked_episodes WHERE id = :id)")
    fun isLiked(id: Long): Flow<Boolean>

    @Query("DELETE FROM episodes WHERE id = :id")
    suspend fun deleteEpisode(id: Long)

    @Query("DELETE FROM episodes")
    suspend fun deleteEpisodes()

    @Query("SELECT COUNT(*) FROM episodes")
    fun getEpisodeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM liked_episodes")
    fun getLikedEpisodeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM resume_episodes")
    fun getResumeEpisodeCount(): Flow<Int>
}