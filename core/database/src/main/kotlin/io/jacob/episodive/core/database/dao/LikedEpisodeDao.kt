package io.jacob.episodive.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.jacob.episodive.core.database.mapper.toInstant
import io.jacob.episodive.core.database.model.LikedEpisodeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

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

    @Transaction
    suspend fun toggleLike(id: Long) {
        if (isLiked(id).first()) {
            removeLike(id)
        } else {
            addLike(LikedEpisodeEntity(id, System.currentTimeMillis().toInstant()))
        }
    }
}