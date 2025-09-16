package io.jacob.episodive.core.database.di

import android.content.Context
import androidx.room.Room
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
        ).build()
    }
}