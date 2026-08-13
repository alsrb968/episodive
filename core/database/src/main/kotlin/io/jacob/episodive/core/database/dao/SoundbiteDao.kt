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
    // 재생할 수 없는 행은 내보내지 않는다. 피드가 그런 사운드바이트를 주는 일이 있다.
    //  - 길이가 0 이하: 시작=끝인 창이 올라가 재생하자마자 끝나고, 그 완료가 다음 클립으로
    //    넘기는 것을 연쇄시켜 목록을 소리 없이 훑고 지나간다.
    //  - 시작이 음수: media3 의 setStartPositionMs 가 그 자리에서 예외를 던진다. 크래시다.
    // 받아들이는 쪽(SoundbiteEpisodePagingSource)에도 같은 조건이 있다. 그쪽이 새 응답을
    // 막는 앞문이고 여기는 이미 캐시에 들어와 있는 옛 행을 막는 뒷문이라, 둘 다 필요하다.
    // 한때 "앞문에서 걸러 표가 비면 MIN(cachedAt) 이 null 이라 페이지마다 원격을 다시
    // 때린다" 며 앞문을 뺀 적이 있는데 틀린 걱정이었다 — 목록이 비면 넘길 페이지가 없어
    // load 가 다시 불리지 않는다. 오히려 뒷문만 남기면 표는 차 있어 "신선함" 으로 판정되는데
    // 조회는 빈 목록을 주어, 다시 받아올 길 없이 빈 화면에 갇힌다.
    @Query("SELECT * FROM soundbites WHERE duration > 0 AND startTime >= 0 ORDER BY sortOrder, episodeId LIMIT :limit")
    fun getSoundbites(limit: Int): Flow<List<SoundbiteEntity>>

    @Query("SELECT * FROM soundbites WHERE duration > 0 AND startTime >= 0 ORDER BY sortOrder, episodeId")
    fun getSoundbitesPaging(): PagingSource<Int, SoundbiteEntity>

    @Query("SELECT * FROM soundbites WHERE duration > 0 AND startTime >= 0 ORDER BY sortOrder, episodeId LIMIT :limit OFFSET :offset")
    suspend fun getSoundbitesPagingList(offset: Int, limit: Int): List<SoundbiteEntity>

    @Query("SELECT MIN(cachedAt) FROM soundbites")
    suspend fun getSoundbitesOldestCachedAt(): Instant?
}