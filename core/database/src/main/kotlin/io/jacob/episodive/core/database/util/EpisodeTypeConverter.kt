package io.jacob.episodive.core.database.util

import androidx.room.TypeConverter
import io.jacob.episodive.core.model.EpisodeType

class EpisodeTypeConverter {
    @TypeConverter
    fun fromEpisodeType(episodeType: EpisodeType?): String? =
        episodeType?.name

    @TypeConverter
    fun toEpisodeType(name: String?): EpisodeType? =
        name?.let { EpisodeType.valueOf(it) }
}