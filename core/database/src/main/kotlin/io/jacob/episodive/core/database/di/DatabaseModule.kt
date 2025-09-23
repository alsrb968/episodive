package io.jacob.episodive.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.jacob.episodive.core.database.EpisodiveDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideEpisodiveDatabase(
        @ApplicationContext context: Context
    ): EpisodiveDatabase {
        return Room.databaseBuilder(
            context,
            EpisodiveDatabase::class.java,
            "episodive-database",
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Podcast 테이블 인덱스
                db.execSQL("CREATE INDEX idx_podcasts_title_nocase ON podcasts (title COLLATE NOCASE)")
                db.execSQL("CREATE INDEX idx_podcasts_description_nocase ON podcasts (description COLLATE NOCASE)")
                db.execSQL("CREATE INDEX idx_podcasts_author_nocase ON podcasts (author COLLATE NOCASE)")
                db.execSQL("CREATE INDEX idx_podcasts_ownerName_nocase ON podcasts (ownerName COLLATE NOCASE)")

                // Episode 테이블 인덱스
                db.execSQL("CREATE INDEX idx_episodes_title_nocase ON episodes (title COLLATE NOCASE)")
                db.execSQL("CREATE INDEX idx_episodes_description_nocase ON episodes (description COLLATE NOCASE)")
                db.execSQL("CREATE INDEX idx_episodes_feedAuthor_nocase ON episodes (feedAuthor COLLATE NOCASE)")
            }
        }).build()
    }
}