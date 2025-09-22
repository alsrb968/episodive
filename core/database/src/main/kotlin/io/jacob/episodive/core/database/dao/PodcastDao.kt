package io.jacob.episodive.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import io.jacob.episodive.core.database.model.FollowedPodcastDto
import io.jacob.episodive.core.database.model.FollowedPodcastEntity
import io.jacob.episodive.core.database.model.PodcastEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PodcastDao {
    @Upsert
    suspend fun upsertPodcast(podcast: PodcastEntity)

    @Upsert
    suspend fun upsertPodcasts(podcasts: List<PodcastEntity>)

    @Query("DELETE FROM podcasts WHERE id = :id")
    suspend fun deletePodcast(id: Long)

    @Query("DELETE FROM podcasts")
    suspend fun deletePodcasts()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFollowed(followedPodcastEntity: FollowedPodcastEntity)

    @Query("DELETE FROM followed_podcasts WHERE id = :id")
    suspend fun removeFollowed(id: Long)

    @Query(
        """
        SELECT *
        FROM podcasts
        WHERE id = :id
        ORDER BY cachedAt DESC
        LIMIT 1
    """
    )
    fun getPodcast(id: Long): Flow<PodcastEntity?>

    @Query("SELECT * FROM podcasts")
    fun getPodcasts(): Flow<List<PodcastEntity>>

    @Query("SELECT * FROM podcasts WHERE cacheKey = :cacheKey")
    fun getPodcastsByCacheKey(cacheKey: String): Flow<List<PodcastEntity>>

    @Query(
        """
        SELECT
            p.*,
            fp.followedAt,
            fp.isNotificationEnabled
        FROM followed_podcasts fp
        LEFT JOIN podcasts p ON fp.id = p.id
        WHERE p.cachedAt = (
            SELECT MAX(cachedAt) FROM podcasts WHERE id = fp.id
        )
        ORDER BY fp.followedAt DESC
    """
    )
    fun getFollowedPodcasts(): Flow<List<FollowedPodcastDto>>

    @Query("SELECT EXISTS(SELECT 1 FROM followed_podcasts WHERE id = :id)")
    fun isFollowed(id: Long): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM podcasts")
    fun getPodcastCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM followed_podcasts")
    fun getFollowedPodcastCount(): Flow<Int>
}