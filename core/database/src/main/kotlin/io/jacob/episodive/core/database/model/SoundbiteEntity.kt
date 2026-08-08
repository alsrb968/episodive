package io.jacob.episodive.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Duration
import kotlin.time.Instant

@Entity(tableName = "soundbites")
data class SoundbiteEntity(
    val enclosureUrl: String,
    val title: String,
    val startTime: Instant,
    val duration: Duration,
    @PrimaryKey val episodeId: Long,
    val episodeTitle: String,
    val feedTitle: String,
    val feedUrl: String,
    val feedId: Long,
    /**
     * 원격이 준 순위. 이 컬럼이 없을 때는 `episodeId` 로 정렬할 수밖에 없었고, 그건 사실상
     * 아이디 오름차순이라 서버가 매긴 순서를 통째로 버리는 것이었다. 캐시를 채울 때 응답
     * 순서를 그대로 박아 둔다.
     *
     * 기존 행에는 기본값 0 이 들어간다. 전부 같은 값이면 정렬이 다시 불안정해지므로 쿼리는
     * `episodeId` 를 동점 처리로 함께 쓴다 — 그 경우 예전과 똑같은 순서가 나온다.
     */
    @ColumnInfo(defaultValue = "0") val sortOrder: Int = 0,
    val cachedAt: Instant,
)
