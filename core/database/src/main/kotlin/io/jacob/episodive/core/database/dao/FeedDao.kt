package io.jacob.episodive.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import io.jacob.episodive.core.database.model.FeedEntity
import kotlin.time.Instant

@Dao
interface FeedDao {
    @Upsert
    suspend fun upsertFeeds(feeds: List<FeedEntity>)

    @Query("DELETE FROM feeds WHERE groupKey = :groupKey")
    suspend fun deleteFeedsByGroupKey(groupKey: String)

    @Query("DELETE FROM feeds")
    suspend fun deleteFeeds()

    @Transaction
    suspend fun replaceFeedsByGroupKey(feeds: List<FeedEntity>, groupKey: String) {
        deleteFeedsByGroupKey(groupKey)
        upsertFeeds(feeds)
    }

    // 세 쿼리 모두 sortOrder 로 정렬한다. LIMIT/OFFSET 은 정렬이 정해져야 의미가 있고,
    // 정렬 없이 쓰면 페이지마다 SQLite 가 고른 순서에 기대게 된다 — 실행 계획이 바뀌는 순간
    // 같은 피드가 두 페이지에 나오거나 아예 빠진다.
    // id 는 동점 처리다. 마이그레이션으로 넘어온 옛 행은 sortOrder 가 전부 0 이라
    // 이 컬럼만으로는 다시 순서가 정해지지 않는다.
    @Query("SELECT * FROM feeds WHERE groupKey = :groupKey ORDER BY sortOrder, id LIMIT :limit")
    fun getFeeds(groupKey: String, limit: Int): List<FeedEntity>

    @Query("SELECT * FROM feeds WHERE groupKey = :groupKey ORDER BY sortOrder, id")
    fun getFeedsPaging(groupKey: String): PagingSource<Int, FeedEntity>

    @Query(
        "SELECT * FROM feeds WHERE groupKey = :groupKey " +
            "ORDER BY sortOrder, id LIMIT :limit OFFSET :offset"
    )
    suspend fun getFeedsPagingList(groupKey: String, offset: Int, limit: Int): List<FeedEntity>

    @Query("SELECT MIN(cachedAt) FROM feeds WHERE groupKey = :groupKey")
    suspend fun getFeedsOldestCachedAt(groupKey: String): Instant?
}