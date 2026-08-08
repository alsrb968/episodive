package io.jacob.episodive.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import io.jacob.episodive.core.model.Category
import kotlin.time.Instant

/**
 * 원격 피드 목록의 캐시. 사용자 데이터가 아니라 순수 캐시라 언제 비워도 안전하다.
 *
 * PK 가 `(id, groupKey)` 복합키인 이유: 같은 피드가 여러 목록(추천·트렌딩·최신)에 동시에
 * 들어간다. `id` 단독 PK 였을 때는 나중에 쓴 그룹이 앞선 그룹의 행을 덮어써 **그룹끼리 서로의
 * 항목을 빼앗았다** — 트렌딩 전체 목록을 채우면 추천 목록에 구멍이 났다. 그래서 피드 목록을
 * 미리 받아 두고 페이지 단위로만 상세를 채우는 방식(`FeedWindowPodcastPagingSource`)을
 * 추천 하나에만 쓸 수 있었다.
 */
@Entity(tableName = "feeds", primaryKeys = ["id", "groupKey"])
data class FeedEntity(
    val id: Long,
    val url: String,
    val title: String,
    val newestItemPublishTime: Instant,
    val description: String? = null,
    val image: String? = null,
    val itunesId: Long? = null,
    val language: String,
    val categories: List<Category> = emptyList(),
    val groupKey: String,
    /**
     * 그룹 안에서의 순위. 원격이 준 순서를 그대로 담는다.
     *
     * 페이징이 `LIMIT`/`OFFSET` 으로 창을 자르므로 정렬이 정해져 있지 않으면 같은 항목이 두
     * 페이지에 나오거나 빠질 수 있다. 정렬 기준을 이 컬럼에 명시해 실행 계획과 무관하게 만든다.
     */
    @ColumnInfo(defaultValue = "0") val sortOrder: Int = 0,
    val cachedAt: Instant,
)
