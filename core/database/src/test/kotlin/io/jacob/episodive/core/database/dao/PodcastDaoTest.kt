package io.jacob.episodive.core.database.dao

import io.jacob.episodive.core.database.RoomDatabaseRule
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PodcastDaoTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val dbRule = RoomDatabaseRule()

    private lateinit var dao: PodcastDao

    @Before
    fun setup() {
        dao = dbRule.db.podcastDao()
    }
}