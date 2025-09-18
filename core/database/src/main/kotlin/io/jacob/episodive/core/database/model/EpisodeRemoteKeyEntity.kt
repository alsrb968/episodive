package io.jacob.episodive.core.database.model

import androidx.room.Entity

@Entity(
    tableName = "episode_remote_keys",
    primaryKeys = ["id", "type"],
)
data class EpisodeRemoteKeyEntity(
    val id: Long,
    val type: String,
)
