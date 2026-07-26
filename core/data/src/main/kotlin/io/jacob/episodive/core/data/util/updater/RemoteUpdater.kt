package io.jacob.episodive.core.data.util.updater

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import io.jacob.episodive.core.data.util.query.CacheableQuery
import io.jacob.episodive.core.model.DataErrorException
import io.jacob.episodive.core.network.util.toDataError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import kotlin.time.Clock
import kotlin.time.Instant

abstract class RemoteUpdater<Query : CacheableQuery, Response, Entity, Output : Any>(
    protected open val query: Query,
) {
    companion object {
        private const val FETCH_SIZE = 1000
    }

    protected abstract suspend fun fetchFromRemote(fetchSize: Int = FETCH_SIZE): List<Response>
    protected abstract suspend fun convertToEntity(responses: List<Response>): List<Entity>
    protected abstract suspend fun replaceToLocal(entities: List<Entity>)

    /**
     * 이 쿼리로 캐시된 것 중 가장 오래된 시각. 캐시가 하나도 없으면 null.
     *
     * 만료 여부(Boolean)가 아니라 시각을 그대로 받는다. "캐시가 없다"와 "캐시가 오래됐다"는
     * 둘 다 갱신 대상이지만 **갱신에 실패했을 때 취할 행동이 정반대**라서, 한쪽으로 뭉뚱그리면
     * 그 판단을 할 수 없다.
     */
    protected abstract suspend fun getOldestCachedAt(): Instant?

    protected abstract fun getPagingSource(): PagingSource<Int, Output>
    protected abstract fun getFlowSource(count: Int): Flow<List<Output>>

    fun getPagingData(pagingConfig: PagingConfig): Flow<PagingData<Output>> {
        return Pager(
            config = pagingConfig,
            pagingSourceFactory = { getPagingSource() }
        ).flow
            .onStart { refreshIfNeeded() }
    }

    fun getFlowList(count: Int): Flow<List<Output>> {
        return getFlowSource(count)
            .onStart { refreshIfNeeded() }
    }

    private suspend fun refreshIfNeeded() {
        val cachedAt = getOldestCachedAt()
        val expired = cachedAt == null || Clock.System.now() - cachedAt > query.timeToLive
        if (!expired) return

        try {
            refresh()
        } catch (e: CancellationException) {
            // 취소는 실패가 아니다. 여기서 삼키면 코루틴 취소가 전파되지 않는다.
            throw e
        } catch (e: Throwable) {
            // onStart 에서 예외가 나가면 Flow 가 통째로 끊겨 DB 에 이미 있는 것까지 화면에서
            // 사라진다. 캐시가 있으면 오래된 데이터라도 보여주는 편이 빈 화면보다 낫다.
            // 반대로 캐시가 아예 없으면 대신 보여줄 것이 없으니, 화면이 오류를 다루도록 올린다.
            // 이때 원인을 여기서 한 번만 판별해 실어 보낸다 — 화면마다 예외 종류를 다시
            // 들여다보게 하면 판별 규칙이 여러 벌로 갈라진다.
            if (cachedAt == null) throw DataErrorException(e.toDataError(), e)

            Timber.e(e, "캐시 갱신에 실패해 기존 캐시를 유지한다 (key=${query.key})")
        }
    }

    suspend fun refresh() {
        val remote = fetchFromRemote()
        val entity = convertToEntity(remote)
        replaceToLocal(entity)
    }
}
