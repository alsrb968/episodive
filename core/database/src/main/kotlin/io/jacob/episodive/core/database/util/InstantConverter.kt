package io.jacob.episodive.core.database.util

import androidx.room.TypeConverters
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class InstantConverter {
    @TypeConverters
    fun fromInstant(instant: Instant?): Long? =
        instant?.epochSeconds

    @TypeConverters
    fun toInstant(epochSeconds: Long?): Instant? =
        epochSeconds?.let { Instant.fromEpochSeconds(it) }
}