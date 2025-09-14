package io.jacob.episodive.core.database.util

import androidx.room.TypeConverter
import kotlin.time.Instant

class InstantConverter {
    @TypeConverter
    fun fromInstant(instant: Instant?): Long? =
        instant?.epochSeconds

    @TypeConverter
    fun toInstant(epochSeconds: Long?): Instant? =
        epochSeconds?.let { Instant.fromEpochSeconds(it) }
}