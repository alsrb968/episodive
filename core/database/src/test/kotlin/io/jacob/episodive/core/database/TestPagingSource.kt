package io.jacob.episodive.core.database

import androidx.paging.PagingSource
import androidx.paging.PagingState

class TestPagingSource<T : Any>(
    private val items: List<T>
) : PagingSource<Int, T>() {

    override fun getRefreshKey(state: PagingState<Int, T>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val page = params.key ?: 0
        val loadSize = params.loadSize
        val fromIndex = page * loadSize
        val toIndex = minOf(fromIndex + loadSize, items.size)

        return LoadResult.Page(
            data = items.subList(fromIndex, toIndex),
            prevKey = if (page == 0) null else page - 1,
            nextKey = if (toIndex < items.size) page + 1 else null
        )
    }
}