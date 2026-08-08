package io.jacob.episodive.core.database.migration

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import io.jacob.episodive.core.database.EpisodiveDatabase
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

/**
 * v11 → v12 마이그레이션 검증.
 *
 * 이 마이그레이션은 이 앱에서 처음으로 **테이블을 다시 만든다**(`feeds` 의 기본키 변경). 컬럼
 * 추가와 달리 DDL 을 손으로 적어야 하고, 한 글자만 어긋나도 업그레이드한 기기에서 앱이 열리다
 * 죽는다 — Room 이 여는 순간 스키마를 검증하기 때문이다.
 *
 * 그 검증을 그대로 이용한다. v11 스키마로 DB 파일을 만들어 두고 [EpisodiveDatabase] 를 열면,
 * Room 이 마이그레이션을 돌린 뒤 결과가 v12 정의와 같은지 직접 확인한다. 어긋나면 이 테스트가
 * 예외로 죽는다. 스키마 정의는 Room 이 내보낸 `schemas/…/11.json` 을 읽어 쓰므로, 사람이 옮겨
 * 적다가 틀릴 여지가 없다.
 */
@RunWith(RobolectricTestRunner::class)
class Migration11to12Test {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    @After
    fun deleteTestDatabase() {
        context.deleteDatabase(TEST_DB)
    }

    @Test
    fun `Given a v11 database, When opened at v12, Then the schema Room expects is produced`() {
        createV11DatabaseWithOneFeedAndOneSoundbite()

        val db = openAtCurrentVersion()

        try {
            // 열리는 것 자체가 스키마 검증 통과다. 이어서 옮겨온 데이터를 확인한다.
            val feeds = db.feedDao().getFeeds(groupKey = "trending", limit = 10)
            assertEquals(1, feeds.size)
            assertEquals(11L, feeds.first().id)
            assertEquals("Feed 11", feeds.first().title)
            // 옛 행에는 순위 정보가 없다. 기본값 0 이 들어가고, 쿼리의 동점 처리가 순서를 잡는다.
            assertEquals(0, feeds.first().sortOrder)
        } finally {
            db.close()
        }
    }

    @Test
    fun `Given a migrated database, When the same feed is stored in another group, Then both survive`() = runTest {
        // 복합키가 실제로 적용됐는지 본다. DDL 만 맞고 PRIMARY KEY 절이 빠지면 위 테스트는
        // 통과할 수 있어도(컬럼 구성은 같으므로) 이 테스트는 통과할 수 없다.
        createV11DatabaseWithOneFeedAndOneSoundbite()

        val db = openAtCurrentVersion()

        try {
            val dao = db.feedDao()
            val migrated = dao.getFeeds(groupKey = "trending", limit = 10).first()
            dao.upsertFeeds(listOf(migrated.copy(groupKey = "recommended")))

            assertEquals(1, dao.getFeeds(groupKey = "trending", limit = 10).size)
            assertEquals(1, dao.getFeeds(groupKey = "recommended", limit = 10).size)
        } finally {
            db.close()
        }
    }

    private fun openAtCurrentVersion(): EpisodiveDatabase =
        Room.databaseBuilder(context, EpisodiveDatabase::class.java, TEST_DB)
            .addMigrations(Migration11to12)
            .allowMainThreadQueries()
            .build()

    private fun createV11DatabaseWithOneFeedAndOneSoundbite() {
        createDatabaseAt(version = 11) { db ->
            db.execSQL(
                "INSERT INTO `feeds` (`id`, `url`, `title`, `newestItemPublishTime`, " +
                    "`description`, `image`, `itunesId`, `language`, `categories`, `groupKey`, " +
                    "`cachedAt`) VALUES (11, 'https://example.com/11', 'Feed 11', 2000, " +
                    "NULL, NULL, NULL, 'en', '', 'trending', 1000)"
            )
            db.execSQL(
                "INSERT INTO `soundbites` (`enclosureUrl`, `title`, `startTime`, `duration`, " +
                    "`episodeId`, `episodeTitle`, `feedTitle`, `feedUrl`, `feedId`, `cachedAt`) " +
                    "VALUES ('https://example.com/clip', 'Clip', 0, 30, 11, 'Episode', " +
                    "'Feed 11', 'https://example.com/11', 11, 1000)"
            )
        }
    }

    /**
     * Room 이 내보낸 스키마 정의대로 [version] 시점의 DB 파일을 만든다.
     *
     * `MigrationTestHelper` 를 쓰지 않는 이유는 그쪽이 스키마를 계측 테스트의 asset 에서 찾기
     * 때문이다. 이 모듈의 테스트는 Robolectric 로컬 테스트라 파일에서 직접 읽는 편이 단순하다.
     */
    private fun createDatabaseAt(version: Int, populate: (SupportSQLiteDatabase) -> Unit) {
        val schema = JSONObject(schemaFile(version).readText()).getJSONObject("database")

        val callback = object : SupportSQLiteOpenHelper.Callback(version) {
            override fun onCreate(db: SupportSQLiteDatabase) {
                val entities = schema.getJSONArray("entities")
                for (i in 0 until entities.length()) {
                    val entity = entities.getJSONObject(i)
                    val tableName = entity.getString("tableName")
                    db.execSQL(entity.getString("createSql").withTableName(tableName))

                    entity.optJSONArray("indices")?.let { indices ->
                        for (j in 0 until indices.length()) {
                            db.execSQL(
                                indices.getJSONObject(j).getString("createSql")
                                    .withTableName(tableName)
                            )
                        }
                    }
                    entity.optJSONArray("contentSyncTriggers")?.let { triggers ->
                        for (j in 0 until triggers.length()) {
                            db.execSQL(triggers.getString(j))
                        }
                    }
                }

                val views = schema.optJSONArray("views")
                for (i in 0 until (views?.length() ?: 0)) {
                    val view = views!!.getJSONObject(i)
                    db.execSQL(
                        view.getString("createSql")
                            .replace("\${VIEW_NAME}", view.getString("viewName"))
                    )
                }

                // room_master_table 과 신원 해시. 이게 없으면 Room 이 "데이터 무결성을 확인할 수
                // 없다"며 마이그레이션을 시작하지도 않는다.
                val setupQueries = schema.getJSONArray("setupQueries")
                for (i in 0 until setupQueries.length()) {
                    db.execSQL(setupQueries.getString(i))
                }

                populate(db)
            }

            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) =
                Unit
        }

        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(TEST_DB)
                .callback(callback)
                .build()
        )
        helper.writableDatabase.close()
    }

    private fun String.withTableName(tableName: String) =
        replace("\${TABLE_NAME}", tableName)

    private fun schemaFile(version: Int) = File(
        "schemas/io.jacob.episodive.core.database.EpisodiveDatabase/$version.json"
    ).also {
        check(it.exists()) { "스키마 파일을 찾지 못했다: ${it.absolutePath}" }
    }

    companion object {
        private const val TEST_DB = "migration-test.db"
    }
}
