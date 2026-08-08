package io.jacob.episodive.core.database.dao

import app.cash.turbine.test
import io.jacob.episodive.core.database.RoomDatabaseRule
import io.jacob.episodive.core.database.mapper.toSoundbiteEntities
import io.jacob.episodive.core.testing.model.soundbiteTestDataList
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import io.jacob.episodive.core.testing.util.loadAsSnapshot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SoundbiteDaoTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val dbRule = RoomDatabaseRule()

    private lateinit var dao: SoundbiteDao

    @Before
    fun setup() {
        dao = dbRule.db.soundbiteDao()
    }

    private val soundbiteEntities = soundbiteTestDataList.toSoundbiteEntities()

    @Test
    fun `Given soundbites, When upsertSoundbites, Then upserted correctly`() =
        runTest {
            // Given
            dao.upsertSoundbites(soundbiteEntities)

            // When
            dao.getSoundbites(10).test {
                val items = awaitItem()
                // Then
                assertEquals(items.size, soundbiteEntities.size)
                cancel()
            }

            // When
            dao.deleteSoundbite(soundbiteEntities.first().episodeId)
            dao.getSoundbites(10).test {
                val items = awaitItem()
                // Then
                assertEquals(items.size, soundbiteEntities.size - 1)
                cancel()
            }

            // When
            dao.deleteSoundbites()
            dao.getSoundbites(10).test {
                val items = awaitItem()
                // Then
                assertEquals(items.size, 0)
                cancel()
            }
        }

    @Test
    fun `Given some soundbites, When deleteSoundbites, Then deleted correctly`() =
        runTest {
            // Given
            dao.upsertSoundbites(soundbiteEntities)

            // When
            dao.deleteSoundbites()
            dao.getSoundbites(10).test {
                val items = awaitItem()
                // Then
                assertEquals(items.size, 0)
                cancel()
            }
        }

    @Test
    fun `Given soundbites with different cache keys, When replaceSoundbites, Then replaced by cache key`() =
        runTest {
            // Given - Insert initial soundbites
            val initialSoundbites = soundbiteEntities.take(3)
            dao.upsertSoundbites(initialSoundbites)

            // When - Replace with new soundbites
            val newSoundbites = listOf(
                soundbiteEntities[5].copy(episodeId = 400L),
                soundbiteEntities[6].copy(episodeId = 401L)
            )
            dao.replaceSoundbites(newSoundbites)

            // Then
            dao.getSoundbites(10).test {
                val items = awaitItem()
                assertEquals(2, items.size)
                assertTrue(items.any { it.episodeId == 400L })
                assertTrue(items.any { it.episodeId == 401L })
                cancel()
            }
        }

    @Test
    fun `Given soundbites, When getSoundbitesPaging is called, Then soundbites are returned`() =
        runTest {
            // Given
            dao.upsertSoundbites(soundbiteEntities)

            // When
            val soundbites = dao.getSoundbitesPaging().loadAsSnapshot()

            // Then
            assertEquals(10, soundbites.size)
        }

    @Test
    fun `Given multiple soundbites with same cache key, When getSoundbitesOldestCachedAt is called, Then oldest cachedAt is returned`() =
        runTest {
            // Given
            dao.upsertSoundbites(soundbiteEntities)

            // When
            val oldestCachedAt = dao.getSoundbitesOldestCachedAt()

            // Then
            val expectedOldest = soundbiteEntities.minByOrNull { it.cachedAt }?.cachedAt
            assertEquals(expectedOldest?.epochSeconds, oldestCachedAt?.epochSeconds)
        }

    @Test
    fun `Given soundbites, When getSoundbitesPagingList, Then returns correct list`() = runTest {
        // Given
        dao.upsertSoundbites(soundbiteEntities)

        // When
        val result = dao.getSoundbitesPagingList(offset = 0, limit = 5)

        // Then
        assertEquals(5, result.size)
    }

    @Test
    fun `Given soundbites, When paged twice, Then pages partition the set in a fixed order`() =
        runTest {
            // LIMIT/OFFSET 페이징은 정렬이 정해져야만 성립한다. 여기서 고정하는 것은
            // "episodeId 오름차순"이다.
            //
            // 다만 이 테스트로 잡히는 것과 아닌 것을 구분해 둔다. 정렬 컬럼을 바꾸면
            // (cachedAt, title 등) 잡힌다. 반면 ORDER BY 를 통째로 지워도 통과한다 —
            // episodeId 가 INTEGER PRIMARY KEY, 즉 rowid 별칭이라 전체 스캔이 마침 같은
            // 순서를 내주기 때문이다(실측). DAO 쪽 정렬 명시는 그 우연에 기대지 않으려는
            // 것이고, 그 부분은 테스트로 증명할 수 없다.
            // Given
            dao.upsertSoundbites(soundbiteEntities)
            val pageSize = 4

            // When
            val first = dao.getSoundbitesPagingList(offset = 0, limit = pageSize)
            val second = dao.getSoundbitesPagingList(offset = pageSize, limit = pageSize)
            val third = dao.getSoundbitesPagingList(offset = pageSize * 2, limit = pageSize)
            val paged = first + second + third

            // Then
            assertEquals(
                soundbiteEntities.map { it.episodeId }.sorted(),
                paged.map { it.episodeId },
            )
        }
}
