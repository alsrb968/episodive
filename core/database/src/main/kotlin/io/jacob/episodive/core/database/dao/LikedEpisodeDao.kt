package io.jacob.episodive.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.jacob.episodive.core.database.model.LikedEpisodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LikedEpisodeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addLike(likedEpisode: LikedEpisodeEntity)

    @Query("DELETE FROM liked_episodes WHERE id = :id")
    suspend fun removeLike(id: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM liked_episodes WHERE id = :id)")
    fun isLiked(id: Long): Flow<Boolean>

    @Query("SELECT id FROM liked_episodes ORDER BY likedAt DESC")
    fun getLikedEpisodeIds(): Flow<List<Long>>

    @Query("SELECT COUNT(*) FROM liked_episodes")
    fun getLikedCount(): Flow<Int>
}