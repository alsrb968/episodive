package io.jacob.episodive.core.database.util

import androidx.room.TypeConverter
import io.jacob.episodive.core.model.mapper.toInstant
import io.jacob.episodive.core.model.mapper.toSeconds
import kotlin.time.Instant

class InstantConverter {
    @TypeConverter
    fun fromInstant(instant: Instant?): Long? = instant?.toSeconds()

    @TypeConverter
    fun toInstant(epochSeconds: Long?): Instant? = epochSeconds?.toInstant()
}