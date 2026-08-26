package io.jacob.episodive.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import io.jacob.episodive.core.database.model.FollowedPodcastEntity
import io.jacob.episodive.core.database.model.GroupKeyWithCount
import io.jacob.episodive.core.database.model.PodcastEntity
import io.jacob.episodive.core.database.model.PodcastGroupEntity
import io.jacob.episodive.core.database.model.PodcastWithExtrasView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlin.time.Clock
import kotlin.time.Instant

@Dao
interface PodcastDao {
    companion object {
        private const val FTS_SEARCH_CONDITION = """
            (:query IS NULL OR id IN (SELECT rowid FROM podcasts_fts WHERE podcasts_fts MATCH :query))
        """
    }


    /** PODCASTS **/

    @Upsert
    suspend fun upsertPodcast(podcast: PodcastEntity)

    @Upsert
    suspend fun upsertPodcasts(podcasts: List<PodcastEntity>)

    @Upsert
    suspend fun upsertPodcastGroup(podcastGroup: PodcastGroupEntity)

    @Upsert
    suspend fun upsertPodcastGroups(podcastGroups: List<PodcastGroupEntity>)

    @Transaction
    suspend fun upsertPodcastsWithGroup(podcasts: List<PodcastEntity>, groupKey: String) {
        upsertPodcasts(podcasts)

        val createdAt = Clock.System.now()
        val groups = podcasts.mapIndexed { order, podcast ->
            PodcastGroupEntity(
                groupKey = groupKey,
                id = podcast.id,
                order = order,
                createdAt = createdAt,
            )
        }
        upsertPodcastGroups(groups)
    }

    @Query("DELETE FROM podcasts WHERE id = :id")
    suspend fun deletePodcast(id: Long)

    @Query("DELETE FROM podcasts")
    suspend fun deletePodcasts()

    @Query(
        """
        DELETE FROM podcasts
        WHERE id IN (:ids)
          AND NOT EXISTS (SELECT 1 FROM followed_podcasts WHERE followed_podcasts.id = podcasts.id)
          AND NOT EXISTS (SELECT 1 FROM podcast_group WHERE podcast_group.id = podcasts.id)
    """
    )
    suspend fun deletePodcastsIfOrphaned(ids: List<Long>)

    @Query("DELETE FROM podcast_group WHERE groupKey = :groupKey")
    suspend fun deletePodcastGroupsByGroupKey(groupKey: String)

    @Query("SELECT * FROM podcast_with_extras WHERE id = :id")
    fun getPodcastById(id: Long): Flow<PodcastWithExtrasView?>

    @Query("SELECT * FROM podcast_with_extras WHERE id IN (:ids)")
    fun getPodcastsByIds(ids: List<Long>): Flow<List<PodcastWithExtrasView>>

    @Query("SELECT * FROM podcast_with_extras WHERE id IN (:ids)")
    suspend fun getPodcastsByIdsOnce(ids: List<Long>): List<PodcastWithExtrasView>

    @Query(
        """
        SELECT * FROM podcast_with_extras
        WHERE $FTS_SEARCH_CONDITION
        ORDER BY lastUpdateTime DESC, id DESC
        LIMIT :limit
    """
    )
    fun getPodcasts(query: String? = null, limit: Int): Flow<List<PodcastWithExtrasView>>

    @Query(
        """
        SELECT * FROM podcast_with_extras
        WHERE $FTS_SEARCH_CONDITION
        ORDER BY lastUpdateTime DESC, id DESC
    """
    )
    fun getPodcastsPaging(query: String? = null): PagingSource<Int, PodcastWithExtrasView>

    @Query(
        """
        SELECT * FROM podcast_with_extras
        WHERE id IN (SELECT id FROM podcast_group WHERE groupKey = :groupKey)
        ORDER BY lastUpdateTime DESC, id DESC
        LIMIT :limit
    """
    )
    fun getPodcastsByGroupKey(groupKey: String, limit: Int): Flow<List<PodcastWithExtrasView>>

    @Query(
        """
        SELECT * FROM podcast_with_extras
        WHERE id IN (SELECT id FROM podcast_group WHERE groupKey = :groupKey)
        ORDER BY lastUpdateTime DESC, id DESC
    """
    )
    fun getPodcastsByGroupKeyPaging(groupKey: String): PagingSource<Int, PodcastWithExtrasView>


    /** PODCAST GROUPS **/

    @Query("SELECT * FROM podcast_group WHERE groupKey = :groupKey")
    suspend fun getPodcastGroupsByGroupKey(groupKey: String): List<PodcastGroupEntity>

    @Query("SELECT MIN(createdAt) FROM podcast_group WHERE groupKey = :groupKey")
    suspend fun getOldestCreatedAtByGroupKey(groupKey: String): Instant?

    @Query(
        """
        SELECT COUNT(*)
        FROM podcast_group
        WHERE :prefix IS NULL OR groupKey LIKE :prefix || '%'
    """
    )
    suspend fun getPodcastGroupCount(prefix: String? = null): Int

    @Query(
        """
        SELECT groupKey, COUNT(*) as count
        FROM podcast_group
        WHERE :prefix IS NULL OR groupKey LIKE :prefix || '%'
        GROUP BY groupKey
        ORDER BY MIN(createdAt) ASC
    """
    )
    suspend fun getGroupKeysWithCounts(prefix: String? = null): List<GroupKeyWithCount>

    @Query("SELECT id FROM podcast_group WHERE groupKey IN (:groupKeys)")
    suspend fun getPodcastIdsByGroupKeys(groupKeys: List<String>): List<Long>

    @Query("DELETE FROM podcast_group WHERE groupKey IN (:groupKeys)")
    suspend fun deletePodcastGroupsByGroupKeys(groupKeys: List<String>)

    @Transaction
    suspend fun deleteOldestGroupsIfExceedsLimit(
        threshold: Int,
        targetCount: Int = threshold,
        prefix: String? = null,
    ) {
        val totalCount = getPodcastGroupCount(prefix)
        if (totalCount <= threshold) return

        val groupKeysWithCounts = getGroupKeysWithCounts(prefix)

        var currentCount = totalCount
        val groupKeysToDelete = mutableListOf<String>()

        for ((groupKey, count) in groupKeysWithCounts) {
            if (currentCount <= targetCount) break
            groupKeysToDelete.add(groupKey)
            currentCount -= count
        }

        if (groupKeysToDelete.isEmpty()) return

        val ids = getPodcastIdsByGroupKeys(groupKeysToDelete)
        deletePodcastGroupsByGroupKeys(groupKeysToDelete)
        deletePodcastsIfOrphaned(ids)
    }

    @Transaction
    suspend fun replacePodcasts(podcasts: List<PodcastEntity>, groupKey: String) {
        val oldPodcastIds = getPodcastGroupsByGroupKey(groupKey).map { it.id }
        deletePodcastGroupsByGroupKey(groupKey)
        upsertPodcastsWithGroup(podcasts, groupKey)
        deletePodcastsIfOrphaned(oldPodcastIds)

        val prefix = groupKey.split(":").first()
        deleteOldestGroupsIfExceedsLimit(threshold = 1_000, targetCount = 800, prefix = prefix)
    }


    /** FOLLOWED PODCASTS **/

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFollowedPodcast(followedPodcast: FollowedPodcastEntity)

    @Query("DELETE FROM followed_podcasts WHERE id = :id")
    suspend fun removeFollowedPodcast(id: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM followed_podcasts WHERE id = :id)")
    fun isFollowedPodcast(id: Long): Flow<Boolean>

    @Transaction
    suspend fun toggleFollowedPodcast(id: Long): Boolean {
        return if (isFollowedPodcast(id).first()) {
            removeFollowedPodcast(id)
            deletePodcastsIfOrphaned(listOf(id))
            false
        } else {
            addFollowedPodcast(
                FollowedPodcastEntity(
                    id = id,
                    followedAt = Clock.System.now(),
                    isNotificationEnabled = false
                )
            )
            true
        }
    }

    @Query(
        """
        SELECT * FROM podcast_with_extras
        WHERE followedAt IS NOT NULL
        AND $FTS_SEARCH_CONDITION
        ORDER BY followedAt DESC, id DESC
        LIMIT :limit
    """
    )
    fun getFollowedPodcasts(query: String? = null, limit: Int): Flow<List<PodcastWithExtrasView>>

    @Query(
        """
        SELECT * FROM podcast_with_extras
        WHERE followedAt IS NOT NULL
        AND $FTS_SEARCH_CONDITION
        ORDER BY followedAt DESC, id DESC
    """
    )
    fun getFollowedPodcastsPaging(query: String? = null): PagingSource<Int, PodcastWithExtrasView>

    @Query("SELECT id, followedAt FROM followed_podcasts")
    suspend fun getFollowedPodcastsToSync(): Map<@MapColumn(columnName = "id") Long, @MapColumn(columnName = "followedAt") Instant>

    // OPML 내보내기는 전량이 필요한 one-shot 조회다. FTS_SEARCH_CONDITION 은 검색어가 없는
    // 자리라 빼고, LIMIT 도 두지 않는다 — 잘리면 사용자가 모르는 채로 일부만 담긴 파일이 나간다.
    @Query(
        """
        SELECT * FROM podcast_with_extras
        WHERE followedAt IS NOT NULL
        ORDER BY followedAt DESC, id DESC
    """
    )
    suspend fun getFollowedPodcastsOnce(): List<PodcastWithExtrasView>

    // OPML 가져오기는 "새로 추가됐는지" 를 알아야 진행 상황을 셀 수 있다. addFollowedPodcast
    // 의 onConflict=IGNORE 는 반환값 없이 조용히 무시하므로 그것만으로는 "이미 있었다" 를
    // 구분할 수 없다 — 확인과 삽입을 한 트랜잭션으로 묶어 직접 판정한다.
    @Transaction
    suspend fun followPodcast(id: Long): Boolean {
        if (isFollowedPodcast(id).first()) return false

        addFollowedPodcast(
            FollowedPodcastEntity(
                id = id,
                followedAt = Clock.System.now(),
                isNotificationEnabled = false
            )
        )
        return true
    }
}