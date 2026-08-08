package io.jacob.episodive.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import io.jacob.episodive.core.database.model.SoundbiteEntity
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

@Dao
interface SoundbiteDao {
    @Upsert
    suspend fun upsertSoundbites(soundbites: List<SoundbiteEntity>)

    @Query("DELETE FROM soundbites WHERE episodeId = :episodeId")
    suspend fun deleteSoundbite(episodeId: Long)

    @Query("DELETE FROM soundbites")
    suspend fun deleteSoundbites()

    @Transaction
    suspend fun replaceSoundbites(soundbites: List<SoundbiteEntity>) {
        deleteSoundbites()
        upsertSoundbites(soundbites)
    }

    // ORDER BY 를 명시한다. LIMIT/OFFSET 은 정렬이 정해져야만 의미가 있고, 정렬 없이 쓰면
    // 페이지마다 SQLite 가 고른 순서에 기대게 된다 — 실행 계획이 바뀌는 순간 같은 항목이
    // 두 페이지에 나오거나 아예 빠진다.
    // episodeId 는 INTEGER PRIMARY KEY, 즉 rowid 별칭이라 지금 전체 스캔이 내주던 순서와
    // 같다. 즉 이 변경으로 보이는 순서가 달라지지는 않는다. (원격이 준 순위를 살리려면
    // 순서 컬럼이 따로 필요하다 — 여기서 다루지 않는다.)
    @Query("SELECT * FROM soundbites ORDER BY episodeId LIMIT :limit")
    fun getSoundbites(limit: Int): Flow<List<SoundbiteEntity>>

    @Query("SELECT * FROM soundbites ORDER BY episodeId")
    fun getSoundbitesPaging(): PagingSource<Int, SoundbiteEntity>

    @Query("SELECT * FROM soundbites ORDER BY episodeId LIMIT :limit OFFSET :offset")
    suspend fun getSoundbitesPagingList(offset: Int, limit: Int): List<SoundbiteEntity>

    @Query("SELECT MIN(cachedAt) FROM soundbites")
    suspend fun getSoundbitesOldestCachedAt(): Instant?
}