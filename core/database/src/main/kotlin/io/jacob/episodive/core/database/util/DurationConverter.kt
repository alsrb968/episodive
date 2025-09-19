package io.jacob.episodive.core.database.util

import androidx.room.TypeConverter
import io.jacob.episodive.core.model.mapper.toDurationSeconds
import io.jacob.episodive.core.model.mapper.toIntSeconds
import kotlin.time.Duration

class DurationConverter {
    @TypeConverter
    fun fromDuration(duration: Duration?): Int? =
        duration?.toIntSeconds()

    @TypeConverter
    fun toDuration(seconds: Int?): Duration? =
        seconds?.toDurationSeconds()
}