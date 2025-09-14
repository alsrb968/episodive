package io.jacob.episodive.core.database.util

import androidx.room.TypeConverter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class DurationConverter {
    @TypeConverter
    fun fromDuration(duration: Duration?): Long? =
        duration?.inWholeSeconds

    @TypeConverter
    fun toDuration(seconds: Long?): Duration? =
        seconds?.seconds
}