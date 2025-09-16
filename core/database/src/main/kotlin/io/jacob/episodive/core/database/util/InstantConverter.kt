package io.jacob.episodive.core.database.util

import androidx.room.TypeConverter
import io.jacob.episodive.core.database.mapper.toInstant
import io.jacob.episodive.core.database.mapper.toLong
import kotlin.time.Instant

class InstantConverter {
    @TypeConverter
    fun fromInstant(instant: Instant?): Long? = instant?.toLong()

    @TypeConverter
    fun toInstant(epochSeconds: Long?): Instant? = epochSeconds?.toInstant()
}