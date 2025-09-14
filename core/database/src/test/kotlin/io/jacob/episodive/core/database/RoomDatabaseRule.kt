package io.jacob.episodive.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import kotlin.jvm.java

class RoomDatabaseRule : TestWatcher() {
    lateinit var db: PodcastIndexDatabase
        private set

    override fun starting(description: Description) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, PodcastIndexDatabase::class.java)
            .build()
    }

    override fun finished(description: Description) {
        db.close()
    }
}