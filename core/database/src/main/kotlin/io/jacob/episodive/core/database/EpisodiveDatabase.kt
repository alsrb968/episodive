package io.jacob.episodive.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.jacob.episodive.core.database.dao.EpisodeDao
import io.jacob.episodive.core.database.dao.LikedEpisodeDao
import io.jacob.episodive.core.database.dao.PodcastDao
import io.jacob.episodive.core.database.model.EpisodeEntity
import io.jacob.episodive.core.database.model.LikedEpisodeEntity
import io.jacob.episodive.core.database.model.PodcastEntity
import io.jacob.episodive.core.database.util.CategoryConverter
import io.jacob.episodive.core.database.util.DurationConverter
import io.jacob.episodive.core.database.util.EpisodeTypeConverter
import io.jacob.episodive.core.database.util.InstantConverter
import io.jacob.episodive.core.database.util.MediumConverter
import io.jacob.episodive.core.database.util.PersonConverter
import io.jacob.episodive.core.database.util.SoundbiteConverter
import io.jacob.episodive.core.database.util.TranscriptConverter

@Database(
    entities = [
        PodcastEntity::class,
        EpisodeEntity::class,
        LikedEpisodeEntity::class,
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(
    CategoryConverter::class,
    DurationConverter::class,
    EpisodeTypeConverter::class,
    InstantConverter::class,
    MediumConverter::class,
    PersonConverter::class,
    SoundbiteConverter::class,
    TranscriptConverter::class,
)
abstract class EpisodiveDatabase : RoomDatabase() {
    abstract fun podcastDao(): PodcastDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun likedEpisodeDao(): LikedEpisodeDao
}