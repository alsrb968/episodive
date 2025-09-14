package io.jacob.episodive.core.database.util

import androidx.room.TypeConverters
import io.jacob.episodive.core.model.EpisodeType

class EpisodeTypeConverter {
    @TypeConverters
    fun fromEpisodeType(episodeType: EpisodeType?): String? =
        episodeType?.name

    @TypeConverters
    fun toEpisodeType(name: String?): EpisodeType? =
        name?.let { EpisodeType.valueOf(it) }
}