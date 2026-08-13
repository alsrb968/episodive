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
    // 기준은 sortOrder, 즉 원격이 준 순위다. episodeId 는 동점 처리로만 쓴다 — 옛 행은
    // sortOrder 가 전부 0 이라(마이그레이션 기본값) 이 컬럼만으로는 순서가 정해지지 않는다.
    // 길이가 0 이하인 행은 내보내지 않는다. 피드가 그런 사운드바이트를 주는 일이 있는데,
    // 그대로 목록에 오르면 시작=끝인 창이 올라가 재생하자마자 끝나고, 그 완료가 다음 클립으로
    // 넘기는 것을 연쇄시켜 목록을 소리 없이 훑고 지나간다. 재생할 수 없는 항목이므로 애초에
    // 목록에서 뺀다. (캐시에 이미 들어온 옛 행도 이 조건으로 함께 걸러진다.)
    @Query("SELECT * FROM soundbites WHERE duration > 0 ORDER BY sortOrder, episodeId LIMIT :limit")
    fun getSoundbites(limit: Int): Flow<List<SoundbiteEntity>>

    @Query("SELECT * FROM soundbites WHERE duration > 0 ORDER BY sortOrder, episodeId")
    fun getSoundbitesPaging(): PagingSource<Int, SoundbiteEntity>

    @Query("SELECT * FROM soundbites WHERE duration > 0 ORDER BY sortOrder, episodeId LIMIT :limit OFFSET :offset")
    suspend fun getSoundbitesPagingList(offset: Int, limit: Int): List<SoundbiteEntity>

    @Query("SELECT MIN(cachedAt) FROM soundbites")
    suspend fun getSoundbitesOldestCachedAt(): Instant?
}