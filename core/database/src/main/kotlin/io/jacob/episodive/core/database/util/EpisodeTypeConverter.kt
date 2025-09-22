package io.jacob.episodive.core.database.util

import androidx.room.TypeConverter
import io.jacob.episodive.core.model.EpisodeType
import io.jacob.episodive.core.model.mapper.toEpisodeType
import io.jacob.episodive.core.model.mapper.toValue

class EpisodeTypeConverter {
    @TypeConverter
    fun fromEpisodeType(episodeType: EpisodeType?): String? =
        episodeType?.toValue()

    @TypeConverter
    fun toEpisodeType(name: String?): EpisodeType? =
        name?.toEpisodeType()
}