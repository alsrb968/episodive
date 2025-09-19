package io.jacob.episodive.core.database.util

import androidx.room.TypeConverter
import io.jacob.episodive.core.model.Medium
import io.jacob.episodive.core.model.mapper.toMedium
import io.jacob.episodive.core.model.mapper.toValue

class MediumConverter {
    @TypeConverter
    fun fromMedium(medium: Medium?): String? =
        medium?.toValue()

    @TypeConverter
    fun toMedium(value: String?): Medium? =
        value?.toMedium()
}