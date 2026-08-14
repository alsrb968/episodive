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
    // 때린다" 며 앞문을 뺀 적이 있다. **넘기는 쪽(APPEND)에 대해서는 틀린 걱정이다** — 목록이
    // 비면 넘길 페이지가 없어 그쪽으로는 load 가 다시 불리지 않는다. 다만 REFRESH 는 다르다:
    // 좋아요 토글이 liked_episodes 를 무효화하면 그때마다 load 가 다시 불린다(Paging 3.3.6 은
    // 무효화 뒤 LoadType.REFRESH 로 부른다). 캐시가 통째로 재생 불가면 그때마다 원격을 때린다.
    // 그 값을 치르고도 앞문을 두는 이유는 없을 때가 더 나쁘기 때문이다: 뒷문만 남기면 표는
    // 차 있어 "신선함" 으로 판정되는데 조회는 빈 목록을 주어, 다시 받아올 길 없이 빈 화면에
    // 갇힌다. (같은 설명이 SoundbiteEpisodePagingSource 에도 있다 — 둘은 짝이므로 한쪽만
    // 고치지 마라.)
    @Query("SELECT * FROM soundbites WHERE duration > 0 AND startTime >= 0 ORDER BY sortOrder, episodeId LIMIT :limit")
    fun getSoundbites(limit: Int): Flow<List<SoundbiteEntity>>

    @Query("SELECT * FROM soundbites WHERE duration > 0 AND startTime >= 0 ORDER BY sortOrder, episodeId")
    fun getSoundbitesPaging(): PagingSource<Int, SoundbiteEntity>

    @Query("SELECT * FROM soundbites WHERE duration > 0 AND startTime >= 0 ORDER BY sortOrder, episodeId LIMIT :limit OFFSET :offset")
    suspend fun getSoundbitesPagingList(offset: Int, limit: Int): List<SoundbiteEntity>

    // 신선도도 **내보낼 수 있는 행** 만 보고 잰다. 조회 셋과 조건이 어긋나면, 재생 불가 행만
    // 남은 캐시가 "신선함" 으로 판정되는데 목록 조회는 빈 결과를 준다 — 위 주석이 막겠다고 한
    // "표는 차 있는데 화면은 비어 있고 다시 받아올 길도 없는" 상태가 TTL 이 지날 때까지 이어진다.
    // 앞문 필터가 생기기 전에 캐시된 옛 행이 정확히 그 경우다.
    @Query("SELECT MIN(cachedAt) FROM soundbites WHERE duration > 0 AND startTime >= 0")
    suspend fun getSoundbitesOldestCachedAt(): Instant?
}