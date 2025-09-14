package io.jacob.episodive.core.database.util

import androidx.room.TypeConverter
import io.jacob.episodive.core.model.Medium

class MediumConverter {
    @TypeConverter
    fun fromMedium(medium: Medium?): String? =
        medium?.value

    @TypeConverter
    fun toMedium(value: String?): Medium? =
        value?.let { stringValue -> Medium.entries.find { it.value == stringValue } }
}